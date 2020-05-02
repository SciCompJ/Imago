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
public class ZoomIn implements Plugin
{
    public ZoomIn()
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
        
        ImageViewer view = iframe.getImageView();
        double zoom = view.getZoom();
        zoom = zoom * 2;
        view.setZoom(zoom);
        
        view.invalidate();
        iframe.getWidget().validate();
        iframe.repaint();
    }
    
}
