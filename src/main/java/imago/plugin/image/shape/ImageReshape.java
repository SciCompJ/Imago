/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.process.shape.Reshape;
import net.sci.image.Image;

/**
 * Reshape an image by specifying new dimensions.
 * 
 * @author David Legland
 *
 */
public class ImageReshape implements FramePlugin
{
	public ImageReshape()
	{
	}

	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("reshape image");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		int[] newDims = array.size();
		while(true)
		{
		    GenericDialog gd = new GenericDialog(frame, "Reshape");
		    for (int d = 0; d < nd; d++)
		    {
		        gd.addNumericField("Size dim. " + (d+1), newDims[d], 0);
		    }
		    gd.showDialog();
		    
		    if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		    {
		        return;
		    }
		    
		    // parse dialog results
		    for (int d = 0; d < nd; d++)
		    {
		        newDims[d] = (int) gd.getNextNumber();
		    }
		    
		    // If compatibility of dimensions is met, break loop 
		    int numel = cumProd(array.size());
		    if (cumProd(newDims) == numel)
		    {
		        break;
		    }
		    
            ImagoGui.showErrorDialog(frame, 
                    "Output element number should match input element number: " + numel);
		};
		
		// create reshape operator
		Reshape filter = new Reshape(newDims);
		
		// apply operator on current image
        Array<?> result = filter.process(array);
        Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-reshape");
		
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage);
	}

    private static final int cumProd(int[] dims)
    {
        int prod = 1;
        for (int d : dims)
        {
            prod *= d;
        }
        return prod;
    }
}
