/**
 * 
 */
package imago.image.plugins.vectorize;

import imago.app.shape.Shape;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.geom.graph.Graph2D;
import net.sci.image.Image;
import net.sci.image.vectorize.BinaryImage2DBoundaryGraph;

/**
 * Compute boundary graph of a binary image.
 * 
 * @author David Legland
 *
 */
public class BinaryImageBoundaryGraph implements FramePlugin
{
	public BinaryImageBoundaryGraph()
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
		ImageHandle handle = ((ImageFrame) frame).getImageHandle();
		Image image	= handle.getImage();
		Array<?> array = image.getData();
		if (!(array instanceof BinaryArray))
		{
			frame.showErrorDialog("Requires a binary image input", "Data Type Error");
			return;
		}

		int nd = array.dimensionality();
		if (nd != 2)
		{
			frame.showErrorDialog("Can process only 2D", "Dimensionality Error");
			return;
		}

		// wrap into a binary 2D
		BinaryArray2D binary = BinaryArray2D.wrap((BinaryArray) array);
		
		// create median box operator
		Graph2D graph = new BinaryImage2DBoundaryGraph().process(binary);

		// add to the document
        handle.addShape(new Shape(graph));
        handle.notifyImageHandleChange();
	}
}
