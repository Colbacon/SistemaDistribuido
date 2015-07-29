/**
 * JobSTP, extensión de Job, representa un trabajo de actualización del spanning tree del algoritmo 
 * de terminación distribuida de Dijkstra-Scholten. Se encarga de:
 * -Crear el fichero y generar la cabecera, si no existe, sobre el que se va a guardar las 
 * relaciones padre-hijo entre nodos en el spanning tree que se van generando durante la ejecución.
 * -Realizar la actualización sobre el fichero cada vez que un nodo decide cual es su padre.
 * -Realizar la última escritura de la última línea del fichero, propia del formato de este.
 */
package practica_3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diego Blasco Quetglas
 */
public class JobSPT extends Job{
    
    private int spt_parent;     //Indica el padre actual del SPT para el nodo asociado.
    static final String fn_spt = "ficheros/spt.dot";
    static File file;
    
    public JobSPT(){
    }
    
    public JobSPT(MessageManager gs){
        super(gs);
        this.spt_parent = gs.dt_parent;
    }
    
    /**
     * Comprueba si desde el último mensaje, el padre del SPT ha cambiado, si es así
     * actualiza spt_parent con el nuevo padre y lo indica en el fichero spt.dot
     */
    public void realizeJob() {
        try {
            gs.mutex_dt.acquire();
            if(spt_parent != gs.dt_parent){
                spt_parent = gs.dt_parent;
                gs.mutex_dt.release();
                updateSPT();
                gs.mutex_dt.acquire();
            }
            gs.mutex_dt.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(JobSPT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Actualiza el fichero spt.dot con el nuevo padre del SPT para este nodo.
     */
    private void updateSPT(){

        DEMPreprotocol();
        
        FileWriter fw = null;
        try {
             
            fw = new FileWriter(file,true);
            
            fw.append("\n\t\t"+ spt_parent + " -> " + gs.my_id); //parent -> child
            fw.close();
 
        } catch (IOException ex) {
            Logger.getLogger(MessageManager.class.getName()).log(Level.SEVERE, null, ex);
            if(fw!=null) try {
                fw.close();
            } catch (IOException ex1) {
                Logger.getLogger(JobSPT.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        DEMPostprotocol();

    }
    
    /**
     * Realiza la última escritura del fichero, escribiendo (/n}) para estar conforme con el formato del fichero.
     */
    public void lastWriteOnFile(){
        FileWriter fw;
        try {
            fw = new FileWriter(file,true);
            fw.write("\n}");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(JobSPT.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Crea el fichero del spt si no existe y escribe la cabezera. 
     */
    @Override
    protected void createFile() {
        file = new File (fn_spt);
        FileWriter fw = null;
        try {            
            if(!file.exists()){
                file.createNewFile();
            }
            fw = new FileWriter(file);
            fw.write("digraph G {");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(JobSPT.class.getName()).log(Level.SEVERE, null, ex);
            if(fw!=null) try {
                fw.close();
            } catch (IOException ex1) {
                Logger.getLogger(JobSPT.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
    
    
    @Override
    public void realizeJob(Job job) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public Job[] calcJob(int n_div) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }




   

 
    
}
