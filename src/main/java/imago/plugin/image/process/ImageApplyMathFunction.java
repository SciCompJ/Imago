/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
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
 */
public class ImageApplyMathFunction implements Plugin
{
    String[] functionNames = new String[]{"Sqrt", "Log10", "Log2", "Exp", "Cos", "Sin"};
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
		System.out.println("apply math function");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
		}
		ScalarArray<?> inputArray = (ScalarArray<?>) array;

		GenericDialog gd = new GenericDialog(frame, "Apply Math Function");
        gd.addChoice("Function", functionNames, functionNames[0]);
        gd.addChoice("OutputT Type", outputTypeNames, outputTypeNames[0]);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		String functionName = gd.getNextChoice();
		String typeName = gd.getNextChoice();
        
		ScalarArray<?> resultArray;
		switch (typeName)
		{
        case "Same": resultArray = inputArray.newInstance(array.size()); break;
        case "Float32": resultArray = Float32Array.create(array.size()); break;
        case "Float64": resultArray = Float64Array.create(array.size()); break;
        default: throw new RuntimeException("Unknown type name: " + typeName); 
		}
		
		switch (functionName)
		{
        case "Sqrt": inputArray.apply(x -> Math.sqrt(x), resultArray); break;
        case "Log10": inputArray.apply(x -> Math.log10(x), resultArray); break;
        case "Log2": inputArray.apply(x -> Math.log(x), resultArray); break;
        case "Exp": inputArray.apply(x -> Math.exp(x), resultArray); break;
        case "Sin": inputArray.apply(x -> Math.sin(x), resultArray); break;
        case "Cos": inputArray.apply(x -> Math.cos(x), resultArray); break;
        default: throw new RuntimeException("Unknown function name: " + functionName); 
		}

		// apply operator on current image
		Image result = new Image(resultArray, image);
		result.setName(image.getName() + "-" + functionName);
		
		// add the image document to GUI
		frame.getGui().addNewDocument(result);
	}
	
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImagoDocViewer))
            return false;
        
        // check image
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.isScalarImage();
    }
}
