/**
 * 
 */
package imago.plugin.image.edit;

import java.util.ArrayList;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.color.Color;
import net.sci.array.color.ColorMap;
import net.sci.array.color.ColorMapFactory;
import net.sci.array.color.DefaultColorMap;
import net.sci.image.Image;
import net.sci.image.ImageType;

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
public class ImageSetColorMapFactory implements FramePlugin
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
		// get current image data
		ImageFrame viewer = (ImageFrame) frame;
		ImageHandle handle = viewer.getImageHandle();
		Image image	= handle.getImage();

		ImageType type = image.getType();
		if (type != ImageType.LABEL && type != ImageType.GRAYSCALE && type != ImageType.INTENSITY && type != ImageType.DISTANCE)
		{
		    throw new RuntimeException("Requires a scalar image as input");
		}
		
		int nColors = 256;
		if (image.isLabelImage())
		{
            nColors = (int) image.getDisplaySettings().getDisplayRange()[1];
		}
		
		// Then, when display range  is changed, map can be recomputed
		ColorMap colorMap = factory.createColorMap(nColors);
		
        // in case of label image, add the background color in the beginning of
        // the colormap
        if (image.isLabelImage())
        {
            ArrayList<Color> newColors = new ArrayList<Color>(nColors + 1);
            newColors.add(image.getDisplaySettings().getBackgroundColor());
            for (int i = 0; i < nColors; i++)
            {
                newColors.add(colorMap.getColor(i));
            }
            colorMap = new DefaultColorMap(newColors);
        }
		
		image.getDisplaySettings().setColorMap(colorMap);
		
		// notify changes
        handle.notifyImageHandleChange(ImageHandle.Event.LUT_MASK | ImageHandle.Event.CHANGE_MASK);
	}
}
