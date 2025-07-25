/**
 * 
 */
package imago.plugin.image.file;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.image.Image;
import net.sci.image.io.ImageIOImageReader;

/**
 * Opens an image given a path to the file, or to the resource within the jar.
 * 
 * @author David Legland
 */
public class OpenDemoImage implements FramePlugin
{
    String fileName;
    
    /**
     * Empty constructor, to allow calling the plugin with an options string containing the filename.
     */
    public OpenDemoImage()
    {
    }
    
    public OpenDemoImage(String imageName)
    {
        this.fileName = imageName;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String options)
    {
        // use inner field as default
        String fileName = this.fileName;
        
        // parse option string
        if (options != null && !options.isEmpty())
        {
            // keep first argument, and remove option name
            fileName = options.split(",")[0].split("=")[1].trim();
        }
        File file = new File(fileName);
        
        // First try to read the image from within the jar
        try
        {
            Image image = readImageIO(fileName);
            // add the image document to GUI
            ImageFrame.create(image, frame);
            return;
        }
        catch(Exception ex)
        {
            // could not find within jar, so continue with local file system
        }
        
        // If image could not be found, try with more standard method
        Image image;
        try
        {
            image = Image.readImage(file);
        }
        catch (FileNotFoundException ex)
        {
            // ex.printStackTrace(System.err);
            frame.showErrorDialog("Could not find the file: " + file.getName());
            return;
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
            frame.showErrorDialog(ex.getMessage(), "File Input Error");
            return;
        }
        
        // add the image document to GUI
        ImageFrame.create(image, frame);
    }
    
    private Image readImageIO(String fileName) throws IOException
    {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        if (stream == null)
        {
            throw new IllegalArgumentException("Could not find image file: " + fileName);
        }
        BufferedImage bufImg = ImageIO.read(stream);
        
        // Convert to Image class
        Image image = ImageIOImageReader.convertBufferedImage(bufImg);
        image.setNameFromFileName(fileName);
        image.setFilePath(fileName);

        return image;
    }
}
