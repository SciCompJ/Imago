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
import net.sci.image.Image;
import net.sci.image.filtering.GaussianFilter;

/**
 * Applies Gaussian filter on a multidimensional image, by opening a dialog to
 * choose relevant options.
 * 
 * @see ImageBoxFilter
 * 
 * @author David Legland
 */
public class ImageGaussianFilter implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageGaussianFilter()
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

        int nd = array.dimensionality();

        // create a dialog for the user to choose options
        GenericDialog gd = new GenericDialog(frame, "Box Filter");
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        for (int d = 0; d < nd; d++)
        {
            double size = prefs.getDouble("ImageGaussianFilterSigma" + (d + 1), 2.0);
            gd.addNumericField("Sigma_" + (d + 1), size, 1);
        }
//        gd.addEnumChoice("Output Type", ScalarOutputTypes.class, ScalarOutputTypes.SAME_AS_INPUT);

        // wait the user to choose
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        double[] sigmas = new double[nd];
        for (int d = 0; d < nd; d++)
        {
            sigmas[d] = gd.getNextNumber();
        }
//        ScalarArray.Factory<?> factory = ((ScalarOutputTypes) gd.getNextEnumChoice()).factory();

        // keep preferences for future calls to plugin
        for (int d = 0; d < nd; d++)
        {
            prefs.putDouble("ImageGaussianFilterSigma" + (d + 1), sigmas[d]);
        }

        // create box filtering operator from user settings
        GaussianFilter filter = new GaussianFilter(sigmas);
//        filter.setFactory(factory);

        // apply operator on current image
        Image result = imageFrame.runOperator(filter, image);
        result.setName(image.getName() + "-gaussFilt");

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
