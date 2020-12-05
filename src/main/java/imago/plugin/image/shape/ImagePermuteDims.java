/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.process.shape.PermuteDimensions;
import net.sci.image.Calibration;
import net.sci.image.Image;

/**
 * Permute image dimensions, by applying a permutation of integers between 0 and
 * nd-1.
 * 
 * @author David Legland
 *
 */
public class ImagePermuteDims implements FramePlugin
{
	public ImagePermuteDims()
	{
	}

	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("flip image dimensions");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		// create initial text
		String text = "0";
		for (int d = 1; d < nd; d++)
		{
		    text = text + "," + d;
		}
		
		int[] newDims;
		while(true)
		{
		    GenericDialog gd = new GenericDialog(frame, "Flip Dims");
		    gd.addTextField("dims order", text);
		    gd.showDialog();
		    
		    if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		    {
		        return;
		    }
		    
		    newDims = parseLabelList(gd.getNextString());
		    
		    // If compatibility of dimensions is met, break loop 
		    if (checkLabelList(newDims))
		    {
		        break;
		    }
		    
            ImagoGui.showErrorDialog(frame, 
                    "Not a valid permutation of integers between 0 and " + (nd-1));
		};
		
		// create reshape operator
		PermuteDimensions op = new PermuteDimensions(newDims);
		
		// apply operator on current image
        Array<?> result = op.process(array);
        Image resultImage = new Image(result, image);
        
        // also permute axis information
        Calibration calib0 = image.getCalibration();
        Calibration calib = resultImage.getCalibration();
        for (int d = 0; d < image.getDimension(); d++)
        {
            calib.setAxis(d, calib0.getAxis(newDims[d]).duplicate());
        }
		resultImage.setName(image.getName() + "-permDims");
		
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage);
	}

    /**
     * Extracts a list of integer labels from a string representation.
     * 
     * @param string
     *            the String containing labels, separated by commas or spaces.
     * @return the list of integer labels identified within the string
     */
    private static final int[] parseLabelList(String string) 
    {
        String[] tokens = string.split("[, ]+");
        int n = tokens.length;
        
        int[] labels = new int[n];
        for (int i = 0; i < n; i++)
        {
            labels[i] = Integer.parseInt(tokens[i]);
        }
        return labels;
    }
    
    /**
     * Each integer between 0 and n-1 should be present once in the array, no
     * matter the position.
     * 
     * @param labelList
     *            a list of n integers
     * @return true if the array contains each integer between 0 and n-1.
     */
    private static final boolean checkLabelList(int[] labelList)
    {
        // iterate over the values to check
        for (int i = 0; i < labelList.length; i++)
        {
            // iterate over the vaues in the array
            boolean found = false;
            for (int v : labelList)
            {
                if (v == i)
                {
                    found = true;
                    break;
                }
            }
            
            if (!found)
            {
                return false;
            }
        }
        
        return true;
    }
}
