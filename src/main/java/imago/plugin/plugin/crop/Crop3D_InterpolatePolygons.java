/**
 * 
 */
package imago.plugin.plugin.crop;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import imago.app.ImageHandle;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.StackSliceViewer;
import net.sci.array.Array;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;
import net.sci.image.Image;

/**
 * Creates a new shape from the current selection and add it to the current
 * image handle.
 * 
 * @author David Legland
 *
 */
public class Crop3D_InterpolatePolygons implements Plugin
{
	public Crop3D_InterpolatePolygons()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
		System.out.println("crop3d - interpolate polygons");

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
		
		// number of digits for creating slice names
		int nDigits = (int) Math.ceil(Math.log10(array.size(2)));
        
		if (array.dimensionality() != 3)
		{
		    throw new RuntimeException("Requires an image containing 3D Array");
		}

		// get input and output node references
        ImageHandle handle = iframe.getImageHandle();
        ImageSerialSectionsNode smoothNode = Crop3D.getSmoothPolygonsNode(handle);
        ImageSerialSectionsNode interpNode = Crop3D.getInterpolatedPolygonsNode(handle);
        interpNode.clear();

        if (smoothNode.isLeaf())
        {
            System.out.println("smooth node is empty");
            return;
        }
        
        Collection<Integer> indices = smoothNode.getSliceIndices();
        // TODO:assume indices are ordered
        Iterator<Integer> sliceIndexIter = indices.iterator();
        if (!sliceIndexIter.hasNext())
        {
            return;
        }
        
        // Extract polygon of bottom slice
        int currentSliceIndex = sliceIndexIter.next();
        ShapeNode shapeNode = (ShapeNode) smoothNode.getSliceNode(currentSliceIndex).children().iterator().next();
        LinearRing2D currentPoly = (LinearRing2D) shapeNode.getGeometry();
        
        // iterate over pairs of indices
        while (sliceIndexIter.hasNext())
        {
            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            
            // Extract polygon of upper slice
            shapeNode = (ShapeNode) smoothNode.getSliceNode(nextSliceIndex).children().iterator().next();
            LinearRing2D nextPolyRef = (LinearRing2D) shapeNode.getGeometry();
            
            // compute projection points of current poly over next poly
            int nv = currentPoly.vertexNumber();
            LinearRing2D nextPoly = new LinearRing2D(nv);
            for (Point2D point : currentPoly.vertexPositions())
            {
            	nextPoly.addVertex(nextPolyRef.projection(point));
            }

            // create shape for interpolated polygon
        	interpNode.addSliceNode(createInterpNode(currentPoly, currentSliceIndex, nDigits));

            // iterate over slices in-between bottom and upper
            double dz = nextSliceIndex - currentSliceIndex;
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex < nextSliceIndex; sliceIndex++)
            {
            	System.out.println("  process slice " + sliceIndex);
            	
            	double t0 = ((double) (sliceIndex - currentSliceIndex)) / dz;
            	double t1 = 1 - t0;
            	
            	LinearRing2D interpPoly = new LinearRing2D(nv);
            	for (int iv = 0; iv < nv; iv++)
            	{
            		Point2D p1 = currentPoly.vertexPosition(iv);
            		Point2D p2 = nextPoly.vertexPosition(iv);
            		
            		double x = p1.getX() * t1 + p2.getX() * t0;
            		double y = p1.getY() * t1 + p2.getY() * t0;
            		interpPoly.addVertex(new Point2D(x, y));
            	}
            	
                // create shape for interpolated polygon
            	interpNode.addSliceNode(createInterpNode(interpPoly, sliceIndex, nDigits));
            }
            
            // prepare for next pair of indices
            currentSliceIndex = nextSliceIndex;
            currentPoly = nextPoly;
        }

        // create shape for interpolated polygon
        interpNode.addSliceNode(createInterpNode(currentPoly, currentSliceIndex, nDigits));

    	// need to call this to update items to display 
        viewer.refreshDisplay(); 
        
        viewer.repaint();
	}
	
	private ImageSliceNode createInterpNode(LinearRing2D ring, int sliceIndex, int nDigits)
	{
        // create a node for the shape
        String sliceName = String.format(Locale.US, "interp%0" + nDigits + "d", sliceIndex);
        ShapeNode shapeNode = new ShapeNode(sliceName, ring);
        shapeNode.getStyle().setColor(Color.MAGENTA);
        shapeNode.getStyle().setLineWidth(2);

        // create the slice for interpolated version
        ImageSliceNode sliceNode = new ImageSliceNode(sliceName, sliceIndex);
        sliceNode.addNode(shapeNode);
        
        return sliceNode;
	}
}
