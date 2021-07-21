/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.image.Image;



/**
 * @author David Legland
 *
 */
public class ImageDuplicate implements FramePlugin
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
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        
        Image result = image.duplicate();
        
        // add the image document to GUI
        frame.createImageFrame(result);
    }
}
