/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.viewer.StackSliceViewer;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.image.Image;
import net.sci.image.process.segment.OtsuThreshold;
import net.sci.image.process.segment.ValueThreshold;


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
		System.out.println("Manual Threshold");
		
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
	        // get slice index
	        StackSliceViewer viewer3d = (StackSliceViewer) ((ImageFrame) frame).getImageView();
	        // TODO: have some "Image3DViewer" interface
	        int sliceIndex = viewer3d.getSliceIndex();
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
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        double thresholdValue = gd.getNextNumber();
        boolean upperThreshold = gd.getNextBoolean();
        
        // compute threshold
//        ScalarToBinary algo = new ScalarToBinary(upperThreshold ? x -> x >= thresholdValue : x -> x <= thresholdValue);
        ValueThreshold algo = new ValueThreshold(thresholdValue, upperThreshold);
        Image resultImage = imageFrame.runOperator(algo, image);
        resultImage.setName(image.getName() + "-bin");
        
		// add the image document to GUI
		imageFrame.createImageFrame(resultImage); 
	}
}
