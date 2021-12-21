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
import net.sci.array.binary.BinaryArray;
import net.sci.array.process.type.ConvertToBinary;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;


/**
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
		System.out.println("convert to binary image");
		
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
		if (array == null)
		{
			return;
		}
		if (!(array instanceof ScalarArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires a scalar image", "Data Type Error");
			return;
		}

		// Create and run operator
		ConvertToBinary algo = new ConvertToBinary();
		algo.addAlgoListener((ImageFrame) frame);
		long t0 = System.nanoTime();
		BinaryArray result = algo.process(array);
        long t1 = System.nanoTime();
        
        // display elapsed time
        imageFrame.showElapsedTime("Convert To Binary", (t1 - t0) / 1_000_000.0, image);
        
		// add the image document to GUI
        Image resultImage = new Image(result, image);
		frame.getGui().createImageFrame(resultImage); 
	}
}
