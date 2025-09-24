/**
 * 
 */
package imago.image.plugin.edit;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.gui.FramePlugin;

/**
 * @author dlegland
 *
 */
public class RefreshDisplay implements FramePlugin
{
	public RefreshDisplay() 
	{
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current frame
        ImageViewer viewer = ((ImageFrame) frame).getImageViewer();
        viewer.refreshDisplay();
        frame.repaint();
    }
}
