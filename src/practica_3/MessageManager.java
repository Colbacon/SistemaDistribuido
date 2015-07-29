/**
 * MessageManager, clase que implementa la clase Runnable. Un objeto de esta clase se asocia a 
 * un único nodo y va recibiendo mensajes y tratándolos de forma independiente a la ejecución del 
 * nodo asociado (dos hilos de ejecución distintos). También se encarga de enviar mensajes a los 
 * correspondientes “tubos” mediante llamadas a sus métodos por parte del nodo.
 * Los mensajes que trata son de de los siguientes tipos: trabajo, señal, token, solicitud de token y 
 * finalización. Se envían y reciben haciendo uso del formato Json y a través de beanstalkd. 
 * Contiene las variables necesarias para  la ejecución de los algoritmos de exclusión mutua de 
 * Neilsen-Mizuno y de terminación distribuida de Dijkstra-Scholten. 
 */

package practica_3;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.*;
import com.surftools.BeanstalkClientImpl.ClientImpl;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diego Blasco Quetglas
 */

public class MessageManager implements Runnable {
    
    final String host = "localhost";
    final int port = 15000;
    
    ClientImpl client;
    Gson gson;
    JsonParser parser;
    int my_id;
    ArrayList <Integer> parents;
    ArrayList <Integer> children;
    
    Semaphore received_job = new Semaphore(0);
    Semaphore token = new Semaphore(0);
    Semaphore mutex_j = new Semaphore(1);
    Semaphore mutex_dt = new Semaphore(1);
    Semaphore mutex_em = new Semaphore(1);
    
    
    ArrayList <Job> jobs = new ArrayList();   //Cola de trabajos
    String fn_spt = JobSPT.fn_spt;
    
    //Variables terminación distribuida.
    int arr_indeficit [];
    int in_deficit = 0;
    int out_deficit = 0;
    int dt_parent = -1;
    int terminated = 0;
    //Variables de exclusión mutua distribuida.
    int em_parent;
    int deferred = 0;
    boolean holding;
    
    
    boolean end = false;    
    
    public MessageManager(int node_id, ArrayList parents, ArrayList children){
        //client = new ClientImpl(); /*modificar para que en el jar se conecta a ip y host*/
        gson = new Gson();
        parser = new JsonParser();
        client = new ClientImpl(host, port);
        my_id = node_id;
        
        this.parents = parents;
        this.children = children;
        
        if(my_id!=0){
            arr_indeficit = new int [parents.size()];
            em_parent = my_id - 1;
            holding = (em_parent==0);
        }
        
    }
    
    /**
     * Recibe mensajes y realiza su tratamiento.
     */
    @Override
    public void run() {
        client.watch("t_"+my_id);   //Indica que tubo le corresponde.
        //Limpia los mensajes basura (mensajes de fin, porcedentes de anteriores ejecuciones) del tubo.
        cleantube();    
        
        while(!end){
            //Obtiene el JsonObject del mensaje.
            com.surftools.BeanstalkClient.Job msg_job = client.reserve(null); //Función bloqueante
            JsonObject jobj = decode(msg_job);

            switch(jobj.get("type").getAsString()){
                case "job":
                    terminated++;
                    receive_job(jobj);
                    break;
                case "signal":
                    receive_signal();
                    break;
                case "token":
                    token.release();
                    break;
                case "token_request":
                    receive_token_request(jobj);
                    break;
                case "finalize":
                    send_finalize();
                    end = true;
                    received_job.release();
                    break;
            }
     
            client.delete(msg_job.getJobId()); //Eliminación del mensaje
        }

            client.close();
    }
    /**
     * Elimina los posibles trabajos basura de finalización de anteriores ejecuciones
     */
    private void cleantube(){
        while (true){
            com.surftools.BeanstalkClient.Job j = client.reserve(0);
            if(j==null){
                break;
            }
            client.delete(j.getJobId());
        }
    }
    
