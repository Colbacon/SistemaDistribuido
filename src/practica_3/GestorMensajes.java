/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.*;
import com.surftools.BeanstalkClientImpl.ClientImpl;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author colbacon
 */

/**
 * Estructura de los mensajes:
 * Trabajo: (fuente,tipo,trabajo)
 * Signal: (tipo)
 * Token_request: (type, source, originator)
 */
public class GestorMensajes implements Runnable {
    
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
    String fn_stp = "src/files/stp.dot";
    
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
    
    public GestorMensajes(int node_id, ArrayList parents, ArrayList children){
        client = new ClientImpl(); /*modificar para que en el jar se conecta a ip y host*/
        gson = new Gson();
        parser = new JsonParser();
        //client = new ClientImpl(host, port);
        my_id = node_id;
        
        this.parents = parents;
        this.children = children;
        
        if(my_id!=0){
            arr_indeficit = new int [parents.size()];
            em_parent = my_id - 1;
            holding = (em_parent==0);
        }
        
    }
    
    @Override
    public void run() {
        
        client.watch("t_"+my_id);   //Indica que tubo le corresponde.
        //Limpia los mensajes basura (mensajes de fin, porcedentes de anteriores ejecuciones) del tubo.
        cleantube();    
        
        while(!end){
            //Obtiene el JsonObject del mensaje.
            com.surftools.BeanstalkClient.Job msg_job = client.reserve(null);
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
    
    //Elimina los posibles trabajos basura de finalización de anteriores ejecuciones
    private void cleantube(){
        while (true){
            com.surftools.BeanstalkClient.Job j = client.reserve(0);
            if(j==null){
                break;
            }
            client.delete(j.getJobId());
        }
    }
    
    /*
    Recibir
    */
    public void receive_job(JsonObject jobj){
        try {
            int source = jobj.get("source").getAsInt();
            mutex_j.acquire();
            switch(jobj.get("job_type").getAsString()){
                case "JobCount":
//                    jobs.add(gson.fromJson(jobj.get("job").getAsString(), JobCount.class));
                    JobCount job = gson.fromJson(jobj.get("job").getAsString(), JobCount.class);
                    job.setGs(this);
                    jobs.add(job);
                    break;
                case "ImgCount":
                   //jobs.add(gson.fromJson(jobj.get("job"), Job.class));
                    break;
            }
            mutex_j.release(); 
            /*
            IMPLEMENTAR TERMINACION DISTRIBUIDA(Y)
            */
            mutex_dt.acquire();
            if(dt_parent == -1){
                dt_parent = source;
                //updateSTP(dt_parent, my_id);
            }
            arr_indeficit[parents.indexOf(source)]++;
            in_deficit++;
            mutex_dt.release();


            received_job.release(); //Avisa al nodo de que hay al menos un trabajo disponible

       
        
        } catch (InterruptedException ex) {
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    public void receive_signal(){
        try {
            mutex_dt.acquire();
            out_deficit--;
            mutex_dt.release();
            
            if(my_id!=0){
                send_signal();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void receive_token_request(JsonObject jobj){
        try {
            int originator = jobj.get("originator").getAsInt();
            int source = jobj.get("source").getAsInt();
            
            mutex_em.acquire();
            if (em_parent == 0){
                if (holding){
                    //System.out.println(my_id+" token envia a "+originator);
                    send_token(originator);
                    holding = false;
                }else{
                    deferred = originator;
                }
            }else{
                //System.out.println("sol_token: yo "+ my_id+ " padre "+em_parent + " source "+source+" originator "+originator);
                send_token_request(originator);
            }
            em_parent = source;
            mutex_em.release();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
    Enviar
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
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
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
                            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
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
                    Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
                }

                arr_indeficit[parents.indexOf(dt_parent)]=0;
                in_deficit = 0;
                dt_parent = -1;
                
            }
            mutex_dt.release();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void send_token(int destiny){
        try {
            JsonObject jobj = new JsonObject();
            jobj.addProperty("type", "token");
            byte[] bytes = jobj.toString().getBytes("UTF-8");
            client.useTube("t_"+destiny);
            client.put(0, 0, 0, bytes);
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
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
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void send_finalize(){
        try {
            //Propaga la señal de finalización a cada hijo.
            for(Integer child : children){
                JsonObject jobj = new JsonObject();
                jobj.addProperty("type", "finalize");
                jobj.addProperty("source", my_id);
                byte[] bytes = jobj.toString().getBytes("UTF-8");
                client.useTube("t_"+child);
                client.put(0, 0, 0, bytes);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void send_dummy(int tube){
        try {
            JsonObject jobj = new JsonObject();
            jobj.addProperty("type", "dummy");
            byte[] bytes = jobj.toString().getBytes("UTF-8");
            client.useTube("t_"+tube);
            client.put(0, 0, 0, bytes);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
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
