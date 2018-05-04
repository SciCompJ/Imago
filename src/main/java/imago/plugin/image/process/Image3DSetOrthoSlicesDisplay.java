/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
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
		ImagoDocViewer docViewer = (ImagoDocViewer) frame;
		ImagoDoc doc = docViewer.getDocument();
		Image image	= doc.getImage();

		if (image.getDimension() < 3)
		{
		    return;
		}
		OrthoSlicesViewer viewer = new OrthoSlicesViewer(image); 
		viewer.validate();
		
		docViewer.setImageView(viewer);
        docViewer.validate();
        docViewer.repaint();
	}
}
