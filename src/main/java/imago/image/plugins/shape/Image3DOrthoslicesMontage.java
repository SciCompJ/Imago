/**
 * 
 */
package imago.image.plugins.shape;

import java.util.ArrayList;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.shape.SimpleSlicer;
import net.sci.image.Image;
import net.sci.image.shape.Montage;

/**
 * Converts a 3D image into a mosaic image containing three orthogonal slices.
 * 
 * @author David Legland
 *
 */
public class Image3DOrthoslicesMontage implements FramePlugin
{
    private static final String[] layoutNames = new String[] {"2-by-2", "3-by-1", "1-by-3"};
    private static final int[][] layoutDims = new int[][] {{2, 2}, {3, 1}, {1, 3}};
    
	public Image3DOrthoslicesMontage()
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
		
		GenericDialog gd = new GenericDialog(frame, "Create orthoslice Image");
        gd.addChoice("Layout", layoutNames, layoutNames[0]);
        for (int d = 0; d < nd; d++)
		{
			gd.addNumericField("Ref. pos. " + (d+1), (int) (array.size(d) / 2), 0);
		}
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		int layoutIndex = gd.getNextChoiceIndex();
		int[] refPos = new int[nd];
		for (int d = 0; d < nd; d++)
		{
			refPos[d] = (int) gd.getNextNumber();
		}

		int[] dims = layoutDims[layoutIndex];
        Array2D<?> res = Montage.create(dims[0], dims[1], orthoSlices(array, refPos));
		
		Image result = new Image(res, image);
		result.setName(image.getName() + "-orthoSlices");
		
		// add the image document to GUI
		ImageFrame.create(result, frame);
	}

    private <T> ArrayList<Array2D<T>> orthoSlices(Array<T> array, int[] refPos)
    {
        ArrayList<Array2D<T>> slices = new ArrayList<Array2D<T>>(3);
        slices.add(SimpleSlicer.slice2d(array, 0, 1, refPos));
        slices.add(SimpleSlicer.slice2d(array, 2, 1, refPos));
        slices.add(SimpleSlicer.slice2d(array, 0, 2, refPos));
        return slices;
    }
}
