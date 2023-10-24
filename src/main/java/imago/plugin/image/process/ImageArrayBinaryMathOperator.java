/**
 * 
 */
package imago.plugin.image.process;

import java.util.function.BiFunction;

import imago.gui.*;
import imago.gui.image.ImageFrame;
import net.sci.array.Arrays;
import net.sci.array.process.math.MathBinaryOperator;
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
public class ImageArrayBinaryMathOperator implements FramePlugin
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
		String[] imageNames = frame.getGui().getAppli().getImageHandleNames().toArray(new String[]{});
		int index1 = 0;
		if (frame instanceof ImageFrame)
		{
		    String imageName = ((ImageFrame) frame).getImageHandle().getName();
		    index1 = findStringIndex(imageName, imageNames);
		}
		int index2 = (int) java.lang.Math.min(index1 + 1, imageNames.length-1);

		GenericDialog gd = new GenericDialog(frame, "Math Binary Operator");
		gd.addChoice("Image 1", imageNames, imageNames[index1]);
        gd.addChoice("Operation", functionNames, functionNames[0]);
        gd.addChoice("Image 2", imageNames, imageNames[index2]);
        gd.addChoice("Output Type", outputTypeNames, outputTypeNames[0]);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        String image1Name = gd.getNextChoice();
		String functionName = gd.getNextChoice();
        String image2Name = gd.getNextChoice();
		int outputTypeIndex = gd.getNextChoiceIndex();
        
        // retrieve images from names
        Image image1 = frame.getGui().getAppli().getImageHandleFromName(image1Name).getImage();
        if (!image1.isScalarImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
        }
        Image image2 = frame.getGui().getAppli().getImageHandleFromName(image2Name).getImage();
        if (!image2.isScalarImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
        }

        // extract arrays
        ScalarArray<?> array1 = (ScalarArray<?>) image1.getData();
        ScalarArray<?> array2 = (ScalarArray<?>) image2.getData();

        // check dimensions
        if (!Arrays.isSameSize(array1, array2))
        {
            ImagoGui.showErrorDialog(frame, "Both image arrays must have same dimensions");
            return;
        }
        
        // create output array
        ScalarArray<?> result;
		switch (outputTypeIndex)
        {
        case 0:
            result = array1.newInstance(array1.size());
            break;
        case 1:
            result = array2.newInstance(array2.size());
            break;
        case 2:
            result = Float32Array.create(array1.size());
            break;
        case 3:
            result = Float64Array.create(array1.size());
            break;
        default:
            throw new RuntimeException("Unknown type index: " + outputTypeIndex);
        }

        // use a lambda to represent the function to apply
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
            fun = java.lang.Math::min;
            break;
        case "Max":
            fun = java.lang.Math::max;
            break;
        default: throw new RuntimeException("Unknown function name: " + functionName); 
		}

		// create operator
		MathBinaryOperator op = new MathBinaryOperator(fun);
        op.addAlgoListener(frame);
        
        // run operator
		long t0 = System.nanoTime();
        op.process(array1, array2, result);
        long t1 = System.nanoTime();
        
        // display elapsed time
		if (frame instanceof ImageFrame)
        {
		    double dt = (t1 - t0) / 1_000_000.0;
           ((ImageFrame) frame).showElapsedTime(functionName, dt, image1);
        }
		
		// create and display result image
		Image resultImage = new Image(result, image1);
		resultImage.setName(String.format("%s(%s, %s)", functionName, image1.getName(), image2.getName()));
        
		// add the image document to GUI
		ImageFrame.create(resultImage, frame);
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
