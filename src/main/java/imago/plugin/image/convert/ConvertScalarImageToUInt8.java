/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;


/**
 * Convert a scalar image to UInt8 data using min and max display values
 * 
 * @author David Legland
 *
 */
public class ConvertScalarImageToUInt8 implements Plugin 
{
	public ConvertScalarImageToUInt8() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("convert scalar image to uint8 image");
		
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
		    ImagoGui.showErrorDialog(frame, "Requires a Scalar image", "Data Type Error");
			return;
		}
		
		// cast to scalar array
		ScalarArray<?> scalarArray = (ScalarArray<?>) array;
		
		// compute ratio 
		double[] range = image.getDisplayRange();
		double ratio = 255 / (range[1] - range[0]);
		
		UInt8Array result = UInt8Array.convert(scalarArray.minus(range[0]).times(ratio));
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		frame.getGui().addNewDocument(resultImage); 
	}

}
