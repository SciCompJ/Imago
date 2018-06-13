/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt8Array;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class ImageSplitChannels implements Plugin
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
		if (!(frame instanceof ImagoDocViewer))
			return;
		ImagoDocViewer iframe = (ImagoDocViewer) frame;
		Image image = iframe.getDocument().getImage();

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
		
		int nc = ((VectorArray<?>) array).getVectorLength();
		
		// Create an array with the name of each channel
		String[] channelNames;
		if (array instanceof RGB8Array)
		{
			channelNames = new String[] { "red", "green", "blue" };

			int c = 0;
			for (UInt8Array channel : RGB8Array.splitChannels((RGB8Array) array))
			{
				// create the image corresponding to current channel
				Image channelImage = new Image(channel, image);
				channelImage.setName(image.getName() + "(" + channelNames[c++] + ")");
				
				// add the image document to GUI
				frame.getGui().addNewDocument(channelImage);
			}
		} 
		else
		{
			channelNames = new String[nc];
			for (int c = 0; c < nc; c++)
			{
				channelNames[c] = "channel" + c;
			}
			
			int c = 0;
			for (ScalarArray<?> channel : VectorArray.splitChannels((VectorArray<?>) array))
			{
				// create the image corresponding to current channel
				Image channelImage = new Image(channel, image);
				channelImage.setName(image.getName() + "(" + channelNames[c++] + ")");
				
				// add the image document to GUI
				frame.getGui().addNewDocument(channelImage);
			}
		}
	}
}
