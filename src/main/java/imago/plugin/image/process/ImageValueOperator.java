/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.*;
import net.sci.array.Array;
import net.sci.array.process.Math;
import net.sci.array.scalar.Float32Array;
import net.sci.array.scalar.Float64Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.vector.Float32VectorArray;
import net.sci.array.vector.Float64VectorArray;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;

/**
 * Combines an image with a scalar value to create a new image.
 * 
 * @author David Legland
 *
 */
public class ImageValueOperator implements FramePlugin
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
        
		// identify source image
        Image image = frame.getGui().getAppli().getImageHandleFromName(imageName).getImage();
        
        Array<?> result = null;
        if (image.isScalarImage())
        {
            ScalarArray<?> array = (ScalarArray<?>) image.getData();
    
            // allocate memory for result
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
    		
    		processScalar(array, (ScalarArray<?>) result, functionName, value);
        }
        else if (image.isVectorImage())
        {
            // marginal processing: process each channel independently
            VectorArray<?> array = (VectorArray<?>) image.getData();
            int nChannels = array.channelCount();
            
            // allocate memory for result
            switch (outputTypeIndex)
            {
            case 0:
                result = array.newInstance(array.size());
                break;
            case 1:
                result = Float32VectorArray.create(array.size(), nChannels);
                break;
            case 2:
                result = Float64VectorArray.create(array.size(), nChannels);
                break;
            default:
                throw new RuntimeException("Unknown type index: " + outputTypeIndex);
            }
            
            // iterate over channels of source and target images
            for (int c = 0;c < nChannels; c++)
            {
                ScalarArray<?> source = array.channel(c);
                ScalarArray<?> target = ((VectorArray<?>) result).channel(c);
                
                processScalar(source, target, functionName, value);
            }
        }
        else
        {
            throw new RuntimeException("Can not process array with type: " + image.getData().getClass());
        }
        
        // create result image
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-" + functionName);
		
		// add the image document to GUI
		frame.createImageFrame(resultImage);
	}
	
	private static void processScalar(ScalarArray<?> source, ScalarArray<?> target, String functionName, double value)
	{
        // apply function and store to result
        switch (functionName)
        {
        case "Plus":
            Math.add(source, value, target);
            break;
        case "Minus":
            Math.subtract(source, value, target);
            break;
        case "Times":
            Math.multiply(source, value, target);
            break;
        case "Divides":
            Math.divide(source, value, target);
            break;
        case "Min":
            Math.min(source, value, target);
            break;
        case "Max":
            Math.max(source, value, target);
            break;
        default: throw new RuntimeException("Unknown function name: " + functionName); 
        }

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
