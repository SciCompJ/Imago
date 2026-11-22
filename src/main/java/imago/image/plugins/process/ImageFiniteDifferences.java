/**
 * 
 */
package imago.image.plugins.process;

import java.util.prefs.Preferences;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.color.ColorMaps;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.process.FiniteDifferences;
import net.sci.image.Image;

/**
 * Computes finite differences of a values within a multidimensional image.
 * Opens a dialog to choose the processing direction, and the type of output
 * image data.
 * 
 * @see ImageBoxFilter
 * 
 * @author David Legland
 */
public class ImageFiniteDifferences implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageFiniteDifferences()
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
        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        if (!array.elementInstanceOf(Scalar.class)) return;
        
        // create a dialog for the user to choose options
        GenericDialog gd = new GenericDialog(frame, "Finite Differences");
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        int axis = prefs.getInt("ImageFiniteDifferencesAxis", 1);
        gd.addIntegerField("Axis (1-indexed)", axis);
        gd.addEnumChoice("Output Type", ScalarOutputTypes.class, ScalarOutputTypes.SAME_AS_INPUT);

        // wait the user to choose
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        axis = gd.getNextInteger();
        ScalarArray.Factory<?> factory = ((ScalarOutputTypes) gd.getNextEnumChoice()).factory();
        prefs.putInt("ImageFiniteDifferencesAxis", axis);

        // create operator
        FiniteDifferences op = new FiniteDifferences(axis - 1);
        op.setFactory(factory);
        
        // apply operator on current image
        Image result = imageFrame.runOperator(op, image);

        double[] range = image.getDisplaySettings().getDisplayRange();
        double extent = range[1] - range[0];
        result.getDisplaySettings().setDisplayRange(new double[] {-extent, extent});
        result.getDisplaySettings().setColorMap(ColorMaps.BLUE_WHITE_RED.createColorMap(256));
        result.setName(image.getName() + "-diff" + axis);

        // add the image document to GUI
        ImageFrame.create(result, frame);
    }
    
    /**
     * Returns true if the current frame contains a scalar image.
     * 
     * @param frame
     *            the frame containing reference to this plugin
     * @return true if the frame contains a scalar image.
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        if (frame instanceof ImageFrame imageFrame)
        {
            Image image = imageFrame.getImageHandle().getImage();
            if (image == null) return false;

            return image.isScalarImage();
        }
        
        return false;
    }
}
