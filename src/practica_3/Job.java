/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dbq560
 */
public abstract class Job {
    
    protected GestorMensajes gs;
    
    public Job(GestorMensajes gs){
        this.gs=gs;
    }
    public Job(){
        
    }
    
    public abstract Job[] calcJob(int n_div);
    public abstract void realizeJob(Job job);
    
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

    public void setGs(GestorMensajes gs) {
        this.gs = gs;
    }

    public GestorMensajes getGs() {
        return gs;
    }
    
    
}
