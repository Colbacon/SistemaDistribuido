<<<<<<< HEAD
/**
 * Node, clase que implementa Runnable y representa un nodo del sistema. Obtiene los padres de 
 * los que recibe y los hijos a los que envía, imprime esta información por pantalla, inicializa su 
 * MessageManager en un hilo de ejecución e instancia un objeto JobSPT. Mientras que no se 
 * reciba la último signal de sus hijos, comprueba si se ha actualizado el nodo padre del SPT, coge
 * un trabajo de la cola de trabajos recibidos por su MessageManager, divide el trabajo en 
 * NúmeroDeHijos+1 trabajos, realiza el suyo y envía uno a cada hijo. Cada vez que acaba un 
 * trabajo envía un signal
=======
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
 */
package practica_3;

import java.io.BufferedReader;
<<<<<<< HEAD

import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
=======
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
<<<<<<< HEAD
 * @author Diego Blasco Quetglas
 */
public class Node implements Runnable {
    
    private int my_id;
    private MessageManager gs;
    
    private ArrayList <Integer> parents;
    private ArrayList <Integer> children;
    
=======
 * @author colbacon
 */
public class Node implements Runnable {
    
    int my_id;
    GestorMensajes gs;
    
    ArrayList <Integer> parents;
    ArrayList <Integer> children;
    
    final String fn_graph = "src/files/graph.dot";
    final String fn_totales = "src/files/totales.txt";
    final String fn_stp = "src/files/stp.dot";
    
//    final static String fn_graph = "graph.dot";
//    final static String fn_totales = "totales.txt";
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
    
    public Node(int node_id){
        my_id = node_id;
        parents = new ArrayList();
        children = new ArrayList();
<<<<<<< HEAD
=======
 
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
    }

    @Override
    public void run() {
        Job[] jobs;
        try {
            //Obtiene padres e hijos y lo muestra por consola
            getParentsAndChildren();
            printInfo();
<<<<<<< HEAD
            //Inicializa el gestor de mensajes y crea el hilo para recibir los mensajes de forma continua.
            gs = new MessageManager(my_id, parents, children);
=======
            //Inicializar el gestor de mensajes y crear hilo para recibir los mensajes
            //de forma continua.
            gs = new GestorMensajes(my_id, parents, children);
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
            Runnable runnable = gs;
            Thread r = new Thread(runnable);
            r.start();
            
<<<<<<< HEAD
            JobSPT spt_job= new JobSPT(gs);
            
            while (true){
                
                gs.received_job.acquire();  //Bloqueado Hasta que gs reciba un trabajo o señal de fin.
                if(gs.end){ //Si gs ha acabado (recibido mensaje de fin).
                    break;
                }
                //Comprueba si se ha actualizado el nodo padre del SPT
                spt_job.realizeJob(); 
=======
            STPJob stp_job= new STPJob(gs);
            
            while (true){
            
                gs.received_job.acquire();  //Bloqueado Hasta que gs reciba un trabajo o señal de fin.
                if(gs.end){ //Si gs ha acabado (recibido mensaje de fin.
                    break;
                }
                
                //Comprueba si se ha actualizado el nodo padre del STP
                stp_job.realizeJob(); 

>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
                //Obtiene un trabajo de la cola.
                gs.mutex_j.acquire();
                Job job =  gs.jobs.remove(0);
                gs.mutex_j.release();

                //Obtiene el trabajo propio y el de los hijos; envía a los hijos.
                jobs = new Job[children.size()+1];
                jobs = job.calcJob(jobs.length);
                sendJobToChildren(jobs);
                //Realiza su trabajo.
<<<<<<< HEAD
                job.realizeJob(jobs[0]);

                gs.terminated--;    //Decrementa en uno los trabajos pendientes.
                gs.send_signal();  //Envía señal de que ha acabado el trabajo
=======
               
                job.realizeJob(jobs[0]);

                
                gs.terminated--;    //Decrementa en uno los trabajos pendientes.
                gs.send_signal();  
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
<<<<<<< HEAD
    /**
     * Envía el trabajo a sus hijos.
     * @param jobs 
     */
=======
    
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
    private void sendJobToChildren(Job[] jobs){
        for(int i=0; i<children.size();i++){
            gs.send_job(jobs[i+1], children.get(i));
            
        }
    }
    
<<<<<<< HEAD
    /**
     * Obtiene los padres e hijos a partir del graph.dot
=======
  
    /**
     * Métodos de inicio
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
    */
    private void getParentsAndChildren(){
        try {
            String line;
            int i,j;
            String[] num = new String [2];

<<<<<<< HEAD
            BufferedReader br = new BufferedReader(new FileReader (new File (EnviromentNode.fn_graph)));
=======
            BufferedReader br = new BufferedReader(new FileReader (new File (fn_graph)));
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
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
<<<<<<< HEAD
    /**
     * Imprime los nodos de quien recibe y a los que envía.
     */
    private void printInfo(){
        String str = "{ Nodo " + my_id;
        if(!parents.isEmpty()){
            str+="\tRecibe de: ";
=======
    
    private void printInfo(){
        String str = "{ Nodo " + my_id;
        if(!parents.isEmpty()){
            str+="\tRecibe: ";
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
            for (Integer padre : parents) {
                str+=padre+" ";
            }
        }
        if(!children.isEmpty()){
<<<<<<< HEAD
            str+="\tEnvia a: ";
=======
            str+="\tEnvia: ";
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
            for (Integer hijo : children) {
                str+=hijo+" ";
            }
        }
        str+="}";
        System.out.println(str);
    }
}
