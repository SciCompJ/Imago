/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class OpenDemoImage implements Plugin
{
    String fileName;
    
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
    public void run(ImagoFrame frame, String args)
    {
        // System.out.println("Open demo image");
        
        File file = new File(this.fileName);
        
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
        frame.getGui().addNewDocument(image);
    }
    
}