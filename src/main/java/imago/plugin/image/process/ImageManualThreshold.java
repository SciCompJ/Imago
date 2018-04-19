/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.data.BinaryArray;
import net.sci.array.data.ScalarArray;
import net.sci.image.Image;
import net.sci.image.process.segment.OtsuThreshold;


/**
 * @author David Legland
 *
 */
public class ImageManualThreshold implements Plugin
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
		System.out.println("Otsu Threshold");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image = doc.getImage();

		// requires scalar array
		if (!(image.getData() instanceof ScalarArray))
		{
		    return;
		}
		
		// Extract min/max values
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
        BinaryArray result = BinaryArray.create(array.getSize());
        
        // create array iterators
        ScalarArray.Iterator<?> iter1 = array.iterator(); 
        BinaryArray.Iterator iter2 = result.iterator();
        
        // iterate on both arrays for computing segmented values
        while(iter1.hasNext() && iter2.hasNext())
        {
            if (dark)
                iter2.setNextBoolean(iter1.nextValue() >= threshold);
            else
                iter2.setNextBoolean(iter1.nextValue() <= threshold);
        }

		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		frame.getGui().addNewDocument(resultImage); 
	}

}
