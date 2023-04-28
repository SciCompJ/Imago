/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import imago.gui.viewer.StackSliceViewer;
import net.sci.image.Image;
import net.sci.image.process.shape.ImageSlicer;

/**
 * Extract a planar slice from a 3D image.
 * 
 * @author David Legland
 *
 */
public class Image3DGetCurrentSlice implements FramePlugin
{
    public Image3DGetCurrentSlice()
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
		ImageViewer viewer = ((ImageFrame) frame).getImageView();

		Image image	= doc.getImage();

		if (!(viewer instanceof StackSliceViewer))
		{
		    System.err.println("Requires 3D image viewer");
		    return;
		}
		
        // retrieve slice index
		int sliceIndex = viewer.getSlicingPosition(2);
		
		// compute slice
		Image result = ImageSlicer.slice2d(image, sliceIndex);
		
		// new name contains slice index, with number of digits depending on slice number
		int sizeZ = image.getSize(2);
		int nDigits = (int) Math.floor(Math.log10(sizeZ-1)) + 1;
		result.setName(image.getName() + "-z" + String.format("%0" + nDigits + "d", sliceIndex));
		
		// add the image document to GUI
		frame.getGui().createImageFrame(result);
	}
}
