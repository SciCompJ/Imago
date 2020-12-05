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
 * @author David Legland
 *
 */
public class OpenImage implements FramePlugin
{
    private JFileChooser openWindow = null;
    
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
        // create file dialog if it doesn't exist
        if (this.openWindow == null)
        {
            createFileChooserFrame();
        }
        
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
        frame.getGui().createImageFrame(image);
    }
    
    private void createFileChooserFrame()
    {
        this.openWindow = new JFileChooser("."); 
        // openWindow.setFileFilter(fileFilter);
    }
    
}
