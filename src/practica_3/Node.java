/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3;

import java.io.BufferedReader;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
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
            //Inicializar el gestor de mensajes y crear hilo para recibir los mensajes
            //de forma continua.
            gs = new GestorMensajes(my_id, parents, children);
            Runnable runnable = gs;
            Thread r = new Thread(runnable);
            r.start();
            
            STPJob stp_job= new STPJob(gs);
            
            while (true){
            
                gs.received_job.acquire();  //Bloqueado Hasta que gs reciba un trabajo o señal de fin.
                if(gs.end){ //Si gs ha acabado (recibido mensaje de fin.
                    break;
                }
                
                //Comprueba si se ha actualizado el nodo padre del STP
                stp_job.realizeJob(); 

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
                gs.send_signal();  
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    
    private void sendJobToChildren(Job[] jobs){
        for(int i=0; i<children.size();i++){
            gs.send_job(jobs[i+1], children.get(i));
            
        }
    }
    
  
    /**
     * Métodos de inicio
    */
    private void getParentsAndChildren(){
        try {
            String line;
            int i,j;
            String[] num = new String [2];

            BufferedReader br = new BufferedReader(new FileReader (new File (fn_graph)));
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
    
    private void printInfo(){
        String str = "{ Nodo " + my_id;
        if(!parents.isEmpty()){
            str+="\tRecibe: ";
            for (Integer padre : parents) {
                str+=padre+" ";
            }
        }
        if(!children.isEmpty()){
            str+="\tEnvia: ";
            for (Integer hijo : children) {
                str+=hijo+" ";
            }
        }
        str+="}";
        System.out.println(str);
    }
}
