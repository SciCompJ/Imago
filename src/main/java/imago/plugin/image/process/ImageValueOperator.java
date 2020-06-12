/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.*;
import net.sci.array.process.Math;
import net.sci.array.scalar.Float32Array;
import net.sci.array.scalar.Float64Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;

/**
 * Combines an image with a scalar value to create a new image.
 * 
 * @author David Legland
 *
 */
public class ImageValueOperator implements Plugin
{
    /**
     * The list of functions that can be applied.
     */
    String[] functionNames = new String[]{"Plus", "Minus", "Times", "Divides", "Min", "Max"};
    
    /**
     * Control the type of output array.
     */
    String[] outputTypeNames = new String[]{"Same as Input", "Float32", "Float64"};
    
	public ImageValueOperator()
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

		String[] imageNames = frame.getGui().getAppli().getImageHandleNames().toArray(new String[]{});
		int index = 0;
		if (frame instanceof ImageFrame)
		{
		    String imageName = ((ImageFrame) frame).getImageHandle().getName();
		    index = findStringIndex(imageName, imageNames);
		}

		GenericDialog gd = new GenericDialog(frame, "Math Operator");
		gd.addChoice("Image", imageNames, imageNames[index]);
        gd.addChoice("Operation", functionNames, functionNames[0]);
        gd.addNumericField("Value", 1.0, 2, "The scalar value to use as operand");
        gd.addChoice("Output Type", outputTypeNames, outputTypeNames[0]);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        String imageName = gd.getNextChoice();
		String functionName = gd.getNextChoice();
        double value = gd.getNextNumber();
		int outputTypeIndex = gd.getNextChoiceIndex();
        
        Image image = frame.getGui().getAppli().getImageHandleFromName(imageName).getImage();
        if (!image.isScalarImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
        }
        ScalarArray<?> array = (ScalarArray<?>) image.getData();

        // allocate memory for result
        ScalarArray<?> result;
		switch (outputTypeIndex)
        {
        case 0:
            result = array.newInstance(array.size());
            break;
        case 1:
            result = Float32Array.create(array.size());
            break;
        case 2:
            result = Float64Array.create(array.size());
            break;
        default:
            throw new RuntimeException("Unknown type index: " + outputTypeIndex);
        }

        // apply function and store to result
		switch (functionName)
		{
        case "Plus":
            Math.add(array, value, result);
            break;
        case "Minus":
            Math.subtract(array, value, result);
            break;
        case "Times":
            Math.multiply(array, value, result);
            break;
        case "Divides":
            Math.divide(array, value, result);
            break;
        case "Min":
            Math.min(array, value, result);
            break;
        case "Max":
            Math.max(array, value, result);
            break;
        default: throw new RuntimeException("Unknown function name: " + functionName); 
		}

        // create result image
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-" + functionName);
		
		// add the image document to GUI
		frame.createImageFrame(resultImage);
	}

	private int findStringIndex(String string, String[] array)
	{
	    if (string == null)
	    {
	        return 0;
	    }

	    for (int i = 0; i < array.length; i++)
	    {
	        if (string.equals(array[i]))
	        {
	            return i;
	        }
	    }

	    return 0;
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
