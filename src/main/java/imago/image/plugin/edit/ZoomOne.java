/**
 * 
 */
package imago.image.plugin.edit;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.gui.FramePlugin;

/**
 * @author David Legland
 *
 */
public class ZoomOne implements FramePlugin
{
    public ZoomOne()
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
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        
        ImageViewer display = iframe.getImageViewer();
        display.setZoom(1);
        
        display.invalidate();
        iframe.getWidget().validate();
        iframe.repaint();
    }
    
}
