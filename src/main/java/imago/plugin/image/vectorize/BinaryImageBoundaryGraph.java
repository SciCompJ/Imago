/**
 * 
 */
package imago.plugin.image.vectorize;

import imago.app.ImagoDoc;
import imago.app.shape.ImagoShape;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.data.BinaryArray;
import net.sci.array.data.scalar2d.BinaryArray2D;
import net.sci.geom.geom2d.graph.SimpleGraph2D;
import net.sci.image.Image;
import net.sci.image.vectorize.BinaryImage2DBoundaryGraph;

/**
 * Compute boundary graph of a binary image.
 * 
 * @author David Legland
 *
 */
public class BinaryImageBoundaryGraph implements Plugin
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
		System.out.println("boundary graph of binary image");

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
		if (nd != 2)
		{
			frame.showErrorDialog("Can process only 2D", "Dimensionality Error");
			return;
		}

		// TODO: use BinaryArray2D.wrap(...)
		BinaryArray2D binary = (BinaryArray2D) array;
		
		// create median box operator
		SimpleGraph2D graph = new BinaryImage2DBoundaryGraph().process(binary);

		// add to the document
        doc.addShape(new ImagoShape(graph));
                
        // TODO: maybe propagating events would be better
        ImagoDocViewer viewer = (ImagoDocViewer) frame;
        viewer.repaint(); 
	}

}