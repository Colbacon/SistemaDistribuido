<<<<<<< HEAD
/**
 * EnviromentNode, clase que representa el nodo de entorno del sistema. Genera el resto de nodos 
 * del grafo y ejecuta cada uno en un hilo distinto. Obtiene los hijos a los que envía e imprime esta 
 * información por la salida estándar. Inicializa su MessageManager en un hilo de ejecución, crea y 
 * prepara el trabajo de contar (JobCount), de difuminado de imagen (JobImage), divide cada uno en 
 * númeroDeHijos trabajos y reparte estos entre sus hijos. Realiza una espera activa hasta que
 * reciba el último signal del algoritmo de terminación distribuida, indicando que sus nodos hijos han 
 * acabado la tarea, imprime en milisegundos el tiempo que ha durado el trabajo distribuido. 
 * Finalmente imprime el contenido del fichero de contar, muestra la imagen difuminada y realiza las 
 * operaciones restantes (cierre del MessageManager y cierre del fichero de spannig tree)
 */

=======
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
// * and open the template in the editor.
 */
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
package practica_3;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
<<<<<<< HEAD
=======
import java.io.FileWriter;
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

<<<<<<< HEAD

/**
 *
 * @author Diego Blasco Quetglas
=======
/**
 *
 * @author colbacon
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
 */

public class EnviromentNode {

<<<<<<< HEAD
    public final static String fn_graph = "ficheros/graph.dot";
    
    private final static long totalCount = 333333333;
    
    private static int n_nodes;
    private static ArrayList <Integer> children;
    private static MessageManager gs;
   
 
    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        generateNodes();
        getChildren();
        printInfo();

        //Inicializa su gestor de mensajes.
        gs = new MessageManager(0,null, children);
        Runnable runnable = gs;
        Thread r = new Thread(runnable);
        r.start();
        //Crea/Inicializa el fichero del SPT
        JobSPT spt = new JobSPT();
        spt.createFile();

        long time_start, time_end;
        time_start = System.currentTimeMillis();

        createCountJob();
        createImgJob();

        //Espera hasta que reciba los signals de sus hijos.
        while(gs.out_deficit > 0){
            sleep(1);
        }
        time_end = System.currentTimeMillis();
        System.out.println("La tarea ha durado "+ ( time_end - time_start ) +" milisegundos");
        //Propaga mensaje de finalización a sus hijos.
        gs.send_finalize();
        //Finaliza la ejecución de su gestor de mensajes.
        gs.end=true;
        //Envia un mensaje "basura" al gs del propio nodo para que deje de estar bloqueado y pueda finalizar.
        gs.send_dummy(0);   
        //Realiza la última escritura del fichero ("\n}) para estar conforme con el formato de este.
        spt.lastWriteOnFile();
        //Imprime el valor de fichero totales.txt y muestra la imagen tras el difuminado
        JobCount.showTotalCount();
        JobImage.showImgOut();

        System.out.println("_________Fin de la ejecución________");
    }
    
    /**
     * Crea un trabajo de contar, lo divide y envía a sus hijos.
     */
    private static void createCountJob(){
        Job job = new JobCount(gs,totalCount); 
        job.createFile();
        Job [] jobs = job.calcJob(children.size());
        sendJobsToChildren(jobs);
    }
    /**
     * Crea un trabajo de difuminar una imagen, lo divide y envía a sus hijos.
     */
    private static void createImgJob(){
        BufferedImage bimg;
        try {
            bimg = ImageIO.read(new File(JobImage.fn_imgIn));
            Job job = new JobImage(gs, 0, bimg.getWidth());
            job.createFile();
            Job [] jobs = job.calcJob(children.size());
            sendJobsToChildren(jobs);
        } catch (IOException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        }        
=======
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
        
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
    }
    
    private static void sendJobsToChildren(Job[] jobs){
        for(int i=0; i<children.size(); i++){
            gs.send_job(jobs[i], children.get(i));
        }
    }
<<<<<<< HEAD
    

    /**
     * Genera los nodos y los ejecuta en distintos hilos.
     */
=======
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
    

    
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
    private static void generateNodes(){
        n_nodes = getNumberNodes();
        for(int i=1; i<n_nodes; i++){
            Runnable node = new Node(i);
            new Thread(node).start();
        }
    }
    
<<<<<<< HEAD
    /**
     * A partir del grafo de graph.dot obtiene el número de nodos que lo compone
     * @return 
     */
=======
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
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
    
<<<<<<< HEAD
    /**
     * A partir del grafo de graph.dot, obtiene los nodos hjos de este nodo.
     */
=======
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
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
<<<<<<< HEAD
=======
      
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
        }   catch (FileNotFoundException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
<<<<<<< HEAD
    /**
     * Imprime la información sobre los nodos a los que envía.
     */
    private static void printInfo(){
        String str = "{ Nodo " + 0;
        if(!children.isEmpty()){
            str+="\tEnvia a: ";
=======
    private static void printInfo(){
        String str = "{ Nodo " + 0;
        if(!children.isEmpty()){
            str+="\tEnvia: ";
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
            for (Integer child : children) {
                str+=child+" ";
            }
        }
        str+="}";
        System.out.println(str);
    }
<<<<<<< HEAD
    
=======
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
}
