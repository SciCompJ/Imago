/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt8Array;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;

/**
 * @see CreateVectorImageRGB8View
 * 
 * @author David Legland
 *
 */
public class VectorImageConvertToRGB implements FramePlugin
{
	public VectorImageConvertToRGB()
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
        if (!(array instanceof VectorArray))
        {
            ImagoGui.showErrorDialog(frame, "Requires a Vector image", "Data Type Error");
            return;
        }

        VectorArray<?> vectorArray = (VectorArray<?>) array;
        int nChannels = vectorArray.channelNumber();
        
        // Create dialog for choosing channel indices
        GenericDialog dlg = new GenericDialog(frame, "Extract Channel");
        dlg.addNumericField("Red Channel index", 0, 0, String.format("Between 0 and %d", nChannels));
        dlg.addNumericField("Green Channel index", 0, 0, String.format("Between 0 and %d", nChannels));
        dlg.addNumericField("Blue Channel index", 0, 0, String.format("Between 0 and %d", nChannels));
        
        // Display dialog and wait for OK or Cancel
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // extract user choices
        int redChannelIndex = (int) dlg.getNextNumber();
        if (redChannelIndex < 0 || redChannelIndex >= nChannels)
        {
            ImagoGui.showErrorDialog(frame, "Red Channel index must be comprised between 0 and " + (nChannels-1), "Input Error");
            return;
        }
        int greenChannelIndex = (int) dlg.getNextNumber();
        if (greenChannelIndex < 0 || greenChannelIndex >= nChannels)
        {
            ImagoGui.showErrorDialog(frame, "Green Channel index must be comprised between 0 and " + (nChannels-1), "Input Error");
            return;
        }
        int blueChannelIndex = (int) dlg.getNextNumber();
        if (blueChannelIndex < 0 || blueChannelIndex >= nChannels)
        {
            ImagoGui.showErrorDialog(frame, "Blue Channel index must be comprised between 0 and " + (nChannels-1), "Input Error");
            return;
        }
        

        ScalarArray<?> redChannel = vectorArray.channel(redChannelIndex);
        ScalarArray<?> greenChannel = vectorArray.channel(greenChannelIndex);
        ScalarArray<?> blueChannel = vectorArray.channel(blueChannelIndex);
        double[] redValuesRange = redChannel.finiteValueRange();
        double[] greenValuesRange = greenChannel.finiteValueRange();
        double[] blueValuesRange = blueChannel.finiteValueRange();
        
        
		// convert arrays to UInt8
        UInt8Array redChannel8 = UInt8Array.convert(redChannel, redValuesRange[0],
                redValuesRange[1]);
        UInt8Array greenChannel8 = UInt8Array.convert(greenChannel, greenValuesRange[0],
                greenValuesRange[1]);
        UInt8Array blueChannel8 = UInt8Array.convert(blueChannel, blueValuesRange[0],
                blueValuesRange[1]);
		
		// concatenate the three channels to create an RGB8 array
		RGB8Array rgbArray = RGB8Array.mergeChannels(redChannel8, greenChannel8, blueChannel8);
		
		// create the image corresponding to channels concatenation
		Image rgbImage = new Image(rgbArray, image);
		rgbImage.setName(image.getName() + "-RGB");

		// add the image document to GUI
		frame.getGui().createImageFrame(rgbImage);
	}
	
}
