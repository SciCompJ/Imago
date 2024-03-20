/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.plugin.options.SingleValueFunction;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.Float32Array;
import net.sci.array.scalar.Float64Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;

/**
 * Apply a simple math function chosen from a list.
 * 
 * @author David Legland
 *
 * @see ImageApplySingleValueOperator
 * @see ImageApplyMathBinaryOperator
 */
public class ImageApplyMathFunction implements FramePlugin
{
    String[] outputTypeNames = new String[]{"Same", "Float32", "Float64"};
    
	public ImageApplyMathFunction()
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
		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
		}
		ScalarArray<?> inputArray = (ScalarArray<?>) array;

        // create dialog
		GenericDialog gd = new GenericDialog(frame, "Apply Math Function");
        gd.addEnumChoice("Function", SingleValueFunction.class, SingleValueFunction.SQRT);
        gd.addChoice("Output Type", outputTypeNames, outputTypeNames[0]);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		SingleValueFunction fun = (SingleValueFunction) gd.getNextEnumChoice();
		String typeName = gd.getNextChoice();
        
        // create result array
        ScalarArray<?> resultArray = switch (typeName)
        {
            case "Same" -> inputArray.newInstance(array.size());
            case "Float32" -> Float32Array.create(array.size());
            case "Float64" -> Float64Array.create(array.size());
            default -> throw new RuntimeException("Unknown type name: " + typeName);
        };

		// apply function to input array and stores in result array
		inputArray.apply(fun.getFunction(), resultArray);

		// create result image
		Image result = new Image(resultArray, image);
		result.setName(image.getName() + "-" + fun.toString());
		
		// add the image document to GUI
		ImageFrame.create(result, frame);
	}
	
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.isScalarImage();
    }
}
