/**
 * 
 */
package imago.plugin.plugin.crop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import imago.app.ImageHandle;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ShapeNode;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.StackSliceViewer;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.array.scalar.Scalar;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.geom.geom2d.Box2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.StraightLine2D;
import net.sci.geom.geom2d.Vector2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;
import net.sci.image.Image;

/**
 * Crop image from the set of crop polygons.
 * 
 * @author David Legland
 *
 */
public class Crop3D_CropImage extends AlgoStub implements Plugin
{
	public Crop3D_CropImage()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
		System.out.println("crop3d - crop image");

		// Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
                
        ImageViewer viewer = iframe.getImageView();
        if (!(viewer instanceof StackSliceViewer))
        {
            System.out.println("requires an instance of stack slice viewer");
            return;
        }

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();
		
		// get input node reference
        ImageHandle handle = iframe.getImageHandle();
        ImageSerialSectionsNode interpNode = Crop3D.getInterpolatedPolygonsNode(handle);
        if (interpNode.getSliceIndices().isEmpty())
        {
            return;
        }
        
        this.addAlgoListener((ImageFrame) frame);

        Array<?> result = process((ScalarArray<?>) array, interpNode);
        
        Image resImage = new Image(result, image);
        resImage.setName(image.getName() + "-crop");
		
		// add the image document to GUI
		frame.createImageFrame(resImage);
	}
	
	public <T extends Scalar> ScalarArray<T> process(ScalarArray<T> array, ImageSerialSectionsNode interpNode)
	{
		if (array.dimensionality() != 3)
		{
		    throw new RuntimeException("Requires an image containing 3D Array");
		}
		ScalarArray3D<T> array3d = ScalarArray3D.wrap(array);

        
        // Create empty result array
		ScalarArray3D<T> resArray = ScalarArray3D.wrap(array3d.newInstance(array.size()));
        
        // size of array
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        this.fireStatusChanged(this, "crop 3D image");
        for (int sliceIndex : interpNode.getSliceIndices())
        {
//        	System.out.println("crop slice " + sliceIndex);
            this.fireProgressChanged(this, sliceIndex, array.size(2));
        	
        	// get 2D view on array slices
        	ScalarArray2D<T> slice = (ScalarArray2D<T>) array3d.slice(sliceIndex);
        	ScalarArray2D<T> resSlice = (ScalarArray2D<T>) resArray.slice(sliceIndex);
        	
            // get crop polygon
            ShapeNode shapeNode = (ShapeNode) interpNode.getSliceNode(sliceIndex).children().iterator().next();
            LinearRing2D ring = (LinearRing2D) shapeNode.getGeometry();
            
            // compute bounds in Y direction
            Box2D box = ring.boundingBox();
            int ymin = (int) Math.max(0, Math.ceil(box.getYMin()));
            int ymax = (int) Math.min(sizeY, Math.floor(box.getYMax()));

            // iterate over lines inside bounding box
            for (int y = ymin; y < ymax; y++)
            {
                StraightLine2D line = new StraightLine2D(new Point2D(0, y), new Vector2D(1, 0));
            	Collection<Point2D> points = ring.intersections(line);
            	points = sortPointsByX(points);

            	int np = points.size();
//        		System.out.print("  y=" + y + ", " + np + " points");

            	if (np % 2 != 0)
            	{
            		System.err.println("can not manage odd number of intersections bewteen linear ring and straight line");
            		continue;
            	}
            	
            	Iterator<Point2D> iter = points.iterator();
            	while (iter.hasNext())
            	{
            		int x0 = (int) Math.max(0, Math.ceil(iter.next().getX()));
            		int x1 = (int) Math.min(sizeX, Math.floor(iter.next().getX() + 1));
//            		System.out.print(", x0=" + x0 + " x1=" + x1);
            		for (int x = x0; x < x1; x++)
            		{
                		resSlice.setValue(slice.getValue(x, y), x, y);
            		}
            	}
//        		System.out.println("");
            }
//    		System.out.println("");
        }

        this.fireStatusChanged(this, "");
        this.fireProgressChanged(this, 0, 0);
       
        return resArray;
	}
	
	private ArrayList<Point2D> sortPointsByX(Collection<Point2D> points)
	{
		ArrayList<Point2D> res = new ArrayList<Point2D>(points.size());
		res.addAll(points);
		Collections.sort(res, new Comparator<Point2D>()
		{
			@Override
			public int compare(Point2D p0, Point2D p1)
			{
				// compare X first
				double dx = p0.getX() - p1.getX();
				if (dx < 0) return -1;
				if (dx > 0) return +1;
				// add y comparison
				double dy = p0.getY() - p1.getY();
				if (dy < 0) return -1;
				if (dy > 0) return +1;
				// same point
				return 0;
			}
		});
		return res;
	}
}
