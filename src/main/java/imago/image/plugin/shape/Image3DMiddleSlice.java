/**
 * 
 */
package imago.image.plugin.shape;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.shape.SimpleSlicer;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class Image3DMiddleSlice implements FramePlugin
{
	public Image3DMiddleSlice() 
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

		// check image dimensionality
		if (image.getDimension() < 2)
		{
			System.err.println("Requires 3-dimensional image");
			return;
		}
		
		// compute slice index
		int index = Math.max((image.getSize(2)) / 2 - 1, 0);
		
		// compute resulting slice
		SimpleSlicer filter = new SimpleSlicer(2, index);
		Image result = image.apply(filter);
				
		// add the image document to GUI
		ImageFrame.create(result, frame);
	}

}
