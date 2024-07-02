/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class ImageSplitChannels implements FramePlugin
{
	public ImageSplitChannels()
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
		// Check type is image frame
		if (!(frame instanceof ImageFrame))
			return;
		ImageFrame iframe = (ImageFrame) frame;
		Image image = iframe.getImageHandle().getImage();

		// requires a vector image (color)
		if (!image.isVectorImage())
		{
			return;
		}
		
		Array<?> array = image.getData();
		if (!(array instanceof VectorArray))
		{
			System.out.println("Requires a Vector array");
			return; 
		}
		
		
		// Create an array with the name of each channel
		String[] channelNames;
		if (array instanceof RGB8Array)
		{
			channelNames = new String[] { "red", "green", "blue" };

			int c = 0;
			for (UInt8Array channel : ((RGB8Array) array).channels())
			{
				// create the image corresponding to current channel
				Image channelImage = new Image(channel.duplicate(), image);
				channelImage.setName(image.getName() + "-" + channelNames[c++]);
				
				// add the image document to GUI
				ImageFrame.create(channelImage, frame);
			}
		} 
		else
		{
	        int nc = ((VectorArray<?,?>) array).channelCount();
			channelNames = new String[nc];
			String pattern = String.format("channel%%0%dd", (int) Math.ceil(Math.log10(nc)));
			for (int c = 0; c < nc; c++)
			{
				channelNames[c] = String.format(pattern, c);
			}
			
			int c = 0;
			for (ScalarArray<?> channel : ((VectorArray<?,?>) array).channels())
			{
				// create the image corresponding to current channel
				Image channelImage = new Image(channel.duplicate(), image);
				// use same display settings as original image
				channelImage.getDisplaySettings().setDisplayRange(image.getDisplaySettings().getDisplayRange());
				channelImage.setName(image.getName() + "-" + channelNames[c++]);
				
				// add the image document to GUI
				ImageFrame.create(channelImage, frame);
			}
		}
	}
}
