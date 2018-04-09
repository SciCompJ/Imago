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
import net.sci.image.process.filter.BoxVarianceFilter;

/**
 * Applies variance box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class ImageBoxVarianceFilter implements Plugin
{
	public ImageBoxVarianceFilter()
	{
	}

	@Override
	public void run(ImagoFrame frame)
	{
		System.out.println("variance box filter (generic)");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(frame, "Variance Filter");
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
		int[] diameters = new int[nd];
		for (int d = 0; d < nd; d++)
		{
			diameters[d] = (int) gd.getNextNumber();
		}
		
		// create median box operator
		BoxVarianceFilter filter = new BoxVarianceFilter(diameters);
		filter.addAlgoListener((ImagoDocViewer) frame); 
		
		// apply operator on current image
		Image result = filter.process(image);
		result.setName(image.getName() + "-varFilt");
		
		// add the image document to GUI
		frame.getGui().addNewDocument(result);
	}

}
