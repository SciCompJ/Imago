/**
 * 
 */
package imago.plugin.image.file;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.data.BinaryArray;
import net.sci.array.data.Float32Array;
import net.sci.array.data.Float64Array;
import net.sci.array.data.Int32Array;
import net.sci.array.data.ScalarArray;
import net.sci.array.data.UInt16Array;
import net.sci.array.data.UInt8Array;
import net.sci.image.Image;

/**
 * Creates a new empty image.
 * 
 * @author David Legland
 *
 */
public class CreateNewImage implements Plugin
{

	enum ImageType
	{
		GRAY8,
		GRAY16,
		INT32,
		FLOAT32,
		FLOAT64
	};
	
	public CreateNewImage()
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
		System.out.println("create new image");

		String[] typeList = new String[]{"Binary", "Gray8", "Gray16", "Int32", "Float32", "Float64"};
		
		GenericDialog gd = new GenericDialog(frame, "Create Image");
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
		ScalarArray<?> array = null;
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
		array.fillValue(fillValue);
		
		// Create image
		Image image = new Image(array);
		
		// add the image document to GUI
		frame.getGui().addNewDocument(image);
	}
}
