/**
 * 
 */
package imago.image.plugins.file;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

import imago.Imago;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import net.sci.image.Image;
import net.sci.image.io.ImageIOImageReader;

/**
 * Opens a dialog to choose an image file, and opens the selected file in a new
 * image frame.
 * 
 * @author David Legland
 *
 */
public class OpenImage implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public OpenImage()
    {
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
        // If options is given, use it to choose the file
        if (options != null && !options.isEmpty())
        {
            String filePath = FramePlugin.parseOptionsString(options).get("fileName");
            if (filePath != null)
            {
                Image image = readImage(frame, filePath);
                if (image == null) return;

                // add the image document to GUI
                ImageFrame.create(image, frame);
                return;
            }
        }

        // opens a dialog to choose the file
        FileFilter[] filters = new FileFilter[] {
                ImageFileFilters.COMMON, 
                ImageFileFilters.BMP, 
                ImageFileFilters.GIF, 
                ImageFileFilters.JPEG, 
                ImageFileFilters.META_IMAGE, 
                ImageFileFilters.PNG, 
                ImageFileFilters.TIFF, 
        };
        
        File file = frame.getGui().chooseFileToOpen(frame, "Open Image", filters);
        if (file == null)
        {
            return;
        }
        
        // Check the chosen file is valid
        if (!file.isFile())
        {
            ImagoGui.showErrorDialog(frame, "Could not find the selected file: " + file.getName(),
                    "Image I/O Error");
            return;
        }
        
        // Try to read the image
        Image image = Imago.readImage(file, frame);
        if (image == null)
        {
            return;
        }
        
        // setup metadata
        String fileName = file.getName();
        image.setNameFromFileName(fileName);
        
        // add the image document to GUI
        ImageFrame.create(image, frame);
    }
    
    private Image readImage(ImagoFrame frame, String filePath)
    {
        // First try to read the image from within the jar
        try
        {
            return readResource(filePath);
        }
        catch(Exception ex)
        {
            // could not find within jar, so continue with local file system
        }
        
        // If image could not be found, try with more standard method
        try 
        {
            return Image.readImage(new File(filePath));
        }
        catch (FileNotFoundException ex)
        {
            // ex.printStackTrace(System.err);
            frame.showErrorDialog("Could not find the file: " + filePath);
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
            frame.showErrorDialog(ex.getMessage(), "File Input Error");
        }
        
        return null;
    }
    
    private Image readResource(String path) throws IOException
    {
        // open a stream to the resource
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path);
        if (stream == null)
        {
            throw new IllegalArgumentException("Could not find resource with path: " + path);
        }
        
        // read AWT image from input stream
        BufferedImage bufImg = ImageIO.read(stream);
        
        // Convert to Image class
        Image image = ImageIOImageReader.convertBufferedImage(bufImg);
        
        // setup metadata
        String fileName = Path.of(path).getFileName().toString();
        image.setNameFromFileName(fileName);

        return image;
    }
}
