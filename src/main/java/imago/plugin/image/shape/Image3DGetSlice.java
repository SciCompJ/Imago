/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.image.Image;
import net.sci.image.shape.ImageSlicer;

/**
 * Extract a planar slice from a 3D image.
 * 
 * @author David Legland
 *
 */
public class Image3DGetSlice implements FramePlugin
{
	public Image3DGetSlice()
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
		Image image	= doc.getImage();

		GenericDialog gd = new GenericDialog(frame, "Extract planar slice");
        gd.addNumericField("Dim. 1 ", 0, 0);
        gd.addNumericField("Dim. 2 ", 1, 0);
        gd.addNumericField("Slice index ", 0, 0);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        int dim1 = (int) gd.getNextNumber();
        int dim2 = (int) gd.getNextNumber();
        int sliceIndex = (int) gd.getNextNumber();

        int[] refPos = new int[]{sliceIndex, sliceIndex, sliceIndex};
        
        Image result = ImageSlicer.slice2d(image, dim1, dim2, refPos);
		result.setName(image.getName() + "-slices");
		
		// add the image document to GUI
		ImageFrame.create(result, frame);
	}
}
