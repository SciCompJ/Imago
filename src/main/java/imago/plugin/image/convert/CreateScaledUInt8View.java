/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArrayUInt8View;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;


/**
 * Create a view to convert a scalar to an UInt8 array, using min and max values
 * for conversion.
 * 
 * @author David Legland
 *
 */
public class CreateScaledUInt8View implements Plugin
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
		System.out.println("convert to uint8 image");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
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
		frame.getGui().addNewDocument(resultImage); 
	}
}