/**
 * 
 */
package imago.image.plugins.edit;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.image.viewers.PlanarImageViewer;

/**
 * Clears the current selection.
 */
public class ImageClearSelection implements FramePlugin
{
	public ImageClearSelection()
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
                
        ImageViewer viewer = iframe.getImageViewer();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }

		// clear current selection
        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        piv.clearSelection();
        
        // need to call this to update items to display 
        viewer.refreshDisplay(); 
        viewer.repaint();
	}
}
