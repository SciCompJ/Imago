/**
 * 
 */
package imago.image.plugins.process;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.viewers.OrthoSlicesViewer;
import imago.gui.FramePlugin;
import net.sci.image.Image;

/**
 * Extract a planar slice from a 3D image.
 * 
 * @author David Legland
 *
 */
public class Image3DSetOrthoSlicesDisplay implements FramePlugin
{
	public Image3DSetOrthoSlicesDisplay()
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
		ImageFrame imageFrame = (ImageFrame) frame;
		ImageHandle handle = imageFrame.getImageHandle();
		Image image	= handle.getImage();

		if (image.getDimension() < 3)
		{
		    return;
		}
		OrthoSlicesViewer viewer = new OrthoSlicesViewer(handle); 
		viewer.validate();
		
		imageFrame.setImageViewer(viewer);
        imageFrame.getWidget().validate();
        imageFrame.repaint();
	}
}
