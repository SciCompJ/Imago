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
import net.sci.array.data.BinaryArray;
import net.sci.array.type.RGB8;
import net.sci.image.ColorMaps;
import net.sci.image.Image;
import net.sci.image.binary.BinaryImages;

/**
 * Connected component labeling of a binary image.
 * 
 * @author David Legland
 *
 */
public class BinaryImageConnectedComponentsLabeling implements Plugin
{
	public BinaryImageConnectedComponentsLabeling()
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
		System.out.println("connected components labeling");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();
		if (!(array instanceof BinaryArray))
		{
			frame.showErrorDialog("Requires a binary image input", "Data Type Error");
			return;
		}

		int nd = array.dimensionality();
		if (nd != 2 && nd != 3)
		{
			frame.showErrorDialog("Can process only 2D and 3D images", "Dimensionality Error");
			return;
		}
		
		GenericDialog gd = new GenericDialog(frame, "CC Labeling");
		if (nd == 2)
		{
			gd.addChoice("Connectivity: ", new String[]{"4", "8"}, "4");
		}
		else if (nd == 3)
		{
			gd.addChoice("Connectivity: ", new String[]{"6", "26"}, "6");
		}
		gd.addChoice("Output Type: ", new String[]{"8-bits", "16-bits", "32-bits"}, "16-bits");
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		int connIndex = gd.getNextChoiceIndex();
		int conn = nd == 2 ? (connIndex == 0 ? 4 : 8) : (connIndex == 0 ? 6 : 26);
		int bitDepthIndex = gd.getNextChoiceIndex();
		int[] bitDepths = new int[]{8, 16, 32};
		int bitDepth = bitDepths[bitDepthIndex];
		
		// apply connected components labeling
		Image result = BinaryImages.componentsLabeling(image, conn, bitDepth);
		
		// compute JET lut by default
		// TODO: update by scaling?
		int nColors = (int) Math.min(result.getDisplayRange()[1], 255);
		result.setColorMap(ColorMaps.JET.createColorMap(nColors));
		result.setBackgroundColor(RGB8.WHITE);
		
		// add the image document to GUI
		frame.getGui().addNewDocument(result);
	}

}
