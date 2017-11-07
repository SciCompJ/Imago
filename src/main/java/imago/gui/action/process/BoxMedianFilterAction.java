/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.process.filter.BoxMedianFilter;

/**
 * Applies median box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class BoxMedianFilterAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BoxMedianFilterAction(ImagoFrame frame, String name)
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
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("median box filter (generic)");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(this.frame, "Median Filter");
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
		
		// create median box operator
		BoxMedianFilter filter = new BoxMedianFilter(radiusList);
		filter.addAlgoListener((ImagoDocViewer) this.frame);
		
		// apply operator on current image
		Image result = filter.process(image);
		result.setName(image.getName() + "-medFilt");
		
		// add the image document to GUI
		this.gui.addNewDocument(result);
	}

}
