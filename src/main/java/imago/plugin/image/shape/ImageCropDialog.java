/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.process.shape.Crop;
import net.sci.image.Image;

/**
 * Crop an image by specifying the bounds in a dialog. 
 * 
 * @author David Legland
 *
 */
public class ImageCropDialog implements FramePlugin
{
	public ImageCropDialog()
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
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(frame, "Crop Image");
		for (int d = 0; d < nd; d++)
		{
            gd.addNumericField("Min index. " + (d+1) + " (incl.)", 0, 0);
            gd.addNumericField("Max index. " + (d+1) + " (excl.)", array.size(d), 0);
		}
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        int[] minDims = new int[nd];
        int[] maxDims = new int[nd];
		for (int d = 0; d < nd; d++)
		{
            minDims[d] = (int) gd.getNextNumber();
            maxDims[d] = (int) gd.getNextNumber();
		}

		// create operator box filtering operator
		Crop op = Crop.fromMinMax(minDims, maxDims);
		op.addAlgoListener((ImageFrame) frame);
		
		// apply operator on current image array
		Array<?> result = op.process(array);
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-crop");
		
		// add the image document to GUI
		frame.addChild(frame.getGui().createImageFrame(resultImage));
	}
	
    /**
     * Returns true if the current frame contains an image.
     * 
     * @param frame
     *            the frame containing reference to this plugin
     * @return true if the frame contains an image.
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return true;
    }
}
