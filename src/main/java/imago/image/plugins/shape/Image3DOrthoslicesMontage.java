/**
 * 
 */
package imago.image.plugins.shape;

import java.util.List;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.shape.PermuteDimensions;
import net.sci.array.shape.Slicer2D;
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
		
		// open dialog to choose options
		GenericDialog gd = new GenericDialog(frame, "Create orthoslice Image");
        gd.addChoice("Layout", layoutNames, layoutNames[0]);
        for (int d = 0; d < nd; d++)
		{
			gd.addIntegerField("Ref. pos. " + (d+1), array.size(d) / 2);
		}
        
        // retrieve user options
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
			refPos[d] = gd.getNextInteger();
		}
		
        long t0 = System.nanoTime();
        int[] dims = layoutDims[layoutIndex];
        Array2D<?> res = Montage.create(dims[0], dims[1], orthoSlices(array, refPos, frame));
        long t1 = System.nanoTime();
		
        frame.algoTerminated("Image3DOrthoslicesMontage", (t1 - t0) / 1_000_000.0);

        Image result = new Image(res, image);
		result.setName(image.getName() + "-orthoSlices");
		
		// add the image document to GUI
		ImageFrame.create(result, frame);
	}

    private <T> List<Array2D<T>> orthoSlices(Array<T> array, int[] refPos, AlgoListener al)
    {
        Slicer2D slicerXY = new Slicer2D(0, 1, refPos);
        slicerXY.addAlgoListener(al);
        al.algoStatusChanged(new AlgoEvent(this, "Compute XY Slice"));
        Array2D<T> sliceXY = slicerXY.process(array);

        Slicer2D slicerYZ = new Slicer2D(1, 2, refPos);
        slicerYZ.addAlgoListener(al);
        al.algoStatusChanged(new AlgoEvent(this, "Compute YZ Slice"));
        Array2D<T> sliceYZ = slicerYZ.process(array);

        Slicer2D slicerZX = new Slicer2D(0, 2, refPos);
        slicerZX.addAlgoListener(al);
        al.algoStatusChanged(new AlgoEvent(this, "Compute ZX Slice"));
        Array2D<T> sliceXZ = slicerZX.process(array);
        Array2D<T> sliceZX = Array2D.wrap(new PermuteDimensions(new int[] {0, 1}).process(sliceXZ));

        return List.of(sliceXY, sliceYZ, sliceZX);
    }
}
