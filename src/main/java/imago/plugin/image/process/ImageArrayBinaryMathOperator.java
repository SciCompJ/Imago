/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;

import java.util.function.BiFunction;

import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.process.Math;
import net.sci.array.scalar.Float32Array;
import net.sci.array.scalar.Float64Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;

/**
 * Apply a simple math function between two image arrays.
 * 
 * @author David Legland
 *
 */
public class ImageArrayBinaryMathOperator implements Plugin
{
    /**
     * The list of functions that can be applied.
     */
    String[] functionNames = new String[]{"Plus", "Minus", "Times", "Divides", "Min", "Max"};
    
    /**
     * Control the type of output array.
     */
    String[] outputTypeNames = new String[]{"Same as Image 1", "Same as Image 2", "Float32", "Float64"};
    
	public ImageArrayBinaryMathOperator()
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
		System.out.println("apply math function");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
		}
		ScalarArray<?> inputArray = (ScalarArray<?>) array;

		String[] imageNames = frame.getGui().getAppli().getImageHandleNames().toArray(new String[]{});
		
		GenericDialog gd = new GenericDialog(frame, "Bianry Math Operator");
		gd.addChoice("Image 1", imageNames, imageNames[0]);
        gd.addChoice("Operation", functionNames, functionNames[0]);
        gd.addChoice("Image 2", imageNames, imageNames[imageNames.length > 1 ? 1 : 0]);
        gd.addChoice("OutputT Type", outputTypeNames, outputTypeNames[0]);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        String image1Name = gd.getNextChoice();
		String functionName = gd.getNextChoice();
        String image2Name = gd.getNextChoice();
		String typeName = gd.getNextChoice();
        
        Image image1 = frame.getGui().getAppli().getImageHandleFromName(image1Name).getImage();
        if (!image1.isScalarImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
        }
        ScalarArray<?> array1 = (ScalarArray<?>) image1.getData();

        Image image2 = frame.getGui().getAppli().getImageHandleFromName(image2Name).getImage();
        if (!image2.isScalarImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
        }
        ScalarArray<?> array2 = (ScalarArray<?>) image2.getData();
		
        if (!Arrays.isSameSize(array1, array2))
        {
            ImagoGui.showErrorDialog(frame, "Both image arrays must have same dimensions");
            return;
        }
        
		ScalarArray<?> result;
		switch (typeName)
        {
        case "Same As Image 1":
            result = inputArray.newInstance(array1.size());
            break;
        case "Same As Image 2":
            result = inputArray.newInstance(array2.size());
            break;
        case "Float32":
            result = Float32Array.create(array.size());
            break;
        case "Float64":
            result = Float64Array.create(array.size());
            break;
        default:
            throw new RuntimeException("Unknown type name: " + typeName);
        }

		BiFunction<Double, Double, Double> fun;
		switch (functionName)
		{
        case "Plus":
            fun = (x, y) -> x + y;
            break;
        case "Minus":
            fun = (x, y) -> x - y;
            break;
        case "Times":
            fun = (x, y) -> x * y;
            break;
        case "Divides":
            fun = (x, y) -> x / y;
            break;
        case "Min":
            fun = (x, y) -> java.lang.Math.min(x, y);
            break;
        case "Max":
            fun = (x, y) -> java.lang.Math.max(x, y);
            break;
        default: throw new RuntimeException("Unknown function name: " + functionName); 
		}

        // apply operator on current images
		Math.apply(array1, array2, result, fun);
		
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-" + functionName);
		
		// add the image document to GUI
		frame.getGui().addNewDocument(resultImage);
	}
	
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        return true;
    }
}