    /**
     * Apartir del JsonObject obtiene el Job y lo introduce en la cola de trabajos
     * @param jobj 
     */
    public void receive_job(JsonObject jobj){
        try {
            int source = jobj.get("source").getAsInt();
            mutex_j.acquire();
            switch(jobj.get("job_type").getAsString()){
                case "JobCount":
                    JobCount job_count = gson.fromJson(jobj.get("job").getAsString(), JobCount.class);
                    job_count.setGs(this);
                    jobs.add(job_count);
                    break;
                case "JobImage":
                    JobImage job_image = gson.fromJson(jobj.get("job").getAsString(), JobImage.class);
                    job_image.setGs(this);
                    jobs.add(job_image);
                    break;
            }
            mutex_j.release(); 
            
            //Código correspondiente a la terminación distribuida
            mutex_dt.acquire();
            if(dt_parent == -1){
                dt_parent = source;
            }
            arr_indeficit[parents.indexOf(source)]++;
            in_deficit++;
            mutex_dt.release();

            received_job.release(); //Avisa al nodo de que hay al menos un trabajo disponible

        } catch (InterruptedException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Recibe un signal.
     */
    public void receive_signal(){
        try {
            mutex_dt.acquire();
            out_deficit--;
            mutex_dt.release();
            
            if(my_id!=0){
                send_signal();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Recibe la solicitud del token para la exclusión mutua distribuida.
     * @param jobj 
     */
    public void receive_token_request(JsonObject jobj){
        try {
            int originator = jobj.get("originator").getAsInt();
            int source = jobj.get("source").getAsInt();
            
            mutex_em.acquire();
            if (em_parent == 0){
                if (holding){
                    send_token(originator);
                    holding = false;
                }else{
                    deferred = originator;
                }
            }else{
                send_token_request(originator);
            }
            em_parent = source;
            mutex_em.release();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Envía un trabajo a un hijo
     * @param job
     * @param child
    */
    public void send_job(Job job, int child){
        try {
            mutex_dt.acquire(); 
            if(dt_parent!=-1 || my_id==0){
                //Codificación del mensaje de trabajo
                
                String job_type;
                switch(job.getClass().toString()){
                    case "class practica_3.JobCount":
                        job_type="JobCount";
                        break;
                    case "class practica_3.JobImage":
                        job_type="JobImage";
                        break;
                    default :
                        job_type="";
                }
                
                String cod_job = gson.toJson(job);
                JsonObject jobj = new JsonObject();
                jobj.addProperty("source", my_id);
                jobj.addProperty("type", "job");
                jobj.addProperty("job_type", job_type);
                jobj.addProperty("job", cod_job);
                
                //System.out.println(cod_job);
                //System.out.println(my_id+" envia trabajo");
                byte[] bytes = null;
                try {
                    bytes = jobj.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Envio del mensaje de trabajo
                client.useTube("t_"+child);
                client.put(0, 0, 0, bytes);
                
                out_deficit++;
                 
            }
             mutex_dt.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Envío del signal del algoritmo de la terminación distribuida.
     */
    public void send_signal(){
        try {
            mutex_dt.acquire();
            if (in_deficit > 1){ //Si no es el último signal
                
                for(int i=0; i<arr_indeficit.length; i++){
                    if((arr_indeficit[i]>1) || (arr_indeficit[i]== 1 && parents.get(i)!=dt_parent)){
                        try {
                            JsonObject jobj = new JsonObject();
                            jobj.addProperty("type", "signal");
                            byte[] bytes = jobj.toString().getBytes("UTF-8");
                            client.useTube("t_"+parents.get(i));
                            client.put(0, 0, 0, bytes);

                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        arr_indeficit[i]--;
                        in_deficit--;
                    }
                }
            } else if ((in_deficit==1)&&(out_deficit==0) && (terminated==0)){    //Si es el último signal
                try {
                    JsonObject jobj = new JsonObject();
                    jobj.addProperty("type", "signal");
                    byte[] bytes = jobj.toString().getBytes("UTF-8");
                    client.useTube("t_"+dt_parent);
                    client.put(0, 0, 0, bytes);
                    
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
                }

                arr_indeficit[parents.indexOf(dt_parent)]=0;
                in_deficit = 0;
                dt_parent = -1;
                
            }
            mutex_dt.release();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Envío del token.
     * @param destiny 
     */
    public void send_token(int destiny){
        try {
            JsonObject jobj = new JsonObject();
            jobj.addProperty("type", "token");
            byte[] bytes = jobj.toString().getBytes("UTF-8");
            client.useTube("t_"+destiny);
            client.put(0, 0, 0, bytes);
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Envio de la solicitud del token
     * @param originator 
     */
    public void send_token_request(int originator){
        try {
            
            JsonObject jobj = new JsonObject();
            jobj.addProperty("type", "token_request");
            jobj.addProperty("source", my_id);
            jobj.addProperty("originator", originator);
            byte[] bytes = jobj.toString().getBytes("UTF-8");
            client.useTube("t_"+em_parent);
            client.put(0, 0, 0, bytes);
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /**
     * Propaga la señal de finalización a cada hijo.
     */
    public void send_finalize(){
        try {
            for(Integer child : children){
                JsonObject jobj = new JsonObject();
                jobj.addProperty("type", "finalize");
                jobj.addProperty("source", my_id);
                byte[] bytes = jobj.toString().getBytes("UTF-8");
                client.useTube("t_"+child);
                client.put(0, 0, 0, bytes);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Envio de un mensaje "basura"
     * @param tube 
     */
    public void send_dummy(int tube){
        try {
            JsonObject jobj = new JsonObject();
            jobj.addProperty("type", "dummy");
            byte[] bytes = jobj.toString().getBytes("UTF-8");
            client.useTube("t_"+tube);
            client.put(0, 0, 0, bytes);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
    Funciones auxiliares
    */
    //Obtiene un objeto Json a partir del JobBeanstalk(mensaje)
    private JsonObject decode (com.surftools.BeanstalkClient.Job msg_job){
        
        byte[] bytes = msg_job.getData();
        String msg = null;
        try {
            msg = new String(bytes,"UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JsonElement elementObject = parser.parse(msg);
        
        return elementObject.getAsJsonObject();
    }
  
    
}
