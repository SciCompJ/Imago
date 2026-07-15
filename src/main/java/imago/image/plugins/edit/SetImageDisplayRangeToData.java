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
import net.sci.array.numeric.Vector;
import net.sci.array.numeric.VectorArray;

/**
 * Setup the display range of current viewer according to the minimum and
 * maximum values within image data.
 * 
 * 
 * Note that this updates the current viewer, not the image settings.
 * 
 * @author David Legland
 *
 */
public class SetImageDisplayRangeToData implements FramePlugin
{
    /**
     * Default empty constructor
     */
    public SetImageDisplayRangeToData()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageFrame iFrame = (ImageFrame) frame;
        ImageHandle handle = iFrame.getImageHandle();

        // retrieve image data, converting to scalar array of necessary
        Array<?> array = handle.getImage().getData();
        if (Vector.class.isAssignableFrom(array.elementClass()))
        {
            array = VectorArray.norm(VectorArray.wrap((Array<? extends Vector>) array));
        }
        if (!(array instanceof ScalarArray))
        {
            throw new IllegalArgumentException("Requires a scalar Array");
        }
        ScalarArray<?> scalarArray = (ScalarArray<?>) array;

        // Compute min and max values within the array
        double[] extent = scalarArray.finiteValueRange();
        System.out.println("Array value range: [" + extent[0] + " ; " + extent[1] + "]");

        ImageDataRenderer renderer = iFrame.getImageViewer().getRenderer();
        if (renderer instanceof IndexedColorMapImageRenderer r)
        {
            r.setDisplayRange(extent);
        }
        else
        {
            throw new IllegalArgumentException("Unexpected value: " + renderer);
        }

        // refresh display
        handle.notifyImageHandleChange(ImageHandle.Event.DISPLAY_RANGE_MASK | ImageHandle.Event.CHANGE_MASK);
    }
}
