/**
 * 
 */
package imago.image.plugins.convert;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt16Array;
import net.sci.array.numeric.UInt8Array;
import net.sci.image.Image;
import net.sci.image.ImageType;


/**
 * Convert a binary of grayscale image into a label image, by using integer
 * values as label values.
 *
 * @see CovnertImageToBinary
 * @see CovnertImageToUInt8
 * 
 * @author David Legland
 *
 */
public class ConvertImageToLabel implements FramePlugin 
{
	public ConvertImageToLabel()
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args) 
	{
		// get current frame
		ImageFrame imageFrame = (ImageFrame) frame;
		
		// retrieve image
		ImageHandle doc = imageFrame.getImageHandle();
		Image image = doc.getImage();
		if (image == null)
		{
			return;
		}
		
		// check array type
		Array<?> array = image.getData();
		
		Array<?> result;
        if (array instanceof UInt8Array || array instanceof UInt16Array)
        {
            result = array; 
        }
        else if (array instanceof ScalarArray)
        {
            result = UInt8Array.wrap(array); 
        }
        else
		{
            ImagoGui.showErrorDialog(frame, "Requires a scalar image", "Data Type Error");
			return;
		}
		
        Image resultImage = new Image(result, ImageType.LABEL, image);
        resultImage.setName(image.getName() + "-lbl");

		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}
	
	@Override
	public boolean isEnabled(ImagoFrame frame)
	{
	    // check frame type
	    if (!(frame instanceof ImageFrame)) return false;

	    // retrieve image
	    Image image = ((ImageFrame) frame).getImageHandle().getImage();
	    if (image == null) return false;

	    // retrieve data
	    Array<?> array = image.getData();
	    return array instanceof ScalarArray;
	}
}
