/**
 * JobCount, extensión de Job, representa un trabajo de contar. Se encarga de:
 * -Crear el fichero, si no existe, sobre el que se va a cargar/guardar la cuenta distribuida 
 * durante la ejecución.
 * -Dividir el trabajo de contar en N JobCount, generando N trabajos con sus correspondientes subcuentas.
 * -Realizar el trabajo de contar sobre el número a contar. Cada vez que se llegue a la décima 
 * parte de dicho número, se suma al valor actual de conteo del fichero.
 * -Imprimir por salida estándar el contenido del fichero de cuenta.
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
 * @author Diego Blasco Quetglas
 */
public class JobCount extends Job{
    
    private long max_count;
    //ruta del fichero donde se almacena el total del contador
    final static String fn_totales = "ficheros/totales.txt";
    
    public JobCount(MessageManager gs, long max_count){
        super(gs);
        this.max_count=max_count;
    }
    public JobCount(long max_count){
        this.max_count=max_count;
    }
    
    /**
     * Divide el trabajo de contar en n_div trabajos.
     * @param n_div
     * @return 
     */
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
    
    /**
     * Realiza la cuenta y a cada décima parte la añade al fichero del conteo.
    */
    @Override
    public void realizeJob(Job job) {
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
    
    /**
     * Carga el valor de la cuenta del fichero y se guarda añadiendole el valor de count.
     * @param count 
     */
    private void writeOnFile(long count) {
        BufferedReader br = null;
        FileWriter fw = null;
        String str;
        
        long value=0;
        
        DEMPreprotocol();
        try {
            File file = new File(fn_totales);
            //Lectura del contador en el fichero e incremento
            br = new BufferedReader(new FileReader (file));
            if((str = br.readLine()) != null){
                value = Long.parseLong(str);
            }
            br.close();
            
            value += count;
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
        }finally{
            DEMPostprotocol();
        }
    }
    
    /**
     * Crea el fichero que almacena la cuenta si no existe y lo inicializa a 0.
     */
    @Override
    protected void createFile(){
        FileWriter fw = null;
        File file = new File(fn_totales);
        try{
            if(!file.exists()){
                file.createNewFile();
            }  //Valor inicial de la cuenta: 0
            fw = new FileWriter(file);
            fw.write("0");
            fw.close();
            
        } catch (IOException ex) {
                Logger.getLogger(JobCount.class.getName()).log(Level.SEVERE, null, ex);
                if(fw!=null) try {
                    fw.close();
                } catch (IOException ex1) {
                    Logger.getLogger(JobCount.class.getName()).log(Level.SEVERE, null, ex1);
                }
        }
    }
    
    /**
     * Imprime el valor de totales.txt
     */
    public static void showTotalCount(){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader (new File(fn_totales))); 
            System.out.println("Total contador: "+br.readLine());
            
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JobCount.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JobCount.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(JobCount.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public long getMax_count() {
        return max_count;
    }
    
 

 
    
    
}
