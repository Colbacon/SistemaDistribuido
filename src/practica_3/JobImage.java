/**
 * JobImage, extensión de Job, representa un trabajo de difuminado de imagen. Se encarga de:
 * -Crear un fichero que será la copia de la imagen original en formato PNG. Sobre esta copia 
 * se va a cargar/guardar el difuminado distribuido que se va realizando durante la ejecución.
 * -Dividir el trabajo del difuminado de una determinada franja de la imagen en N JobImage, 
 * generando N trabajos con sus correspondientes subfranjas. Esta división se realiza sobre 
 * el largo de la imagen.
 * -Realizar el trabajo de difuminado sobre una franja de la imagen a tratar y escritura sobre el 
 * fichero de la imagen.
 * -Mostrar en un Jframe la imagen difuminada.
 */

package practica_3;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Diego Blasco Quetglas
 */
public class JobImage extends Job{
    
    final static String fn_imgIn = "ficheros/imageIn.jpg";
    final static String fn_imgOut = "ficheros/imageOut.png";
    private int xBegin, xEnd;
    
    public JobImage(MessageManager gs, int xBegin, int xEnd){
        super(gs);
        this.xBegin=xBegin;
        this.xEnd=xEnd;
    }
    
    public JobImage(int xBegin, int xEnd){
        this.xBegin=xBegin;
        this.xEnd=xEnd;
    }
    
    /**
     * Divide el el trabajo de difuminado en n_div trabajos de difuminado.
     * @param n_div
     * @return 
     */
    @Override
    public Job[] calcJob(int n_div) {
        
        int i, areaWidth, cutSize;
        JobImage[] jobs;
        
        if(n_div==0) return null;
        
        jobs = new JobImage [n_div];
        i=0; areaWidth=xEnd-xBegin;
        cutSize=areaWidth/n_div;
        while(i<n_div-1){
            jobs[i]=new JobImage(xBegin+i*cutSize, xBegin+(i+1)*cutSize);
            i++;
        }
        //La última división contempla el resto.
        jobs[i]=new JobImage(xBegin+i*cutSize, xBegin+(i+1)*cutSize+areaWidth%n_div);
        
        return jobs;
    }
    
    /**
     * Obtiene la coordenada x inicial y final de la porción a difuminar.
     * Realiza la difuminación haciendo uso de la formula para cada pixel:
     * (4 x i,j + x i+1,j + x i,j+1 + x i-1,j + x i,j-1 )/8
     * 
     * @param job 
     */
    @Override
    public void realizeJob(Job job) {
        JobImage job_image = (JobImage) job;
                
        int x_begin=job_image.getxBegin();
        int x_end=job_image.getxEnd();
        int pixel, width, height;
        BufferedImage img , res;
        
        try {
            img = ImageIO.read(new File(fn_imgIn));
            res = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            width = img.getWidth(); height = img.getHeight();
            
            for (int x = x_begin; x < x_end; x++) {
                for (int y = 0; y < height; y++) {
                    
                    pixel = img.getRGB(x, y)*4;
                    if (x+1 < width) {
                        pixel += img.getRGB(x+1, y);
                    }
                    if (y+1 < height) {
                        pixel += img.getRGB(x, y+1);
                    }
                    if (x-1 > 0) {
                        pixel += img.getRGB(x-1, y);
                    }
                    if (y-1 > 0) {
                        pixel += img.getRGB(x, y-1);
                    }
                    pixel/=8;
                    res.setRGB(x, y, pixel);
                }
            }
            writeOnFile(x_begin, x_end, img, res, fn_imgOut);
        } catch (Exception ex) {
            System.err.print(ex.getMessage());
        }
    }
    
    /**
     * Escribe en el fichero png de la ruta especificada en f_out la porción de la imagen (res) 
     * que ha sido difuminada.
     * @param xBegin
     * @param xEnd
     * @param img
     * @param res
     * @param f_out 
     */
    private void writeOnFile(int xBegin, int xEnd, BufferedImage img, BufferedImage res, String f_out){
        DEMPreprotocol();
        try {
            File resultFile = new File(f_out);
            BufferedImage result = ImageIO.read(resultFile);
            result.setData(res.getData(new java.awt.Rectangle(xBegin, 0, xEnd - xBegin, img.getHeight())));
            ImageIO.write(result, "PNG", resultFile);
            
        } catch (Exception ex) {
            System.err.print(ex.getMessage());
        }finally{
            DEMPostprotocol();
        }
    }
    
    /**
     * Realiza una copia de la imagen original dandole un formato png.
     * Esta copia sera la imagen sobre la que se difuminara.
    */
    @Override
    protected void createFile() {
        try {
            BufferedImage bimg = ImageIO.read(new File(fn_imgIn));
            BufferedImage img = new BufferedImage(bimg.getWidth(), bimg.getHeight(),
            BufferedImage.TYPE_INT_RGB);
            ImageIO.write(img, "PNG", new File(fn_imgOut));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    /**
     * Genera una ventana donde se muestra la imagen difuminada.
     */
    public static void showImgOut(){
        JFrame imgFrame = new JFrame("Imagen difuminada");
        imgFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BufferedImage img = null;
        try {
          img = ImageIO.read(new File(fn_imgOut));
        } catch (Exception ex){
          Logger.getLogger(EnviromentNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        ImageIcon imageIcon = new ImageIcon(img);
        JLabel jLabel = new JLabel();
        jLabel.setIcon(imageIcon);
        imgFrame.getContentPane().add(jLabel, BorderLayout.CENTER);

        //imgFrame.pack();
        imgFrame.setSize(img.getWidth(),img.getHeight());
        imgFrame.setLocationRelativeTo(null);
        imgFrame.setVisible(true);
    }
    
    public int getxBegin() {
        return xBegin;
    }

    public int getxEnd() {
        return xEnd;
    }
   
    
}
