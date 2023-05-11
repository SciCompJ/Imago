/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import imago.Imago;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.image.Image;

/**
 * Opens a dialog to choose an image file, and opens the selected file in a new
 * image frame.
 * 
 * @author David Legland
 *
 */
public class OpenImage implements FramePlugin
{
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
    public void run(ImagoFrame frame, String args)
    {
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
            ImagoGui.showErrorDialog(frame,
                    "Could not find the selected file: " + file.getName(),
                    "Image I/O Error");
            return;
        }
        
        // Try to read the image
        Image image = Imago.readImage(file, frame);
        if (image == null)
        {
            return;
        }
        
        // add the image document to GUI
        frame.createImageFrame(image);
    }    
}
