/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
// * and open the template in the editor.
 */
package practica_3;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author colbacon
 */

public class EnviromentNode {

    final static String fn_graph = "src/files/graph.dot";
    final static String fn_stp = "src/files/stp.dot";
    final static String fn_img = "src/files/image";
    
//    final static String fn_graph = "graph.dot";
    final static String fn_totales = "src/files/totales.txt";
    
    
    final static long total = 333333333;
    static int n_nodes;
    static ArrayList <Integer> children;
    
    static GestorMensajes gs;
    
 
    
    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        try {
            generateNodes();
            getChildren();
            printInfo();
            initFiles();
            
            //Inicializa su gestor de mensajes.
            gs = new GestorMensajes(0,null, children);
            Runnable runnable = gs;
            Thread r = new Thread(runnable);
            r.start();
            
            
            //Crea distintos tipos de trabajos y divide estos entre sus hijos.
            Job job = new JobCount(gs,total); //trabajo contador
            Job [] jobs = job.calcJob(children.size());
            sendJobsToChildren(jobs);
            
            //Espera hasta que reciba los signals de sus hijos.
            while(gs.out_deficit > 0){
                sleep(1);
            }
            
            //Envía mensaje de finalización.
            gs.send_finalize();
            //Finaliza la ejecución de su gestor de mensajes.
            gs.end=true;
            /**
             * Envía un mensaje "basura" al gs del propio nodo para que
             * este pueda finalizar.
             */
            gs.send_dummy(0);
            
            
            //Lee fichero totales.txt 
            BufferedReader br = new BufferedReader(new FileReader (new File(fn_totales)));
            System.out.println("Total contador: "+br.readLine());
            br.close(); 
        
            //Pone el "}" de cierre de stp.dot
            FileWriter fw = new FileWriter (new File(fn_stp), true);
            fw.append("\n}");
            fw.close();
            
            
            System.out.println("_________Fin de la ejecución________");
            
        } catch (IOException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private static void sendJobsToChildren(Job[] jobs){
        for(int i=0; i<children.size(); i++){
            gs.send_job(jobs[i], children.get(i));
        }
    }
    private static void distributeImgJob(){
        try {
            int height, div_height, y=0, children_size = children.size();
                    
            File f_img = new File(fn_img);
            BufferedImage img =  ImageIO.read(f_img);
            
            height = img.getHeight();   //Altura de la imagen original
            div_height = height/children_size;    //Altura de cada porción de la imagen tras la división
            
            boolean first = false;
            for (Integer child : children){
                if(first){
                    //enviar mensaje con el resto
                    y+=height%children_size;    //Añade el resto
                    first = false;
                }
                    //enviar mensaje sin el resto
                
                y+=div_height; //Marca la línea en donde empezará el siguiente nodo
                
            }
        } catch (IOException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void initFiles() {
        //Inicializacion de totales.txt
        try{
            File file_totales = new File (fn_totales);
            if(!file_totales.exists()){
                file_totales.createNewFile();
            } else{
                FileWriter fw = new FileWriter(file_totales);
                fw.write(Integer.toString(0));
                fw.close();
            }
            //Inicializacion de stp.dot
            File file_stp = new File (fn_stp);
            FileWriter fw = new FileWriter(file_stp);
            if(!file_stp.exists()){
                file_totales.createNewFile();
            }
            fw.write("digraph G {");
            fw.close();
        }catch (IOException ex){
        }
    }
    

    
    private static void generateNodes(){
        n_nodes = getNumberNodes();
        for(int i=1; i<n_nodes; i++){
            Runnable node = new Node(i);
            new Thread(node).start();
        }
    }
    
    private static int getNumberNodes(){
        try {
            String line;
            int i;
            ArrayList <Integer> nodes = new ArrayList();
            String[] num = new String [2];
            BufferedReader br = new BufferedReader(new FileReader (new File (fn_graph)));
            while((line=br.readLine())!=null){
                num=line.split("->");
                if(num.length==2){
                    num[1]=num[1].replaceAll("[^\\d]", "");
                    i=Integer.parseInt(num[1]);
                    if(!nodes.contains(i)){
                        nodes.add(i);
                    }
                }
            }
            br.close();
            
            return nodes.size()+1;
        } catch (FileNotFoundException ex) {
           Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex); 
        } catch (IOException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    private static void getChildren(){
        try {
            String line;
            int i,j;
            String[] num = new String [2];
            children = new ArrayList();
            BufferedReader br = new BufferedReader(new FileReader (new File (fn_graph)));
            while((line=br.readLine())!=null){
                num=line.split("->");
                if(num.length==2){
                    num[0]=num[0].replaceAll("[^\\d]", "");
                    num[1]=num[1].replaceAll("[^\\d]", "");
                    i=Integer.parseInt(num[0]);
                    j=Integer.parseInt(num[1]);
                    
                    if(i==0){
                        children.add(j);
                    }
                }
            }
            br.close();
      
        }   catch (FileNotFoundException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void printInfo(){
        String str = "{ Nodo " + 0;
        if(!children.isEmpty()){
            str+="\tEnvia: ";
            for (Integer child : children) {
                str+=child+" ";
            }
        }
        str+="}";
        System.out.println(str);
    }
}
