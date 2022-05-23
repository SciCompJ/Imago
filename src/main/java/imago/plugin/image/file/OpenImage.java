/**
 * 
 */
package imago.plugin.image.file;

import imago.Imago;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.FramePlugin;

import java.io.File;

import javax.swing.JFileChooser;

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
        // create new file dialog
        JFileChooser openWindow = frame.getGui().createOpenFileDialog("Open Image");
        
        // Open dialog to choose the file
        int ret = openWindow.showOpenDialog(frame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        
        // Check the chosen file is state
        File file = openWindow.getSelectedFile();
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
