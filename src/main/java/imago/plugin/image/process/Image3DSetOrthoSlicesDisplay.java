/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.OrthoSlicesViewer;
import net.sci.image.Image;

/**
 * Extract a planar slice from a 3D image.
 * 
 * @author David Legland
 *
 */
public class Image3DSetOrthoSlicesDisplay implements Plugin
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
		System.out.println("set 3D display to orthoslices");

		// get current image data
		ImageFrame docViewer = (ImageFrame) frame;
		ImageHandle doc = docViewer.getImageHandle();
		Image image	= doc.getImage();

		if (image.getDimension() < 3)
		{
		    return;
		}
		OrthoSlicesViewer viewer = new OrthoSlicesViewer(image); 
		viewer.validate();
		
		docViewer.setImageView(viewer);
        docViewer.getWidget().validate();
        docViewer.repaint();
	}
}
