/**
 * 
 */
package imago.gui.action.file;

import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.data.BinaryArray;
import net.sci.array.data.Float32Array;
import net.sci.array.data.Float64Array;
import net.sci.array.data.Int32Array;
import net.sci.array.data.UInt16Array;
import net.sci.array.data.UInt8Array;
import net.sci.image.Image;

/**
 * Creates a new empty image.
 * 
 * @author David Legland
 *
 */
public class CreateEmptyImageAction extends ImagoAction
{

	enum ImageType
	{
		GRAY8,
		GRAY16,
		INT32,
		FLOAT32,
		FLOAT64
	};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CreateEmptyImageAction(ImagoFrame frame, String name)
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
		System.out.println("create new image");

		String[] typeList = new String[]{"Binary", "Gray8", "Gray16", "Int32", "Float32", "Float64"};
		
		GenericDialog gd = new GenericDialog(this.frame, "Create Image");
		gd.addNumericField("Width: ", 200, 0);
		gd.addNumericField("Height: ", 200, 0);
		gd.addNumericField("Depth: ", 1, 0);
		gd.addChoice("Image Type: ", typeList, typeList[1]);
		gd.addNumericField("Fill Value: ", 0, 0);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		int sizeX = (int) gd.getNextNumber();
		int sizeY = (int) gd.getNextNumber();
		int sizeZ = (int) gd.getNextNumber();
		int typeIndex = gd.getNextChoiceIndex();
		double fillValue = gd.getNextNumber();
		
		// create dimension vector for the new array
		boolean is2D = sizeZ <= 1;
		int[] dims = is2D ? new int[]{sizeX, sizeY} : new int[]{sizeX, sizeY, sizeZ}; 
		
		// Create the array depending on the type
		Array<?> array = null;
		switch (typeIndex)
		{
		case 0: 
			array = BinaryArray.create(dims); break;
		case 1: array = UInt8Array.create(dims); break;
		case 2: array = UInt16Array.create(dims); break;
		case 3: array = Int32Array.create(dims); break;
		case 4: array = Float32Array.create(dims); break;
		case 5: array = Float64Array.create(dims); break;
		default:
			return;
		}

		// fill array with specified value
		Array.Iterator<?> iter = array.iterator();
		while(iter.hasNext())
		{
			iter.forward();
			iter.setValue(fillValue);
		}
		
		// apply operator on current image
		Image image = new Image(array);
		
		// add the image document to GUI
		this.gui.addNewDocument(image);
	}

}
