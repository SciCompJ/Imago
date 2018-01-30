/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.image.ColorMap;
import net.sci.image.Image;

/**
 * Choose the colormap of the current scalar image.
 * 
 * @author David Legland
 *
 */
public class ImageSetColorMapAction extends ImagoAction
{
    ColorMap colorMap;
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageSetColorMapAction(ImagoFrame frame, String name, ColorMap colorMap)
	{
		super(frame, name);
		this.colorMap = colorMap;
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
		System.out.println("image set colormap");

		// get current image data
		ImagoDocViewer viewer = (ImagoDocViewer) this.frame;
		ImagoDoc doc = viewer.getDocument();
		Image image	= doc.getImage();

		image.setColorMap(this.colorMap);
		//TODO: notify change ?
		
		viewer.getImageView().refreshDisplay();
		
		viewer.repaint();
	}
}
