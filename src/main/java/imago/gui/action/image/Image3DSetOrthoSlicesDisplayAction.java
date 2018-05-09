/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.viewer.OrthoSlicesViewer;

import java.awt.event.ActionEvent;

import net.sci.image.Image;

/**
 * Extract a planar slice from a 3D image.
 * 
 * @author David Legland
 *
 */
public class Image3DSetOrthoSlicesDisplayAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Image3DSetOrthoSlicesDisplayAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("set 3D display to orthoslices");

		// get current image data
		ImagoDocViewer docViewer = (ImagoDocViewer) this.frame;
		ImagoDoc doc = docViewer.getDocument();
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
