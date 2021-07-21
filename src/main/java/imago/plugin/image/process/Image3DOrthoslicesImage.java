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
import net.sci.array.process.shape.Slicer;
import net.sci.image.Image;

/**
 * Converts a 3D image into a mosaic image containing three orthogonal slices.
 * 
 * @author David Legland
 *
 */
public class Image3DOrthoslicesImage implements FramePlugin
{
	public Image3DOrthoslicesImage()
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
		System.out.println("Create orthoslice Image");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		GenericDialog gd = new GenericDialog(frame, "Create orthoslice Image");
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
		int[] refPos = new int[nd];
		for (int d = 0; d < nd; d++)
		{
			refPos[d] = (int) gd.getNextNumber();
		}

		Array<?> res = process(array, refPos);
		
		Image result = new Image(res, image);
		result.setName(image.getName() + "-slices");
		
		// add the image document to GUI
		frame.getGui().createImageFrame(result);
	}

	private <T> Array<T> process(Array<T> array, int[] refPos)
	{
		int sizeX = array.size(0);
		int sizeY = array.size(1);
		int sizeZ = array.size(2);

		int sizeX2 = sizeX + sizeZ;
		int sizeY2 = sizeY + sizeZ;
		
		Array<T> result = array.newInstance(new int[]{sizeX2, sizeY2});
		
        Array<T> sliceXY = new Slicer(new int[]{0, 1}, refPos).process(array);
        Array<T> sliceZY = new Slicer(new int[]{2, 1}, refPos).process(array);
        Array<T> sliceXZ = new Slicer(new int[]{0, 2}, refPos).process(array);
//		Array<T> sliceXY = SimpleSlicer.slice2d(array, 0, 1, refPos);
//		Array<T> sliceZY = SimpleSlicer.slice2d(array, 2, 1, refPos);
//		Array<T> sliceXZ = SimpleSlicer.slice2d(array, 0, 2, refPos);
		
		int[] srcPos = new int[2];
		int[] tgtPos = new int[2];
		
		for (int y = 0; y < sizeY; y++)
		{
			srcPos[1] = y;
			tgtPos[1] = y;
			for (int x = 0; x < sizeX; x++)
			{
				srcPos[0] = x;
				tgtPos[0] = x;
				result.set(tgtPos, sliceXY.get(srcPos));
			}
		}
				
		for (int y = 0; y < sizeY; y++)
		{
			srcPos[1] = y;
			tgtPos[1] = y;
			for (int z = 0; z < sizeZ; z++)
			{
				srcPos[0] = z;
				tgtPos[0] = z + sizeX;
				result.set(tgtPos, sliceZY.get(srcPos));
			}
		}
				
		for (int z = 0; z < sizeZ; z++)
		{
			srcPos[1] = z;
			tgtPos[1] = z + sizeY;
			for (int x = 0; x < sizeX; x++)
			{
				srcPos[0] = x;
				tgtPos[0] = x;
				result.set(tgtPos, sliceXZ.get(srcPos));
			}
		}
				
		return result;
	}
}
