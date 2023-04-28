/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.Float32Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;
import net.sci.image.process.filter.BoxFilter3x3;

/**
 * @author David Legland
 *
 */
public class BoxFilter3x3FloatPlugin implements FramePlugin
{
	public BoxFilter3x3FloatPlugin()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image = doc.getImage();
		Array<?> array = image.getData();

		// Check array is scalar
		if (!(array instanceof ScalarArray))
		{
		    return;
		}

		// create result image with specified type
		Float32Array output = Float32Array.create(array.size());

		// create operator
		BoxFilter3x3 filter = new BoxFilter3x3();
		if (frame instanceof ImageFrame)
		{
		    filter.addAlgoListener((ImageFrame) frame);
		}
        long t0 = System.nanoTime();
		filter.processScalar((ScalarArray<?>) array, output);
        long t1 = System.nanoTime();
        ((ImageFrame) frame).showElapsedTime("BoxFilter3x3", (t1 - t0) / 1_000_000.0, image);
        
        // create output image by combining info from input image and from filter
		Image result = new Image(output, image);
		result.setName(image.getName() + "-boxFilt");

		// add the image document to GUI
		frame.createImageFrame(result);
	}

}
