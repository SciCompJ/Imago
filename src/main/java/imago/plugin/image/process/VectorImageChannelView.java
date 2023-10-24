/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;


/**
 * Extract a specific channel from a vector image
 * 
 * @author David Legland
 *
 */
public class VectorImageChannelView implements FramePlugin
{
	public VectorImageChannelView()
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
		if (!(array instanceof VectorArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires a Vector image", "Data Type Error");
			return;
		}

		VectorArray<?> vectorArray = (VectorArray<?>) array;
		int nChannels = vectorArray.channelCount();
		
		GenericDialog dlg = new GenericDialog(frame, "Extract Channel");
		dlg.addNumericField("Channel index", 0, 0, String.format("Between 0 and %d", nChannels));
		
		// Display dialog and wait for OK or Cancel
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // extract user choices
        int channelIndex = (int) dlg.getNextNumber();
        if (channelIndex < 0 || channelIndex >= nChannels)
        {
            ImagoGui.showErrorDialog(frame, "Channel index must be comprised between 0 and " + (nChannels-1), "Input Error");
            return;
        }

        // allocate memory for result array
        ScalarArray<?> channelArray = vectorArray.channel(channelIndex);
        
		Image resultImage = new Image(channelArray, image);
		String name = image.getName();
		resultImage.setName(name + "-channel" + String.format("%d", channelIndex));
				
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage); 
	}

}
