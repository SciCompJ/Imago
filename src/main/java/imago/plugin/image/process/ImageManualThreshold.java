/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.viewer.StackSliceViewer;
import imago.gui.FramePlugin;
import net.sci.array.binary.BinaryArray;
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
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image = doc.getImage();

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
//        System.out.println("Compute initial guess for threshold value...");
//        long t0 = System.nanoTime();
        double[] range = slice.finiteValueRange();
//        long t1 = System.nanoTime();
//        System.out.println("  computation of range: " + (t1 - t0) / 1_000_000.0 + " ms");
        double initValue = new OtsuThreshold().computeThresholdValue(slice, range, 256);
//        long t2 = System.nanoTime();
//        System.out.println("  computation of threshold: " + (t2 - t1) / 1_000_000.0 + " ms");
        
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
        ValueThreshold algo = new ValueThreshold(thresholdValue, upperThreshold);
        algo.addAlgoListener((ImageFrame) frame);
        
        long t0 = System.nanoTime();
        BinaryArray result = algo.processScalar(array);
        long t1 = System.nanoTime();
        
        // display elapsed time
        ((ImageFrame) frame).showElapsedTime("Value Threshold", (t1 - t0) / 1_000_000.0, image);

		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage); 
	}
}
