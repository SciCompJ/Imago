/**
 * 
 */
package imago.plugin.image.edit;

import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
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
		
		ImageViewer imageView = imageViewer.getImageView();
		
		imageView.setDisplaySceneGraph(!imageView.isDisplaySceneGraph());
		
		imageView.refreshDisplay();
		imageView.repaint();
	}

}
