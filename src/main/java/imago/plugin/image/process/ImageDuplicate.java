/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Image;



/**
 * @author David Legland
 *
 */
public class ImageDuplicate implements Plugin
{
    public ImageDuplicate()
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
        System.out.println("duplicate");
        
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getDocument();
        Image image = doc.getImage();
        
        Image result = image.duplicate();
        
        // add the image document to GUI
        frame.getGui().addNewDocument(result);
    }
}
