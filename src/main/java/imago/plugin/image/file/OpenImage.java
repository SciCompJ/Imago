/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class OpenImage implements Plugin
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
            return;
        }
        
        Image image;
        try
        {
            image = Image.readImage(file);
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
            // custom title, error icon
            JOptionPane.showMessageDialog(frame.getWidget(), "Could not read the image.",
                    "Image I/O Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(frame.getWidget(), "Could not read the image.",
                    "Image I/O Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(System.err);
            return;
        }
        
        image.setName(file.getName());
        
        // add the image document to GUI
        frame.getGui().addNewDocument(image);
    }
    
    private void createFileChooserFrame()
    {
        this.openWindow = new JFileChooser("."); 
        // openWindow.setFileFilter(fileFilter);
    }
    
}
