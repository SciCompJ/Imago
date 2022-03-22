/**
 * 
 */
package imago.plugin.image.file;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.binary.BinaryArray;
import net.sci.array.scalar.Float32Array;
import net.sci.array.scalar.Float64Array;
import net.sci.array.scalar.Int32Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt16Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;

/**
 * Creates a new image, filled with the specified value.
 * 
 * @author David Legland
 *
 */
public class CreateNewImage implements FramePlugin
{
    /** The list of possible types for creating array. */
    static String[] typeList = new String[]{"Binary", "Gray8", "Gray16", "Int32", "Float32", "Float64"};
	
	/**
     * Default empty constructor.
     */
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
		
		// determine default values for dialog
        String baseName = frame.getGui().getAppli().createHandleName("NoName");
		int sizeX_init = 200;
        int sizeY_init = 200;
        int sizeZ_init = 1;
        if (frame instanceof ImageFrame)
        {
            Image image = ((ImageFrame) frame).getImage();
            sizeX_init = image.getSize(0);
            sizeY_init = image.getSize(1);
            if (image.getDimension() > 2)
            {
                sizeZ_init = image.getSize(2);
            }
        }
		
        // create dialog to enter options
		GenericDialog gd = new GenericDialog(frame, "New Image");
		gd.addTextField("Name: ", baseName);
        gd.addNumericField("Width: ", sizeX_init, 0);
		gd.addNumericField("Height: ", sizeY_init, 0);
		gd.addNumericField("Depth: ", sizeZ_init, 0);
		gd.addChoice("Image Type: ", typeList, typeList[1]);
		gd.addNumericField("Fill Value: ", 0, 0);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        String imageName = gd.getNextString();
		int sizeX = (int) gd.getNextNumber();
		int sizeY = (int) gd.getNextNumber();
		int sizeZ = (int) gd.getNextNumber();
		int typeIndex = gd.getNextChoiceIndex();
		double fillValue = gd.getNextNumber();
		
		// create dimension vector for the new array
		int[] dims = sizeZ <= 1 ? new int[]{sizeX, sizeY} : new int[]{sizeX, sizeY, sizeZ}; 
		
		// Create the array depending on the type
		ScalarArray<?> array = null;
		switch (typeIndex)
		{
		case 0: array = BinaryArray.create(dims); break;
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
		image.setName(imageName);
		
		// add the image document to GUI
		frame.createImageFrame(image);
	}
}
