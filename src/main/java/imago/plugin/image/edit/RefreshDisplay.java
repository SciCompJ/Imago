/**
 * 
 */
package imago.plugin.image.edit;

import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
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
        ImageViewer viewer = ((ImageFrame) frame).getImageView();
        viewer.refreshDisplay();
        frame.repaint();
    }
}
