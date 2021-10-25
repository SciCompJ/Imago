/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.binary.BinaryArray;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;
import net.sci.image.process.segment.OtsuThreshold;


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
		
		// Extract min/max values
        System.out.println("Compute threshold value guess");
		ScalarArray<?> array = (ScalarArray<?>) image.getData();
		double[] range = array.finiteValueRange();
		double initValue = new OtsuThreshold().computeThresholdValue(array);
        
		// Creates generic dialog
        GenericDialog gd = new GenericDialog(frame, "Choose Threshold");
        // TODO: add histogram representation
        gd.addSlider("Threshold Value: ", range[0], range[1], initValue);
        gd.addCheckBox("Dark Background", true);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        double threshold = gd.getNextNumber();
        boolean dark = gd.getNextBoolean();

        // create output array
        BinaryArray result = BinaryArray.create(array.size());
               
        // iterate on array positions for computing segmented values
        for (int[] pos : result.positions())
        {
            if (dark)
                result.setBoolean(pos, array.getValue(pos) >= threshold);
            else
                result.setBoolean(pos, array.getValue(pos) <= threshold);
        }

		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage); 
	}

}
