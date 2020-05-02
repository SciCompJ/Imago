/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.process.filter.BoxFilter;

/**
 * Applies box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class ImageBoxFilter implements Plugin
{
	public ImageBoxFilter()
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
		System.out.println("box filter (generic)");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(frame, "Flat Blur");
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

		// create operator box filtering operator
		BoxFilter filter = new BoxFilter(diameters);
		filter.addAlgoListener((ImageFrame) frame);
		
		// apply operator on current image
		Image result = filter.process(image);
		result.setName(image.getName() + "-filt");
		
		// add the image document to GUI
		frame.addChild(frame.getGui().addNewDocument(result));
	}
	
    /**
     * Returns true if the current frame contains a scalar image or a vector image.
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
        ImageHandle doc = ((ImageFrame) frame).getDocument();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.isScalarImage() || image.isVectorImage();
    }
}
