/**
 * Node, clase que implementa Runnable y representa un nodo del sistema. Obtiene los padres de 
 * los que recibe y los hijos a los que envía, imprime esta información por pantalla, inicializa su 
 * MessageManager en un hilo de ejecución e instancia un objeto JobSPT. Mientras que no se 
 * reciba la último signal de sus hijos, comprueba si se ha actualizado el nodo padre del SPT, coge
 * un trabajo de la cola de trabajos recibidos por su MessageManager, divide el trabajo en 
 * NúmeroDeHijos+1 trabajos, realiza el suyo y envía uno a cada hijo. Cada vez que acaba un 
 * trabajo envía un signal
 */
package practica_3;

import java.io.BufferedReader;

import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diego Blasco Quetglas
 */
public class Node implements Runnable {
    
    private int my_id;
    private MessageManager gs;
    
    private ArrayList <Integer> parents;
    private ArrayList <Integer> children;
    
    
    public Node(int node_id){
        my_id = node_id;
        parents = new ArrayList();
        children = new ArrayList();
    }

    @Override
    public void run() {
        Job[] jobs;
        try {
            //Obtiene padres e hijos y lo muestra por consola
            getParentsAndChildren();
            printInfo();
            //Inicializa el gestor de mensajes y crea el hilo para recibir los mensajes de forma continua.
            gs = new MessageManager(my_id, parents, children);
            Runnable runnable = gs;
            Thread r = new Thread(runnable);
            r.start();
            
            JobSPT spt_job= new JobSPT(gs);
            
            while (true){
                
                gs.received_job.acquire();  //Bloqueado Hasta que gs reciba un trabajo o señal de fin.
                if(gs.end){ //Si gs ha acabado (recibido mensaje de fin).
                    break;
                }
                //Comprueba si se ha actualizado el nodo padre del SPT
                spt_job.realizeJob(); 
                //Obtiene un trabajo de la cola.
                gs.mutex_j.acquire();
                Job job =  gs.jobs.remove(0);
                gs.mutex_j.release();

                //Obtiene el trabajo propio y el de los hijos; envía a los hijos.
                jobs = new Job[children.size()+1];
                jobs = job.calcJob(jobs.length);
                sendJobToChildren(jobs);
                //Realiza su trabajo.
                job.realizeJob(jobs[0]);

                gs.terminated--;    //Decrementa en uno los trabajos pendientes.
                gs.send_signal();  //Envía señal de que ha acabado el trabajo
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    /**
     * Envía el trabajo a sus hijos.
     * @param jobs 
     */
    private void sendJobToChildren(Job[] jobs){
        for(int i=0; i<children.size();i++){
            gs.send_job(jobs[i+1], children.get(i));
            
        }
    }
    
    /**
     * Obtiene los padres e hijos a partir del graph.dot
    */
    private void getParentsAndChildren(){
        try {
            String line;
            int i,j;
            String[] num = new String [2];

            BufferedReader br = new BufferedReader(new FileReader (new File (EnviromentNode.fn_graph)));
            while((line=br.readLine())!=null){
                num=line.split("->");
                if(num.length==2){
                    num[0]=num[0].replaceAll("[^\\d]", "");
                    num[1]=num[1].replaceAll("[^\\d]", "");
                    i=Integer.parseInt(num[0]);
                    j=Integer.parseInt(num[1]);
                    
                    if(i==my_id){
                        children.add(j);
                    } else if(j==my_id){
                        parents.add(i);
                    }
                }
            }
            
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        }     
    }
    /**
     * Imprime los nodos de quien recibe y a los que envía.
     */
    private void printInfo(){
        String str = "{ Nodo " + my_id;
        if(!parents.isEmpty()){
            str+="\tRecibe de: ";
            for (Integer padre : parents) {
                str+=padre+" ";
            }
        }
        if(!children.isEmpty()){
            str+="\tEnvia a: ";
            for (Integer hijo : children) {
                str+=hijo+" ";
            }
        }
        str+="}";
        System.out.println(str);
    }
}
