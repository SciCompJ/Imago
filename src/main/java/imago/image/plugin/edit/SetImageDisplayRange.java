/**
 * 
 */
package imago.image.plugin.edit;

import java.util.Locale;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.Array3D;
import net.sci.array.color.RGB16;
import net.sci.array.color.RGB16Array;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.Vector;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;

/**
 * Opens a dialog to choose the display range of values for current image.
 * 
 * @author David Legland
 *
 */
public class SetImageDisplayRange implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public SetImageDisplayRange()
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
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();

        Array<?> array = image.getData();

        // Compute min and max values within the array
        // or within current slice in the case of 3D array
        if (array.dimensionality() > 2)
        {
            int sliceIndex = ((ImageFrame) frame).getImageViewer().getSlicingPosition(2);
            array = Array3D.wrap(array).slice(sliceIndex);
        }
        double[] extent = computeValueExtent(array);

        double[] displayRange = image.getDisplaySettings().getDisplayRange();

        // Create new dialog populated with widgets
        GenericDialog gd = new GenericDialog(frame, "Set Display Range");
        String labelMin = String.format(Locale.ENGLISH, "Min value (%6.2f) ", extent[0]);
        gd.addNumericField(labelMin, displayRange[0], 3, "Minimal value to display as black");
        String labelMax = String.format(Locale.ENGLISH, "Max value (%6.2f) ", extent[1]);
        gd.addNumericField(labelMax, displayRange[1], 3, "Maximal value to display as white");

        // wait for user validation or cancellation
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        // extract user inputs
        double minRange = gd.getNextNumber();
        double maxRange = gd.getNextNumber();
        extent = new double[] { minRange, maxRange };

        // update display settings
        image.getDisplaySettings().setDisplayRange(extent);

        // refresh display
        ImageViewer viewer = ((ImageFrame) frame).getImageViewer();
        viewer.refreshDisplay();
        viewer.repaint();
    }

    private double[] computeValueExtent(Array<?> array)
    {
        Class<?> elementClass = array.elementClass();
        if (Scalar.class.isAssignableFrom(elementClass))
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            ScalarArray scalarArray = ScalarArray.wrap((Array<Scalar>) array);
            return scalarArray.finiteValueRange();
        }
        else if (RGB8.class.isAssignableFrom(elementClass))
        {
            @SuppressWarnings({ "unchecked" })
            RGB8Array rgb8Array = RGB8Array.wrap((Array<RGB8>) array);
            return computeValueExtent_RGB8(rgb8Array);
        }
        else if (RGB16.class.isAssignableFrom(elementClass))
        {
            @SuppressWarnings({ "unchecked" })
            RGB16Array rgb16Array = RGB16Array.wrap((Array<RGB16>) array);
            return computeValueExtent_RGB16(rgb16Array);
        }
        else if (array instanceof VectorArray)
        {
            return computeValueExtent_Vector((VectorArray<?, ?>) array);
        }
        else
        {
            throw new IllegalArgumentException(
                    "Unable to process array with class: " + array.getClass());
        }
    }

    private double[] computeValueExtent_RGB8(RGB8Array array)
    {
        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;

        int[] samples = new int[3];
        for (int[] pos : array.positions())
        {
            array.getSamples(pos, samples);
            for (int v : samples)
            {
                minValue = Math.min(minValue, v);
                maxValue = Math.max(maxValue, v);
            }
        }
        return new double[] { minValue, maxValue };
    }

    private double[] computeValueExtent_RGB16(RGB16Array array)
    {
        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;

        int[] samples = new int[3];
        for (int[] pos : array.positions())
        {
            array.getSamples(pos, samples);
            for (int v : samples)
            {
                minValue = Math.min(minValue, v);
                maxValue = Math.max(maxValue, v);
            }
        }
        return new double[] { minValue, maxValue };
    }

    private double[] computeValueExtent_Vector(VectorArray<?, ?> array)
    {
        int nc = array.channelCount();
        double[] values = new double[nc];
        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;

        for (int[] pos : array.positions())
        {
            Vector.norm(array.getValues(pos, values));
            double v = Vector.norm(values);
            minValue = Math.min(minValue, v);
            maxValue = Math.max(maxValue, v);
        }
        return new double[] { minValue, maxValue };
    }
}
