/**
 * 
 */
package imago.image.plugins.edit;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageDataRenderer;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.ImageViewer;
import imago.image.render.BinaryImageRenderer;
import imago.image.render.IndexedColorMapImageRenderer;
import net.sci.array.color.ColorMap;
import net.sci.array.color.ColorMapFactory;
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
		ImageFrame iframe = (ImageFrame) frame;
		ImageHandle handle = iframe.getImageHandle();
		Image image	= handle.getImage();

		// compute number of colors in the colormap, keeping one value for background
		int nColors = 256;
		if (image.isLabelImage())
		{
            nColors = (int) image.getDisplaySettings().getDisplayRange()[1];
		}
		
		// compute a new colormap using the current factory
		ColorMap colorMap = factory.createColorMap(nColors);
		
		ImageViewer viewer = iframe.getImageViewer();
        ImageDataRenderer renderer = viewer.getRenderer();
        switch (renderer)
        {
            case BinaryImageRenderer r -> r.setColorMap(colorMap);
            case IndexedColorMapImageRenderer r -> r.setColorMap(colorMap);
            default -> throw new IllegalArgumentException("Unexpected value: " + renderer);
        }

		// notify changes
        handle.notifyImageHandleChange(ImageHandle.Event.LUT_MASK | ImageHandle.Event.CHANGE_MASK);
	}
}
