<<<<<<< HEAD
/**
 * Job, clase abstracta que representa un trabajo y de la cual heredan JobImage, JobCount y 
 * JobSTP. Los nodos reciben, dividen y realizan los trabajos, y envían objetos de esta clase 
 * (polimorfismo) Declara los métodos abstractos para la división del trabajo, la realización de este y 
 * la creación y preparación de fichero sobre el que se va a tratar de forma distribuida para cada 
 * trabajo. Además, implementa el  preprotocolo y postprotocolo del algoritmo de exclusión mutua 
 * distribuida de Neilsen-Mizuno.
=======
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
 */
package practica_3;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
<<<<<<< HEAD
 * @author Diego Blasco Quetglas
 */
public abstract class Job {
    
    protected MessageManager gs;
    
    public Job(MessageManager gs){
=======
 * @author dbq560
 */
public abstract class Job {
    
    protected GestorMensajes gs;
    
    public Job(GestorMensajes gs){
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
        this.gs=gs;
    }
    public Job(){
        
    }
    
    public abstract Job[] calcJob(int n_div);
    public abstract void realizeJob(Job job);
<<<<<<< HEAD
    protected abstract void createFile();
    
    /**
     * Preprotocolo exclusión mutua distribuida.
     */
=======
    
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
    protected void DEMPreprotocol(){
        try {
            gs.mutex_em.acquire();
            
            if (!gs.holding){
                gs.send_token_request(gs.my_id);
                gs.em_parent = 0;
                gs.mutex_em.release();

                gs.token.acquire();
                
                gs.mutex_em.acquire();
            }
            gs.holding = false;
            gs.mutex_em.release();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
<<<<<<< HEAD
   /**
    * Postprotocolo exclusión mutua distribuida
    */
=======
   
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
    protected void DEMPostprotocol(){
        try {
            gs.mutex_em.acquire();
            if(gs.deferred != 0){
                gs.send_token(gs.deferred);
                gs.deferred = 0;
            }else{
                gs.holding = true;
            }
            gs.mutex_em.release();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

<<<<<<< HEAD
    public void setGs(MessageManager gs) {
        this.gs = gs;
    }

    public MessageManager getGs() {
=======
    public void setGs(GestorMensajes gs) {
        this.gs = gs;
    }

    public GestorMensajes getGs() {
>>>>>>> 595750985ce675c29d9b665b572c35618ce2dff2
        return gs;
    }
    
    
}
