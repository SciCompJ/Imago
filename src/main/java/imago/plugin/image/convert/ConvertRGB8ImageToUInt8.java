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
import net.sci.array.color.RGB8Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertRGB8ImageToUInt8 implements Plugin 
{
	public ConvertRGB8ImageToUInt8() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("convert rgb8 image to uint8 image");
		
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
		if (!(array instanceof RGB8Array))
		{
		    ImagoGui.showErrorDialog(frame, "Requires a RGB8 color image", "Data Type Error");
			return;
		}

		UInt8Array result = ((RGB8Array) array).convertToUInt8();
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		frame.getGui().addNewDocument(resultImage); 
	}

}
