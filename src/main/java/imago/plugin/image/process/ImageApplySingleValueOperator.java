/**
 * 
 */
package imago.plugin.image.process;

import java.util.function.UnaryOperator;

import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.plugin.options.ValuePairFunction;
import net.sci.array.Array;
import net.sci.array.numeric.Float32Array;
import net.sci.array.numeric.Float32VectorArray;
import net.sci.array.numeric.Float64Array;
import net.sci.array.numeric.Float64VectorArray;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;

/**
 * Combines an image with a scalar value to create a new image.
 * 
 * @author David Legland
 *
 * @see ImageApplyMathFunction
 * @see ImageApplyMathBinaryOperator
 */
public class ImageApplySingleValueOperator implements FramePlugin
{
    /**
     * Control the type of output array.
     */
    String[] outputTypeNames = new String[]{"Same as Input", "Float32", "Float64"};
    
	public ImageApplySingleValueOperator()
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
		int index = 0;
		if (frame instanceof ImageFrame)
		{
		    String imageName = ((ImageFrame) frame).getImageHandle().getName();
		    index = findStringIndex(imageName, imageNames);
		}

		GenericDialog gd = new GenericDialog(frame, "Math Operator");
		gd.addChoice("Image", imageNames, imageNames[index]);
        gd.addEnumChoice("Operation", ValuePairFunction.class, ValuePairFunction.PLUS);
        gd.addNumericField("Value", 1.0, 2, "The scalar value to use as operand");
        gd.addChoice("Output Type", outputTypeNames, outputTypeNames[0]);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        String imageName = gd.getNextChoice();
        ValuePairFunction op = (ValuePairFunction) gd.getNextEnumChoice();
        double value = gd.getNextNumber();
		int outputTypeIndex = gd.getNextChoiceIndex();
        
		// identify source image
        Image image = ImageHandle.findFromName(frame.getGui().getAppli(), imageName).getImage();
        
        // transform binary operator into unary operator by fixing constant
        UnaryOperator<Double> fun = (x) -> op.getFunction().apply(x, value);

        // dispatch processing depending on if image is scalar or vector
        Array<?> result = null;
        if (image.isScalarImage())
        {
            ScalarArray<?> array = (ScalarArray<?>) image.getData();
            
            // allocate output array
            result = switch (outputTypeIndex)
            {
                case 0 -> array.newInstance(array.size());
                case 1 -> Float32Array.create(array.size());
                case 2 -> Float64Array.create(array.size());
                default -> throw new IllegalArgumentException("Unexpected value: " + outputTypeIndex);
            };
    		
            array.apply(fun, (ScalarArray<?>) result);
        }
        else if (image.isVectorImage())
        {
            // marginal processing: process each channel independently
            VectorArray<?,?> array = (VectorArray<?,?>) image.getData();
            int nChannels = array.channelCount();
            
            // allocate output array
            result = switch (outputTypeIndex)
            {
                case 0 -> array.newInstance(array.size());
                case 1 -> Float32VectorArray.create(array.size(), nChannels);
                case 2 -> Float64VectorArray.create(array.size(), nChannels);
                default -> throw new IllegalArgumentException("Unexpected value: " + outputTypeIndex);
            };
            
            // iterate over channels of source and target images
            for (int c = 0; c < nChannels; c++)
            {
                ScalarArray<?> source = array.channel(c);
                ScalarArray<?> target = ((VectorArray<?,?>) result).channel(c);
                
                source.apply(fun, target);
            }
        }
        else
        {
            throw new RuntimeException("Can not process array with type: " + image.getData().getClass());
        }
        
        // create result image
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-" + op.toString());
		
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
