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
import net.sci.array.numeric.Scalar;
import net.sci.image.Image;
import net.sci.image.filtering.GradientFilter;

/**
 * Applies Gradient filter on a multidimensional image, by opening a dialog to
 * choose relevant options.
 * 
 * @see ImageBoxFilter
 * 
 * @author David Legland
 */
public class ImageGradientFilter implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageGradientFilter()
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
        GenericDialog gd = new GenericDialog(frame, "Gradient Filter");
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        double sigmaRef = prefs.getDouble("ImageGradientFilterSigma", 2.0);
        gd.addNumericField("Sigma", sigmaRef, 1);

        // wait the user to choose
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        double sigma = gd.getNextNumber();
        prefs.putDouble("ImageGradientFilterSigma", sigma);
        GradientFilter op = new GradientFilter(sigma);
        
        // apply operator on current image
        Image result = imageFrame.runOperator(op, image);
        result.setName(image.getName() + "-gradFilt");

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
