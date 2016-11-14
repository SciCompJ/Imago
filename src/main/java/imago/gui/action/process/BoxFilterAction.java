/**
 * 
 */
package imago.gui.action.process;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.ArrayToArrayOperator;
import net.sci.array.data.FloatArray;
import net.sci.image.Image;

/**
 * Applies box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class BoxFilterAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BoxFilterAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		System.out.println("box filter (generic)");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(this.frame, "Flat Blur");
		for (int d = 0; d < nd; d++)
		{
			gd.addNumericField("Radius dim. " + (d+1), 3, 0);
		}
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		int[] radiusList = new int[nd];
		for (int d = 0; d < nd; d++)
		{
			radiusList[d] = (int) gd.getNextNumber();
		}
		
		// create result image with same size as input array
		FloatArray output = FloatArray.create(array.getSize());

		// create operator and apply
		ArrayToArrayOperator filter = new net.sci.image.process.BoxFilter(radiusList);
		filter.process(array, output);
		
		Image result = new Image(output, image);
		result.setName(image.getName() + "-filt");
		
		// add the image document to GUI
		this.gui.addNewDocument(result);
	}

}
