/**
 * 
 */
package imago.plugin.image.edit;

import java.util.ArrayList;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.type.Color;
import net.sci.image.ColorMap;
import net.sci.image.ColorMapFactory;
import net.sci.image.DefaultColorMap;
import net.sci.image.Image;

/**
 * Choose the colormap of the current scalar image.
 * 
 * This class keeps an instance of ColorMapFactory, that will create the
 * appropriate color map for the current image. The number of colors will be
 * chosen according to the image type.
 * 
 * @author David Legland
 * @see ImageSetColorMap
 */
public class ImageSetColorMapFactory implements Plugin
{
    ColorMapFactory factory;
    
	public ImageSetColorMapFactory(ColorMapFactory factory)
	{
		this.factory = factory;
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
		System.out.println("image set colormap");

		// get current image data
		ImagoDocViewer viewer = (ImagoDocViewer) frame;
		ImagoDoc doc = viewer.getDocument();
		Image image	= doc.getImage();

		Image.Type type = image.getType();
		if (type != Image.Type.LABEL && type != Image.Type.GRAYSCALE && type != Image.Type.INTENSITY)
		{
		    throw new RuntimeException("Requires a scalar image as input");
		}
		
		int nColors = 256;
		if (image.isLabelImage())
		{
            nColors = (int) image.getDisplayRange()[1];
		}
		
		System.out.println("number of colors for colormap: " + nColors);
		
		// TODO: factory should be stored within Image instance
		// Then, when display range  is changed, map can be recomputed
		ColorMap colorMap = factory.createColorMap(nColors);
		
        // in case of label image, add the background color in the beginning of
        // the colormap
        if (image.isLabelImage())
        {
            ArrayList<Color> newColors = new ArrayList<Color>(nColors + 1);
            newColors.add(image.getBackgroundColor());
            for (int i = 0; i < nColors; i++)
            {
                newColors.add(colorMap.getColor(i));
            }
            colorMap = new DefaultColorMap(newColors);
        }
		
		image.setColorMap(colorMap);
		//TODO: notify change ?
		
		viewer.getImageView().refreshDisplay();
		
		viewer.repaint();
	}
}