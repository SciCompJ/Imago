/**
 * 
 */
package imago.plugin.image.convert;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.process.type.BinaryToUInt8;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertImageToUInt8 implements FramePlugin
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
		ImageFrame imageFrame = (ImageFrame) frame;
		Image image = imageFrame.getImageHandle().getImage();
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

		Image resultImage;
		if (array.dataType() == Binary.class)
		{
//		    BinaryToUInt8 algo = new BinaryToUInt8();
            UInt8Array res = new BinaryToUInt8.View(BinaryArray.wrap(array));
            resultImage = new Image(res, image);
//            resultImage = imageFrame.runOperator(algo, image);
		}
		else
		{
		    UInt8Array result = UInt8Array.convert((ScalarArray<?>) array);
		    resultImage = new Image(result, image);
		}
		resultImage.setName(image.getName() + "-uint8");
		
		// add the image document to GUI
		imageFrame.createImageFrame(resultImage);
	}
}
