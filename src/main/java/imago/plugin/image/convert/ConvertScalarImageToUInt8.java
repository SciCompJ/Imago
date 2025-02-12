/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.process.BinaryToUInt8;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array;
import net.sci.image.Image;


/**
 * Convert a scalar image to UInt8 data using min and max display values
 * 
 * @deprecated replaced by ConvertImagToUInt8
 * 
 * @author David Legland
 *
 */
@Deprecated
public class ConvertScalarImageToUInt8 implements FramePlugin 
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
		    ImagoGui.showErrorDialog(frame, "Requires a Scalar image", "Data Type Error");
			return;
		}
		
		Image resultImage;
		if (array.elementClass() == Binary.class)
		{
		    UInt8Array res = new BinaryToUInt8.View(BinaryArray.wrap(array));
		    resultImage = new Image(res, image);
		}
		else
		{
		    // extract range, and convert to UInt8
		    double[] range = image.getDisplaySettings().getDisplayRange();
		    UInt8Array result = UInt8Array.convert((ScalarArray<?>) array, range[0], range[1]);

		    // create image
		    resultImage = new Image(result, image);
		}
		resultImage.setName(image.getName() + "-uint8");
		
		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}
}
