/**
 * 
 */
package imago.plugin.edit;

import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.FramePlugin;

/**
 * @author David Legland
 *
 */
public class ZoomOut implements FramePlugin
{
    public ZoomOut()
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
        
        ImageViewer view = iframe.getImageViewer();
        double zoom = view.getZoom();
        zoom = zoom / 2;
        view.setZoom(zoom);
        
        view.invalidate();
        iframe.getWidget().validate();
        iframe.repaint();
    }   
}
