/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.process.filter.BoxMinMaxFilterNaive;

/**
 * Applies box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class ImageBoxMinMaxFilter implements Plugin
{
	public ImageBoxMinMaxFilter()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame)
	{
		System.out.println("box min/max filter");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(frame, "Box Min/Max");
		gd.addChoice("Operation",  new String[]{"Min.",  "Max."}, "Min.");
		for (int d = 0; d < nd; d++)
		{
			gd.addNumericField("Size dim. " + (d+1), 3, 0);
		}
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		boolean minFilter = gd.getNextChoiceIndex() == 0;
		int[] diameters = new int[nd];
		for (int d = 0; d < nd; d++)
		{
			diameters[d] = (int) gd.getNextNumber();
		}

		// create operator box filtering operator
		BoxMinMaxFilterNaive.Type type = minFilter ? BoxMinMaxFilterNaive.Type.MIN : BoxMinMaxFilterNaive.Type.MAX;  
		BoxMinMaxFilterNaive filter = new BoxMinMaxFilterNaive(type, diameters);
		filter.addAlgoListener((ImagoDocViewer) frame); 

		// apply operator on current image
		Image result = filter.process(image);
		
		// choose name of result
		String suffix = minFilter ? "-minFilt" : "-maxFilt";
		result.setName(image.getName() + suffix);
		
		// add the image document to GUI
		frame.getGui().addNewDocument(result);
	}
}
