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
import net.sci.image.ColorMapFactory;
import net.sci.image.Image;

/**
 * Choose the colormap of the current scalar image.
 * 
 * This class keeps an instance of ColorMapFactory, that will create the
 * appropriate color map for the current image. The number of colors will be
 * chosen according to the image type.
 * 
 * @author David Legland
 * @see ImageSetColorMapAction
 */
public class ImageSetColorMapFactoryAction extends ImagoAction
{
    ColorMapFactory factory;
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageSetColorMapFactoryAction(ImagoFrame frame, String name, ColorMapFactory factory)
	{
		super(frame, name);
		this.factory = factory;
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

		if (image.getType() != Image.Type.LABEL && image.getType() != Image.Type.GRAYSCALE && image.getType() != Image.Type.INTENSITY)
		{
		    throw new RuntimeException("Requires a scalar image as input");
		}
		
		int nColors = 256;
		if (image.isLabelImage())
		{
            nColors = (int) image.getDisplayRange()[1];
		}
		
		ColorMap colorMap = factory.createColorMap(nColors);
		image.setColorMap(colorMap);
		//TODO: notify change ?
		
		viewer.getImageView().refreshDisplay();
		
		viewer.repaint();
	}
}
