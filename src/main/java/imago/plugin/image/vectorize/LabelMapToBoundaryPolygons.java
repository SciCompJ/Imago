/**
 * 
 */
package imago.plugin.image.vectorize;

import java.util.ArrayList;
import java.util.Map;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import net.sci.array.Array;
import net.sci.array.scalar.IntArray;
import net.sci.array.scalar.IntArray2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
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
	public LabelMapToBoundaryPolygons()
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
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
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
		        doc.addShape(new Shape(poly));
		    }
		}
                
        // TODO: maybe propagating events would be better
        ImageFrame viewer = (ImageFrame) frame;
        viewer.repaint(); 
	}

}
