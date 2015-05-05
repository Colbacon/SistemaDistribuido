/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dbq560
 */
public class JobCount extends Job{
    
    private long max_count;
    //ruta del fichero donde se almacena el total del contador
    final private String fn_totales = "src/files/totales.txt";
    
    public JobCount(GestorMensajes gs, long max_count){
        super(gs);
        this.max_count=max_count;
    }
    public JobCount(long max_count){
        this.max_count=max_count;
    }

    @Override
    public Job[] calcJob(int n_div) {
        
        int i=0;
        long count=0;
        JobCount[] jobs;
        
        if(n_div==0) return null; 
        
        jobs = new JobCount [n_div];
        count=max_count/n_div;
        jobs[i]=new JobCount(count+max_count%n_div); //La cuenta teniendo cuenta el resto de la división.
        i++;
        while(i<n_div){
            jobs[i]=new JobCount(count);
            i++;
        }
        return jobs;
        
    }

    @Override
    public void realizeJob(Job job) {
//        System.out.println(gs.my_id+": cuenta a realizar: " +max_count);
        JobCount job_count = (JobCount) job;
        long total_count = job_count.max_count;
        int count=0;

        long tenth_part = (long) (total_count/10);
        for(int i = 0; i<10; i++){
            while(count<tenth_part){
                count++;
            }
            writeOnFile(count);
            count = 0;
        }
        writeOnFile ((long) (total_count % 10));
    }

    //Comprobar si fichero existe!!!!!
    private void writeOnFile(long count) {
        BufferedReader br = null;
        FileWriter fw = null;
        String str;
        
        long value=0;
        
        DEMPreprotocol();
        try {
            File file = new File (fn_totales);
            //Lectura del contador en el fichero e incremento
            br = new BufferedReader(new FileReader (file));

            if((str = br.readLine()) != null){
                value = Long.parseLong(str);
            }
            value += count;
            br.close();
            //Escritura del nuevo valor
            fw = new FileWriter(file);
            fw.write(Long.toString(value));
          
            fw.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            try {
                if(fw!=null) fw.close();
                if(br!=null) br.close();
            } catch (IOException ex1) {
                Logger.getLogger(JobCount.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        DEMPostprotocol();
    }


    public long getMax_count() {
        return max_count;
    }
    
    
}
