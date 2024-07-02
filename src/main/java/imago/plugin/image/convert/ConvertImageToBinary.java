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
import net.sci.array.numeric.ScalarArray;
import net.sci.array.process.type.ConvertToBinary;
import net.sci.image.Image;


/**
 * Convert a scalar image (grayscale, intensity) into a binary image, by setting
 * all values greater than zero to true.
 *
 * @see net.sci.array.process.type.ConvertToBinary
 * 
 * @author David Legland
 *
 */
public class ConvertImageToBinary implements FramePlugin 
{
	public ConvertImageToBinary()
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
        if (image == null) return;
		
        // check array type
        Array<?> array = image.getData();
        if (array == null) return;
        
        if (!(array instanceof ScalarArray))
        {
            ImagoGui.showErrorDialog(frame, "Requires a scalar image", "Data Type Error");
            return;
        }

        // create and run algo
        ConvertToBinary algo = new ConvertToBinary();
        Image resultImage = imageFrame.runOperator(algo, image);
        resultImage.setName(image.getName() + "-bin");

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
