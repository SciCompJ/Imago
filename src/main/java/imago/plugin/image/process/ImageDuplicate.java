/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import net.sci.array.process.shape.Duplicate;
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
        
        // retrieve current image
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        
        // run Duplicate operator on current image
        Duplicate op = new Duplicate();
        Image result = imageFrame.runOperator(op, image);
        
        // add the image document to GUI
        frame.createImageFrame(result);
    }
}
