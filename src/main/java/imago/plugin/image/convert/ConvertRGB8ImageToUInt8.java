/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;


/**
 * 
 * @deprecated replaced by ConvertImageToUInt8
 * @author David Legland
 *
 */
@Deprecated
public class ConvertRGB8ImageToUInt8 implements FramePlugin 
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
		if (!(array instanceof RGB8Array))
		{
		    ImagoGui.showErrorDialog(frame, "Requires a RGB8 color image", "Data Type Error");
			return;
		}

		UInt8Array result = ((RGB8Array) array).convertToUInt8();
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage); 
	}

}
