/**
 * 
 */
package imago.image.plugins.vectorize;

import java.util.ArrayList;
import java.util.Map;

import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.numeric.IntArray;
import net.sci.array.numeric.IntArray2D;
import net.sci.geom.polygon2d.Polygon2D;
import net.sci.image.Image;
import net.sci.image.vectorize.LabelMapBoundaryPolygons;

/**
 * Compute boundary polygons of regions within a binary or label image.
 * 
 * @author David Legland
 *
 */
public class LabelMapToBoundaryPolygons implements FramePlugin
{
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
		if (!(array instanceof IntArray))
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

		// wrap into an image of integers
		IntArray2D<?> labelMap = IntArray2D.wrap((IntArray<?>) array);
		
		// create median box operator
		LabelMapBoundaryPolygons algo = new LabelMapBoundaryPolygons(); 
		Map<Integer, ArrayList<Polygon2D>> labelPolygons = algo.process(labelMap);

		// add to the document
		for (int label : labelPolygons.keySet())
		{
		    for (Polygon2D poly : labelPolygons.get(label))
		    {
		        handle.addShape(new Shape(poly));
		    }
		}
              
		// update viewer associated to the ImageHandle
		handle.notifyImageHandleChange();
	}
}
