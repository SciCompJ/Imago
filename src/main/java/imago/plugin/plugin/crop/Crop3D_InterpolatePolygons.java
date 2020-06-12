/**
 * 
 */
package imago.plugin.plugin.crop;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JOptionPane;

import imago.app.ImageHandle;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.StackSliceViewer;
import net.sci.algo.AlgoEvent;
import net.sci.array.Array;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.Vector2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
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
		ImageHandle doc = iframe.getImageHandle();
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
        
        ImageSerialSectionsNode polyNode = Crop3D.getPolygonsNode(handle);
        if (polyNode.isLeaf())
        {
			JOptionPane.showMessageDialog(frame.getWidget(),
					"Requires the frame to contains valid Crop3D Polygons",
					"Crop3D Error", JOptionPane.INFORMATION_MESSAGE);
        	return;
        }
        
        
        // clear output nodes
        ImageSerialSectionsNode smoothNode = Crop3D.getSmoothPolygonsNode(handle);
        smoothNode.clear();
        
        // iterate over polygons to create a smoothed version
        for (ImageSliceNode sliceNode : polyNode.children())
        {
            int sliceIndex = sliceNode.getSliceIndex(); 
        	System.out.println("smooth polygon on slice " + sliceIndex);

            ShapeNode shapeNode = (ShapeNode) sliceNode.children().iterator().next();
            LinearRing2D ring = (LinearRing2D) shapeNode.getGeometry();
            LinearRing2D ring2 = ring.resampleBySpacing(2.0); // every 2 pixels
            ring2 = ring2.smooth(7);

            String sliceName = String.format(Locale.US, "smooth%0" + nDigits + "d", sliceIndex);
            ShapeNode shapeNode2 = new ShapeNode(sliceName, ring2);
            shapeNode2.getStyle().setColor(Color.GREEN);
            shapeNode2.getStyle().setLineWidth(0.5);
            
            // create the slice for smooth version
            ImageSliceNode sliceNode2 = new ImageSliceNode(sliceName, sliceIndex);
            sliceNode2.addNode(shapeNode2);
            
            smoothNode.addSliceNode(sliceNode2);
        }

        // clear interpolated polygons node
        ImageSerialSectionsNode interpNode = Crop3D.getInterpolatedPolygonsNode(handle);
        interpNode.clear();

        Collection<Integer> indices = polyNode.getSliceIndices();
        Iterator<Integer> sliceIndexIter = indices.iterator();
        if (!sliceIndexIter.hasNext())
        {
            return;
        }
        
        // Extract polygon of bottom slice
        int currentSliceIndex = sliceIndexIter.next();
        int minSliceIndex = currentSliceIndex;
        
        // get last index and indices number
        int lastIndex = minSliceIndex;
        for (int ind : polyNode.getSliceIndices())
        {
        	lastIndex = ind;
        }
        int indexCount = lastIndex - minSliceIndex + 1;
        
        ShapeNode shapeNode = (ShapeNode) smoothNode.getSliceNode(currentSliceIndex).children().iterator().next();
        LinearRing2D currentPoly = (LinearRing2D) shapeNode.getGeometry();
//        // resample and smooth the polygon
//        currentPoly = currentPoly.resampleBySpacing(2.0).smooth(7);
        
        // iterate over pairs of indices
        while (sliceIndexIter.hasNext())
        {
            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            iframe.algoStatusChanged(new AlgoEvent(this, "process slice range " + currentSliceIndex + " - " + nextSliceIndex));
        	
            // Extract polygon of upper slice
            shapeNode = (ShapeNode) smoothNode.getSliceNode(nextSliceIndex).children().iterator().next();
            LinearRing2D nextPolyRef = (LinearRing2D) shapeNode.getGeometry();
            
            // compute projection points of current poly over next poly
            LinearRing2D nextPoly = projectRingVerticesNormal(currentPoly, nextPolyRef);
            // smooth and re-project to have vertices distributed more regularly along target polygon
            nextPoly = projectRingVerticesNormal(nextPoly.smooth(15), nextPolyRef);

            // create shape for interpolated polygon
        	interpNode.addSliceNode(createInterpNode(currentPoly, currentSliceIndex, nDigits));

            // iterate over slices in-between bottom and upper
            double dz = nextSliceIndex - currentSliceIndex;
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex < nextSliceIndex; sliceIndex++)
            {
            	System.out.println("  process slice " + sliceIndex);
            	iframe.algoProgressChanged(new AlgoEvent(this, sliceIndex - minSliceIndex, indexCount));
            	
            	double t0 = ((double) (sliceIndex - currentSliceIndex)) / dz;
                LinearRing2D interpPoly = interpolateRings(currentPoly, nextPoly, t0);
            	
                // create shape for interpolated polygon
            	interpNode.addSliceNode(createInterpNode(interpPoly, sliceIndex, nDigits));
            }
            
            // prepare for next pair of indices
            currentSliceIndex = nextSliceIndex;
            currentPoly = nextPoly;
        }

        iframe.algoProgressChanged(new AlgoEvent(this, 1, 1));
        
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
        shapeNode.getStyle().setLineWidth(1.0);

        // create the slice for interpolated version
        ImageSliceNode sliceNode = new ImageSliceNode(sliceName, sliceIndex);
        sliceNode.addNode(shapeNode);
        
        return sliceNode;
	}
	
