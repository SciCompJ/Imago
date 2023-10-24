/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.binary.BinaryArray;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;
import net.sci.image.process.segment.HysteresisThreshold;

/**
 * Opens a dialog to choose upper and lower threshold values, and creates a new
 * binary image from the result of hysteresis threshold.
 * 
 * @author David Legland
 *
 */
public class ImageHysteresisThreshold implements FramePlugin
{
	public ImageHysteresisThreshold() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args) 
	{
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image = doc.getImage();

		// requires scalar array
		if (!(image.getData() instanceof ScalarArray))
		{
		    return;
		}
		
		// Extract min/max values
        System.out.println("Compute threshold value guess");
		ScalarArray<?> array = (ScalarArray<?>) image.getData();
		double[] range = array.finiteValueRange();
		double minValue = range[0];
		double ext = range[1] - minValue;
        double upperThresholdValue = minValue + ext * 0.75; 
        double lowerThresholdValue = minValue + ext * 0.25; 
        
		// Creates generic dialog
        GenericDialog gd = new GenericDialog(frame, "Hysteresis Threshold");
        gd.addSlider("Upper Threshold: ", range[0], range[1], upperThresholdValue);
        gd.addSlider("Lower Threshold: ", range[0], range[1], lowerThresholdValue);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        upperThresholdValue = gd.getNextNumber();
        lowerThresholdValue = gd.getNextNumber();
        
        if (upperThresholdValue < lowerThresholdValue) 
        {
            frame.showErrorDialog("Require upper threshold > lower threshold", "Hysteresis Threshold Error");
            return;
        }

        // compute hysteresis threshold
        HysteresisThreshold algo = new HysteresisThreshold(upperThresholdValue, lowerThresholdValue);
        BinaryArray result = algo.processScalar(array);
               
        // create result image
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		ImageFrame.create(resultImage, frame);
	}

}
