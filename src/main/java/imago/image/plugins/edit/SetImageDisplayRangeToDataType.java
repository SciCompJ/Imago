/**
 * 
 */
package imago.image.plugins.edit;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageDataRenderer;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.render.IndexedColorMapImageRenderer;
import imago.image.render.RGBArrayRenderer;
import net.sci.array.Array;
import net.sci.array.color.RGB16;
import net.sci.array.color.RGB8;
import net.sci.array.numeric.UInt16;
import net.sci.array.numeric.UInt8;

/**
 * Setup the display range of current viewer according to image data type.
 * 
 * Note that this updates the current viewer, not the image settings.
 * 
 * @author David Legland
 *
 */
public class SetImageDisplayRangeToDataType implements FramePlugin
{
    /**
     * Default empty constructor
     */
    public SetImageDisplayRangeToDataType()
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
        // get current frame
        ImageFrame iFrame = (ImageFrame) frame;
        ImageHandle handle = iFrame.getImageHandle();

        // retrieve image data
		Array<?> array = handle.getImage().getData();
		
        double[] extent = switch (array.sampleElement())
        {
            case UInt8 a -> new double[] { 0, 255 };
            case UInt16 a -> new double[] { 0, UInt16.MAX_INT };
            case RGB8 a -> new double[] { 0, 255 };
            case RGB16 a -> new double[] { 0, UInt16.MAX_INT };
            default -> new double[] { 0, 1 };
        };
		System.out.println("  New value range: [" + extent[0] + " ; " + extent[1] + "]");
		
        ImageDataRenderer renderer = iFrame.getImageViewer().getRenderer();
        if (renderer instanceof IndexedColorMapImageRenderer r)
        {
            r.setDisplayRange(extent);
        }
        else if (renderer instanceof RGBArrayRenderer r)
        {
            r.setDisplayRange(extent);
        }
        else
        {
            throw new IllegalArgumentException("Unexpected value: " + renderer);
        }
		
        // notify associated viewers
        handle.notifyImageHandleChange(ImageHandle.Event.DISPLAY_RANGE_MASK | ImageHandle.Event.CHANGE_MASK);
	}
}

