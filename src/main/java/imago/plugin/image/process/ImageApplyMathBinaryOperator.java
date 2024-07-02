/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.gui.*;
import imago.gui.image.ImageFrame;
import imago.plugin.options.ValuePairFunction;
import net.sci.array.Arrays;
import net.sci.array.numeric.Float32Array;
import net.sci.array.numeric.Float64Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.process.math.MathBinaryOperator;
import net.sci.image.Image;

/**
 * Apply a simple math function between two image arrays.
 * 
 * @author David Legland
 *
 * @see ImageApplyMathFunction
 * @see ImageApplySingleValueOperator
 * @see ImageApplyLogicalBinaryOperator
 */
public class ImageApplyMathBinaryOperator implements FramePlugin
{
    /**
     * Control the type of output array.
     */
    String[] outputTypeNames = new String[]{"Same as Image 1", "Same as Image 2", "Float32", "Float64"};
    
	public ImageApplyMathBinaryOperator()
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
        ImagoApp app = frame.getGui().getAppli();
        String[] imageNames = ImageHandle.getAllNames(app).toArray(new String[]{});
		int index1 = 0;
		if (frame instanceof ImageFrame)
		{
		    String imageName = ((ImageFrame) frame).getImageHandle().getName();
		    index1 = findStringIndex(imageName, imageNames);
		}
		int index2 = (int) java.lang.Math.min(index1 + 1, imageNames.length-1);

		GenericDialog gd = new GenericDialog(frame, "Math Binary Operator");
		gd.addChoice("Image 1", imageNames, imageNames[index1]);
        gd.addEnumChoice("Operation", ValuePairFunction.class, ValuePairFunction.PLUS);
        gd.addChoice("Image 2", imageNames, imageNames[index2]);
        gd.addChoice("Output Type", outputTypeNames, outputTypeNames[0]);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        String image1Name = gd.getNextChoice();
        ValuePairFunction opOption = (ValuePairFunction) gd.getNextEnumChoice();
        String image2Name = gd.getNextChoice();
		int outputTypeIndex = gd.getNextChoiceIndex();
        
        // retrieve images from names
        Image image1 = ImageHandle.findFromName(frame.getGui().getAppli(), image1Name).getImage();
        if (!image1.isScalarImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
        }
        Image image2 = ImageHandle.findFromName(frame.getGui().getAppli(), image2Name).getImage();
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
        ScalarArray<?> result = switch (outputTypeIndex)
        {
            case 0 -> array1.newInstance(array1.size());
            case 1 -> array2.newInstance(array1.size());
            case 2 -> Float32Array.create(array1.size());
            case 3 -> Float64Array.create(array1.size());
            default -> throw new IllegalArgumentException("Unexpected value: " + outputTypeIndex);
        };

		// create array operator based on lambda stored in operator option 
		MathBinaryOperator op = new MathBinaryOperator(opOption.getFunction());
        op.addAlgoListener(frame);
        
        // run operator
		long t0 = System.nanoTime();
        op.process(array1, array2, result);
        long t1 = System.nanoTime();
        
        // display elapsed time
		if (frame instanceof ImageFrame)
        {
		    double dt = (t1 - t0) / 1_000_000.0;
           ((ImageFrame) frame).showElapsedTime(opOption.toString(), dt, image1);
        }
		
		// create and display result image
		Image resultImage = new Image(result, image1);
		resultImage.setName(String.format("%s(%s, %s)", opOption.toString(), image1.getName(), image2.getName()));
        
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
