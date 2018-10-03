/**
 * 
 */
package imago.plugin.image.edit;

import imago.gui.ImageViewer;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;

/**
 * @author dlegland
 *
 */
public class RefreshDisplay implements Plugin
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
		System.out.println("refresh display");
		
		// get current frame
        ImageViewer viewer = ((ImagoDocViewer) frame).getImageView();
        viewer.refreshDisplay();
        frame.repaint();
    }
}
