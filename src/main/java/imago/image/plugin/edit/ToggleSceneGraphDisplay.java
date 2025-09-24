/**
 * 
 */
package imago.image.plugin.edit;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.gui.FramePlugin;

/**
 * Toggles the display of scene graph items in the current image viewer.
 * 
 * @author David Legland
 *
 */
public class ToggleSceneGraphDisplay implements FramePlugin
{
	public ToggleSceneGraphDisplay()
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
		// get current image data
		ImageFrame imageViewer = (ImageFrame) frame;
		
		ImageViewer imageView = imageViewer.getImageViewer();
		
		imageView.setDisplaySceneGraph(!imageView.isDisplaySceneGraph());
		
		imageView.refreshDisplay();
		imageView.repaint();
	}

}
