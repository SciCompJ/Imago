/**
 * 
 */
package imago.plugin.edit;

import imago.gui.ImageViewer;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;

/**
 * @author David Legland
 *
 */
public class ZoomOne implements Plugin
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
        
        ImageViewer display = iframe.getImageView();
        display.setZoom(1);
        
        display.invalidate();
        iframe.getWidget().validate();
        iframe.repaint();
    }
    
}
