/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertImageToUInt8 implements Plugin
{
	public ConvertImageToUInt8() 
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
		ImageHandle doc = ((ImageFrame) frame).getDocument();
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

		
		UInt8Array result = UInt8Array.convert((ScalarArray<?>) array);
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		frame.getGui().addNewDocument(resultImage); 
	}
}
