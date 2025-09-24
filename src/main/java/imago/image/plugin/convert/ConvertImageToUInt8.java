/**
 * 
 */
package imago.image.plugin.convert;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.process.BinaryToUInt8;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.impl.ScalarArrayUInt8View;
import net.sci.image.Image;
import net.sci.image.ImageType;


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
		// get current frame
		ImageFrame imageFrame = (ImageFrame) frame;
		Image image = imageFrame.getImageHandle().getImage();
		if (image == null)
		{
			return;
		}
		
		// retrieve data
		Array<?> array = image.getData();
		if (array == null)
		{
			return;
		}
		
		// dispatch processing depending on input image data type
		Image resultImage;
		if (array.elementClass() == Binary.class)
		{
		    // Default behavior for binary: create a view
		    UInt8Array result = new BinaryToUInt8.View(BinaryArray.wrap(array));
		    resultImage = new Image(result, image);
		}
		else if (array instanceof ScalarArray)
		{
		    double[] range = image.getDisplaySettings().getDisplayRange();
		    UInt8Array result = new ScalarArrayUInt8View((ScalarArray<?>) array, range[0], range[1]);
		    resultImage = new Image(result, image);
		}
		else if (array instanceof RGB8Array)
		{
		    UInt8Array result = ((RGB8Array) array).createUInt8View();
		    resultImage = new Image(result, image);
		}
		else
		{
		    ImagoGui.showErrorDialog(frame, "Requires a scalar or color image", "Data Type Error");
		    return;
		}
		
		// force type and display range of result image
		resultImage.setType(ImageType.GRAYSCALE);
		resultImage.getDisplaySettings().setDisplayRange(new double[] {0, 255});
		
		// add the image document to GUI
        resultImage.setName(image.getName() + "-uint8");
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
        return array instanceof ScalarArray || array instanceof RGB8Array;
    }
}
