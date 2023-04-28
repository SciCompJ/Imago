/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.*;
import imago.gui.frames.ImageFrame;
import net.sci.array.Arrays;
import net.sci.array.binary.BinaryArray;
import net.sci.array.process.binary.LogicalBinaryOperator;
import net.sci.image.Image;

/**
 * Applies a logical operator (OR, AND, XOR) to a pair of binary arrays, and
 * display the result in a new frame.
 * 
 * @author David Legland
 *
 */
public class ImageArrayLogicalBinaryOperator implements FramePlugin
{
    /**
     * The list of functions that can be applied.
     */
    String[] functionNames = new String[]{"OR", "AND", "XOR", "AND NOT"};
    
 	public ImageArrayLogicalBinaryOperator()
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
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        String image1Name = gd.getNextChoice();
		String functionName = gd.getNextChoice();
        String image2Name = gd.getNextChoice();
        
        // retrieve images from names
        Image image1 = frame.getGui().getAppli().getImageHandleFromName(image1Name).getImage();
        if (!image1.isBinaryImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a binary array");
            return;
        }
        Image image2 = frame.getGui().getAppli().getImageHandleFromName(image2Name).getImage();
        if (!image2.isBinaryImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a binary array");
            return;
        }

        // extract arrays
        BinaryArray array1 = (BinaryArray) image1.getData();
        BinaryArray array2 = (BinaryArray) image2.getData();

        // check dimensions
        if (!Arrays.isSameSize(array1, array2))
        {
            ImagoGui.showErrorDialog(frame, "Both image arrays must have same dimensions");
            return;
        }
        

        // Creates the operator, using specialized implementation when available
        LogicalBinaryOperator op;
		switch (functionName)
		{
        case "OR":
            op = LogicalBinaryOperator.OR;
            break;
        case "AND":
            op = LogicalBinaryOperator.AND;
            break;
        case "XOR":
            op = new LogicalBinaryOperator((x, y) -> x ^ y);
            break;
        case "AND NOT":
            op = LogicalBinaryOperator.AND_NOT;
            break;
        default: throw new RuntimeException("Unknown function name: " + functionName); 
		}
		
		// create operator
        op.addAlgoListener(frame);
        
        // run operator
		long t0 = System.nanoTime();
		BinaryArray result = op.process(array1, array2);
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
		frame.getGui().createImageFrame(resultImage);
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
