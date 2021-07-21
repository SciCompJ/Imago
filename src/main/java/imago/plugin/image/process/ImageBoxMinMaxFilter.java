/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.process.filter.BoxMinMaxFilterNaive;

/**
 * Applies box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class ImageBoxMinMaxFilter implements FramePlugin
{
	public ImageBoxMinMaxFilter()
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
		System.out.println("box min/max filter");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(frame, "Box Min/Max");
		gd.addChoice("Operation",  new String[]{"Min.",  "Max."}, "Min.");
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
		boolean minFilter = gd.getNextChoiceIndex() == 0;
		int[] diameters = new int[nd];
		for (int d = 0; d < nd; d++)
		{
			diameters[d] = (int) gd.getNextNumber();
		}

		// create operator box filtering operator
		BoxMinMaxFilterNaive.Type type = minFilter ? BoxMinMaxFilterNaive.Type.MIN : BoxMinMaxFilterNaive.Type.MAX;  
		BoxMinMaxFilterNaive filter = new BoxMinMaxFilterNaive(type, diameters);
		filter.addAlgoListener((ImageFrame) frame); 

		// apply operator on current image
		Image result = filter.process(image);
		
		// choose name of result
		String suffix = minFilter ? "-minFilt" : "-maxFilt";
		result.setName(image.getName() + suffix);
		
		// add the image document to GUI
		frame.getGui().createImageFrame(result);
	}

	/**
	 * Returns true if the current frame contains a scalar or vector image.
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
