/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.binary.DenseBinaryArrayFactory;
import net.sci.array.binary.RunLengthBinaryArrayFactory;
import net.sci.array.binary.process.ScalarToBinary;
import net.sci.array.numeric.IntArray;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.image.Image;
import net.sci.image.segmentation.OtsuThreshold;


/**
 * Opens a dialog to choose a threshold, and creates a new binary image.
 *  
 * @author David Legland
 *
 */
public class ImageManualThreshold implements FramePlugin
{
	public ImageManualThreshold() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args) 
	{
		// get current frame
		ImageFrame imageFrame = (ImageFrame) frame;
		Image image = imageFrame.getImageHandle().getImage();

		// requires scalar array
		if (!(image.getData() instanceof ScalarArray))
		{
		    return;
		}
		ScalarArray<?> array = (ScalarArray<?>) image.getData();
        
		// Extract min/max values
		ScalarArray<?> slice = array;
		if (slice.dimensionality() > 2)
		{
	        int sliceIndex = imageFrame.getImageViewer().getSlicingPosition(2);
	        slice = ScalarArray3D.wrapScalar3d(slice).slice(sliceIndex);
		}
		
		// compute initial threshold value from values in current slice
        double[] range = slice.finiteValueRange();
        double initValue = new OtsuThreshold().computeThresholdValue(slice, range, 256);
        
		// Creates generic dialog
        GenericDialog gd = new GenericDialog(frame, "Choose Threshold");
        // TODO: add widget for histogram representation
        gd.addSlider("Threshold Value: ", range[0], range[1], initValue);
        gd.addCheckBox("Upper values threshold", true);
//        gd.addCheckBox("Create view", false);
        String[] outputTypes = new String[] {"Dense array", "Run-Length array", "View"};
        gd.addChoice("Output Type", outputTypes, outputTypes[1]);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        double thresholdValue = gd.getNextNumber();
        boolean upperThreshold = gd.getNextBoolean();
        int outputTypeIndex = gd.getNextChoiceIndex();
        
        // create operator for threshold computation
        ScalarToBinary algo = new ScalarToBinary(upperThreshold ? x -> x >= thresholdValue : x -> x <= thresholdValue);
        
        Image resultImage;
        if (outputTypeIndex == 2)
        {
            resultImage = new Image(algo.createView(array), image);
        }
        else if (outputTypeIndex == 0)
        {
            algo.setFactory(new DenseBinaryArrayFactory());
            resultImage = imageFrame.runOperator("Manual Threshold", algo, image);
        }
        else if (outputTypeIndex == 1)
        {
            algo.setFactory(new RunLengthBinaryArrayFactory());
            resultImage = imageFrame.runOperator("Manual Threshold", algo, image);
        }
        else
        {
            throw new RuntimeException("Impossible choice...");
        }
        
        // create image name
        String suffix = upperThreshold ? "-ge" : "-le";
        if (array instanceof IntArray)
        {
            suffix = suffix + ((int) thresholdValue);
        }
        else
        {
            suffix = suffix + thresholdValue;
        }
        resultImage.setName(image.getName() + suffix);
        
		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}
}