//	private LinearRing2D projectRingVertices(LinearRing2D sourceRing, LinearRing2D targetRing)
//	{
//        // compute translation from current polygon to next polygon
//        Point2D currentCentroid = Polygon2D.convert(sourceRing).centroid();
//        Point2D nextCentroid = Polygon2D.convert(targetRing).centroid();
//        Vector2D centroidShift = new Vector2D(currentCentroid, nextCentroid);
//                    
//        // compute projection points of current poly over next poly
//        int nv = sourceRing.vertexNumber();
//        LinearRing2D nextPoly = new LinearRing2D(nv);
//        for (Point2D point : sourceRing.vertexPositions())
//        {
//        	Point2D point2 = point.plus(centroidShift);
//        	nextPoly.addVertex(targetRing.projection(point2));
//        }
//        
//        return nextPoly;
//	}
	
	private LinearRing2D projectRingVerticesNormal(LinearRing2D sourceRing, LinearRing2D targetRing)
	{
//        // compute translation from current polygon to next polygon
//        Point2D sourceCentroid = Polygon2D.convert(sourceRing).centroid();
//        Point2D targetCentroid = Polygon2D.convert(targetRing).centroid();
//        Vector2D centroidShift = new Vector2D(sourceCentroid, targetCentroid);

        int nv = sourceRing.vertexNumber();

        // compute normals to edges of source ring
        ArrayList<Vector2D> sourceEdgeNormals = new ArrayList<Vector2D>(nv);
        for (Polyline2D.Edge edge : sourceRing.edges())
        {
        	Vector2D tangent = new Vector2D(edge.source().position(), edge.target().position());
        	sourceEdgeNormals.add(tangent.normalize().rotate90(-1));
        }

        // compute vertex normals of source ring
        ArrayList<Vector2D> vertexNormals = new ArrayList<Vector2D>(nv);
        for (int i = 0; i < nv; i++)
        {
        	Vector2D normal1 = sourceEdgeNormals.get((i + nv - 1) % nv);
        	Vector2D normal2 = sourceEdgeNormals.get(i);
        	vertexNormals.add(normal1.plus(normal2).times(0.5));
        }
        
        // compute normals to edges of target ring
        ArrayList<Vector2D> edgeNormals = new ArrayList<Vector2D>(targetRing.vertexNumber());
        for (Polyline2D.Edge edge : targetRing.edges())
        {
        	Vector2D tangent = new Vector2D(edge.source().position(), edge.target().position());
        	edgeNormals.add(tangent.rotate90(-1));
        }
                    
        // compute projection points of current poly over next poly
        LinearRing2D nextPoly = new LinearRing2D(nv);
        for (int iv = 0; iv < nv; iv++)
        {
        	Point2D point = sourceRing.vertexPosition(iv);
            double x = point.getX();
            double y = point.getY();
//            double x = point.getX() + centroidShift.getX();
//            double y = point.getY() + centroidShift.getY();
        	
    		double dist, minDist = Double.POSITIVE_INFINITY;
    		Point2D proj = targetRing.vertexPosition(0);
    		
    		for (int iEdge = 0; iEdge < targetRing.vertexNumber(); iEdge++)
    		{
    			// do not process edges whose normal is opposite to vertex
    			if (edgeNormals.get(iEdge).dotProduct(vertexNormals.get(iv)) < 0)
    			{
    				continue;
    			}
    			
    			LineSegment2D seg = targetRing.edge(iEdge).curve();
    			dist = seg.distance(x, y);
    			if (dist < minDist)
    			{
    				minDist = dist;
    				proj = seg.projection(point);
    			}
    		}
    		
    		nextPoly.addVertex(proj);
        }
        
        return nextPoly;
	}
	
	private LinearRing2D interpolateRings(LinearRing2D ring0, LinearRing2D ring1, double t)
	{
    	double t0 = t;
    	double t1 = 1 - t0;
    	
    	int nv = ring0.vertexNumber();
    	LinearRing2D interpPoly = new LinearRing2D(nv);
    	for (int iv = 0; iv < nv; iv++)
    	{
    		Point2D p1 = ring0.vertexPosition(iv);
    		Point2D p2 = ring1.vertexPosition(iv);
    		
    		double x = p1.getX() * t1 + p2.getX() * t0;
    		double y = p1.getY() * t1 + p2.getY() * t0;
    		interpPoly.addVertex(new Point2D(x, y));
    	}

    	return interpPoly;
	}
	
}
