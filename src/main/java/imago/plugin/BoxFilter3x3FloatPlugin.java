/**
 * 
 */
package imago.plugin;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
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
		System.out.println("box filter 3x3 float");
		
		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image metaImage = doc.getImage();
		Array<?> array = metaImage.getData();

		// Check array is scalar
		if (!(array instanceof ScalarArray))
		{
		    return;
		}

		// create result image with specified type
		Float32Array output = Float32Array.create(array.size());

		// create operator and apply
		BoxFilter3x3 filter = new BoxFilter3x3();
		if (frame instanceof ImageFrame)
		{
		    filter.addAlgoListener((ImageFrame) frame);
		}
		filter.processScalar((ScalarArray<?>) array, output);
		Image result = new Image(output, metaImage);

		// add the image document to GUI
		frame.getGui().createImageFrame(result);
	}

}
