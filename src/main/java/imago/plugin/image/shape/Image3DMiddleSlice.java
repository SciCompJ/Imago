/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.process.shape.SimpleSlicer;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class Image3DMiddleSlice implements Plugin
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
		System.out.println("middle Slice");
		
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
		frame.getGui().createImageFrame(result); 
	}

}
