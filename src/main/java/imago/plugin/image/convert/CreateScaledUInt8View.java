/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.impl.ScalarArrayUInt8View;
import net.sci.image.Image;


/**
 * Create a view to convert a scalar to an UInt8 array, using min and max values
 * for conversion.
 * 
 * @see ConvertImageToUInt8
 * 
 * @author David Legland
 *
 */
public class CreateScaledUInt8View implements FramePlugin
{
	public CreateScaledUInt8View() 
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
		
		if (image == null)
		{
			return;
		}
		Array<?> array = image.getData();
		if (array == null)
		{
			return;
		}
		if (!(array instanceof ScalarArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires a scalar image", "Data Type Error");
			return;
		}

		ScalarArray<?> scalarArray = (ScalarArray<?>) array;
		double[] range = scalarArray.finiteValueRange();
		
		GenericDialog dlg = new GenericDialog(frame, "Create UInt8 View");
        dlg.addNumericField("Min Value: ", range[0], 2);
        dlg.addNumericField("Max Value: ", range[1], 2);
		
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }

        double minValue = dlg.getNextNumber();
        double maxValue = dlg.getNextNumber();
        
		UInt8Array result = new ScalarArrayUInt8View(scalarArray, minValue, maxValue);
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		ImageFrame.create(resultImage, frame);
	}
}
