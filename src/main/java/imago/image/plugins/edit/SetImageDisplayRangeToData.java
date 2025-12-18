/**
 * 
 */
package imago.image.plugins.edit;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.ImageViewer;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.Vector;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;

/**
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
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();

        Array<?> array = image.getData();
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

        image.getDisplaySettings().setDisplayRange(extent);

        // refresh display
        ImageViewer viewer = ((ImageFrame) frame).getImageViewer();
        viewer.refreshDisplay();
    }
}
