/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dbq560
 */
public class STPJob extends Job{
    
    private int stp_parent;     //Indica el padre actual del STP para el nodo asociado.
    private final String fn_stp = "src/files/stp.dot";
    
    public STPJob(GestorMensajes gs){
        super(gs);
        this.stp_parent = gs.dt_parent;
    }
    
    /**
     * Comprueba si desde el último mensaje, el padre del STP ha cambiado, si es así
     * actualiza stp_parent con el nuevo padre y lo indica en el fichero stp.dot
     * 
     */
    public void realizeJob() {
        try {
            gs.mutex_dt.acquire();
            if(stp_parent != gs.dt_parent){
                stp_parent = gs.dt_parent;
                gs.mutex_dt.release();
                updateSTP();
                gs.mutex_dt.acquire();
            }
            gs.mutex_dt.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(STPJob.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Actualiza el fichero stp.dot con el nuevo padre del STP para este nodo.
     */
    private void updateSTP(){

        DEMPreprotocol();
        
        FileWriter fw = null;
        try {
             
            fw = new FileWriter(new File(fn_stp),true);
            
            fw.append("\n\t\t"+ stp_parent + " -> " + gs.my_id); //parent -> child
            fw.close();
 
        } catch (IOException ex) {
            Logger.getLogger(GestorMensajes.class.getName()).log(Level.SEVERE, null, ex);
            if(fw!=null) try {
                fw.close();
            } catch (IOException ex1) {
                Logger.getLogger(STPJob.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        DEMPostprotocol();

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
