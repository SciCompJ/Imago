/**
 * 
 */
package imago.image.plugins.process;

import java.util.prefs.Preferences;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.algo.AlgoEvent;
import net.sci.array.Array;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.image.Image;
import net.sci.image.filtering.Convolution1D;

/**
 * Applies Gradient filter on a multidimensional image, by opening a dialog to
 * choose relevant options.
 * 
 * @see ImageBoxFilter
 * 
 * @author David Legland
 */
public class ImageAxisGradientFilter implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageAxisGradientFilter()
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
        
        int nd = array.dimensionality();

        // create a dialog for the user to choose options
        GenericDialog gd = new GenericDialog(frame, "Gradient Filter");
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        gd.addIntegerField("Axis", 1);
        double sigmaRef = prefs.getDouble("ImageGradientFilterSigma", 2.0);
        gd.addNumericField("Sigma", sigmaRef, 1);
        gd.addEnumChoice("Output Type", ScalarOutputTypes.class, ScalarOutputTypes.SAME_AS_INPUT);

        // wait the user to choose
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        int axis = gd.getNextInteger();
        double sigma = gd.getNextNumber();
        ScalarArray.Factory<?> factory = ((ScalarOutputTypes) gd.getNextEnumChoice()).factory();

        // keep preferences for future calls to plugin
        for (int d = 0; d < nd; d++)
        {
            prefs.putDouble("ImageGradientFilterSigma", sigma);
        }

        // create box filtering operator from user settings
        int radius = (int) Math.ceil(sigma * 2);
        double[][] kernels = computeKernels(2 * radius + 1, sigma);
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ScalarArray<?> res = ScalarArray.wrap((Array<Scalar>) array);
        for(int d = 0; d < array.dimensionality(); d++)
        {
            String msg = String.format("Process axis %d/%d", (d + 1), nd);
            imageFrame.algoStatusChanged(new AlgoEvent(this, msg));
            
            double[] kernel = d == (axis - 1) ? kernels[1] : kernels[0];
            Convolution1D op = new Convolution1D(d, kernel);
            op.setFactory(factory);
            op.addAlgoListener(imageFrame);
            
            res = op.processScalar(res);
            
            op.removeAlgoListener(imageFrame);
        }
        
        // apply operator on current image
        Image result = new Image(res, image);
        result.setName(image.getName() + "-gradFilt");

        // add the image document to GUI
        ImageFrame.create(result, frame);
    }
    
    private double[][] computeKernels(int radius, double sigma)
    {
        // initialize smoothing and derivative kernel
        int diameter = 2 * radius + 1;
        double[] ks = new double[diameter];
        double[] kd = new double[diameter];
        
        // compute values
        double sumKs = 0;
        double sumKd = 0;
        for (int i = 0; i < diameter; i++)
        {
            double x = i - radius - 1;
            double kx = Math.exp(-(x * x) / (2 * sigma * sigma));
            ks[i] = kx;
            sumKs += ks[i];
            kd[i] = -kx * x / sigma;
            if (kd[i] > 0) sumKd += kd[i];
        }
        
        // normalization
        for (int i = 0; i < diameter; i++)
        {
            ks[i] /= sumKs;
            kd[i] /= sumKd;
        }
        
        return new double[][] {ks, kd};
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
