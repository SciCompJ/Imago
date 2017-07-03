/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.process.shape.Slicer;
import net.sci.image.Image;

/**
 * Applies box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class Image3DOrthoslicesImageAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Image3DOrthoslicesImageAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("box filter (generic)");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		GenericDialog gd = new GenericDialog(this.frame, "Flat Blur");
		for (int d = 0; d < nd; d++)
		{
			gd.addNumericField("Ref. pos. " + (d+1), 3, 0);
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
		this.gui.addNewDocument(result);
	}

	private <T> Array<T> process(Array<T> array, int[] refPos)
	{
		int sizeX = array.getSize(0);
		int sizeY = array.getSize(1);
		int sizeZ = array.getSize(2);

		int sizeX2 = sizeX + sizeZ;
		int sizeY2 = sizeY + sizeZ;
		
		Array<T> result = array.newInstance(new int[]{sizeX2, sizeY2});
		
		Array<T> sliceXY = Slicer.slice2d(array, 0, 1, refPos);
		Array<T> sliceZY = Slicer.slice2d(array, 2, 1, refPos);
		Array<T> sliceXZ = Slicer.slice2d(array, 0, 2, refPos);
		
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
