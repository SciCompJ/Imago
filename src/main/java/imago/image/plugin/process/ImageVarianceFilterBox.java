/**
 * 
 */
package imago.image.plugin.process;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.filtering.VarianceFilterBox;

/**
 * Applies variance box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class ImageVarianceFilterBox implements FramePlugin
{
	public ImageVarianceFilterBox()
	{
	}

	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
		ImageHandle doc = imageFrame.getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(frame, "Variance Filter");
		for (int d = 0; d < nd; d++)
		{
			gd.addNumericField("Size dim. " + (d+1), 3, 0);
		}
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		int[] diameters = new int[nd];
		for (int d = 0; d < nd; d++)
		{
			diameters[d] = (int) gd.getNextNumber();
		}
		
		// create median box operator
		VarianceFilterBox filter = new VarianceFilterBox(diameters);
		
		// apply operator on current image
		Image result = imageFrame.runOperator(filter, image);
		result.setName(image.getName() + "-varFilt");
		
		// add the image document to GUI
		ImageFrame.create(result, frame);
	}

    /**
     * Returns true if the current frame contains a scalar image or a vector
     * image.
     * 
     * @param frame
     *            the frame containing reference to this plugin
     * @return true if the frame contains a scalar or vector image.
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

        return image.isScalarImage() || image.isVectorImage();
    }
}
