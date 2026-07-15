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
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt16;
import net.sci.array.numeric.UInt16Array;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.VectorArray;

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

        // retrieve image data, converting to scalar array of necessary
		Array<?> array = handle.getImage().getData();
		if (array instanceof VectorArray) 
		{
			array = VectorArray.norm((VectorArray<?,?>) array);
		}
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar Array");
		}
		ScalarArray<?> scalarArray = (ScalarArray<?>) array;
		
        double[] extent = switch (scalarArray)
        {
            case UInt8Array a -> new double[] { 0, 255 };
            case UInt16Array a -> new double[] { 0, UInt16.MAX_INT };
            default -> new double[] { 0, 1 };
        };
		System.out.println("  New value range: [" + extent[0] + " ; " + extent[1] + "]");
		
        ImageDataRenderer renderer = iFrame.getImageViewer().getRenderer();
        if (renderer instanceof IndexedColorMapImageRenderer r)
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

