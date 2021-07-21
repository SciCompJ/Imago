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
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;


/**
 * Convert a scalar image to UInt8 data using min and max display values
 * 
 * @author David Legland
 *
 */
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
		System.out.println("convert scalar image to uint8 image");
		
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
		
		// extract range, and convert to UInt8
		double[] range = image.getDisplaySettings().getDisplayRange();
		UInt8Array result = processScalar((ScalarArray<?>) array, range);
		
		// create image
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage); 
	}
	
	public UInt8Array processScalar(ScalarArray<?> array, double[] range)
	{
        // compute ratio 
	    double ratio = 255 / (range[1] - range[0]);
	    
	    // remove min, rescale and convert type
        return UInt8Array.convert(array.minus(range[0]).times(ratio));
	}
}
