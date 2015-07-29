/**
 * Job, clase abstracta que representa un trabajo y de la cual heredan JobImage, JobCount y 
 * JobSTP. Los nodos reciben, dividen y realizan los trabajos, y envían objetos de esta clase 
 * (polimorfismo) Declara los métodos abstractos para la división del trabajo, la realización de este y 
 * la creación y preparación de fichero sobre el que se va a tratar de forma distribuida para cada 
 * trabajo. Además, implementa el  preprotocolo y postprotocolo del algoritmo de exclusión mutua 
 * distribuida de Neilsen-Mizuno.
 */
package practica_3;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diego Blasco Quetglas
 */
public abstract class Job {
    
    protected MessageManager gs;
    
    public Job(MessageManager gs){
        this.gs=gs;
    }
    public Job(){
        
    }
    
    public abstract Job[] calcJob(int n_div);
    public abstract void realizeJob(Job job);
    protected abstract void createFile();
    
    /**
     * Preprotocolo exclusión mutua distribuida.
     */
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
   /**
    * Postprotocolo exclusión mutua distribuida
    */
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

    public void setGs(MessageManager gs) {
        this.gs = gs;
    }

    public MessageManager getGs() {
        return gs;
    }
    
    
}
