/**
 * 
 */
package imago.image.plugin.convert;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.color.RGB16Array;
import net.sci.array.color.RGB8Array;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertRGB8ImageToRGB16 implements FramePlugin 
{
	public ConvertRGB8ImageToRGB16() 
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
		if (!(array instanceof RGB8Array))
		{
		    ImagoGui.showErrorDialog(frame, "Requires a RGB8 color image", "Data Type Error");
			return;
		}

	      // Create new dialog populated with widgets
        GenericDialog gd = new GenericDialog(frame, "Convert to RGB16");
        gd.addNumericField("Coefficient:", 256, 2, "Multiply each component by the coefficient");
        
        // wait for user validation or cancellation
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        
        // extract user inputs
        double k = gd.getNextNumber();

        int[] samples = new int[3];
		RGB16Array result = RGB16Array.create(array.size());
		for (int[] pos : result.positions())
		{
		    ((RGB8Array) array).getSamples(pos, samples);
		    for (int c = 0; c < 3; c++)
		    {
		        samples[c] *= k;
		    }
		    result.setSamples(pos, samples);
		}
		
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		ImageFrame.create(resultImage, frame);
	}

}
