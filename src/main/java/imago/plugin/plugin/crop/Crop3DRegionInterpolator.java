/**
 * 
 */
package imago.plugin.plugin.crop;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoStub;
import net.sci.geom.geom2d.polygon.LinearRing2D;

/**
 * Algorithm for interpolating polygons from a series of manually selected
 * parallel polygons.
 * 
 * Usage:
 * <pre>
 * {@code
 * Crop3DRegionInterpolator algo = new Crop3DRegionInterpolator();
 * ImageSerialSectionsNode polygonsNode = region.polygons;
 * ImageSerialSectionsNode interpNode = algo.interpolatePolygons(polygonsNode, "interpolated");
 * region.interpolatedPolygons = interpNode; 
 * }
 * </pre>
 * @author dlegland
 *
 */
public class Crop3DRegionInterpolator extends AlgoStub
{
    private String sliceIndexPattern = "%04d";

    /**
     * Empty constructor.
     */
    public Crop3DRegionInterpolator()
    {
    }

    /**
     * Updates the "interpolatedPolygons' field of the specified crop region 3D.
     * 
     * @param region
     *            the region to update.
     */
    public void interpolatePolygons(Crop3DRegion region)
    {
        ImageSerialSectionsNode polygonsNode = region.polygons;
        ImageSerialSectionsNode interpNode = interpolatePolygons(polygonsNode);
        region.interpolatedPolygons = interpNode;
    }
    
    /**
     * Computes interpolated polygons from the original polygons, using Fuchs'
     * algorithm.
     */
    public ImageSerialSectionsNode interpolatePolygons(ImageSerialSectionsNode polygonsNode)
    {
        ImageSerialSectionsNode interpNode = new ImageSerialSectionsNode("interpolated");
        
        // get iterator on polygon slices
        Collection<Integer> indices = polygonsNode.getSliceIndices();
        Iterator<Integer> sliceIndexIter = indices.iterator();
        if (!sliceIndexIter.hasNext())
        {
            return interpNode;
        }
        
        // Extract polygon of bottom slice
        int currentSliceIndex = sliceIndexIter.next();
        int minSliceIndex = currentSliceIndex;
        
        // get last index and indices number
        int lastIndex = minSliceIndex;
        for (int ind : polygonsNode.getSliceIndices())
        {
            lastIndex = ind;
        }
        int indexCount = lastIndex - minSliceIndex + 1;
        
        LinearRing2D currentPoly = getLinearRing(polygonsNode, currentSliceIndex);
        
        // iterate over pairs of indices
        while (sliceIndexIter.hasNext())
        {
            // create shape for current manual polygon (equivalent to interpolated polygon by definition)
            interpNode.addSliceNode(createInterpolatedPolygonNode(currentPoly, currentSliceIndex));

            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            this.fireStatusChanged(new AlgoEvent(this, "process slice range " + currentSliceIndex + " - " + nextSliceIndex));
            
            // Extract polygon of upper slice
            LinearRing2D nextPoly = getLinearRing(polygonsNode, nextSliceIndex);
            
            // Create interpolation algorithm
            ParallelPolygonsInterpolator algo = new ParallelPolygonsInterpolator(currentPoly, currentSliceIndex, nextPoly, nextSliceIndex);

            // iterate over slices in-between bottom and upper
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex < nextSliceIndex; sliceIndex++)
            {
                System.out.println("  interpolate slice " + sliceIndex);
                this.fireProgressChanged(new AlgoEvent(this, sliceIndex - minSliceIndex, indexCount));
                
                LinearRing2D interpPoly = algo.interpolate(sliceIndex);
                
                // create shape for interpolated polygon
                interpNode.addSliceNode(createInterpolatedPolygonNode(interpPoly, sliceIndex));
            }
            
            // prepare for next pair of indices
            currentSliceIndex = nextSliceIndex;
            currentPoly = nextPoly;
        }

        this.fireProgressChanged(new AlgoEvent(this, 1, 1));
        
        // create a shape for the last manual polygon (equivalent to interpolated polygon by definition)
        interpNode.addSliceNode(createInterpolatedPolygonNode(currentPoly, currentSliceIndex));

        return interpNode;
    }
    
    private ImageSliceNode createInterpolatedPolygonNode(LinearRing2D ring, int sliceIndex)
    {
        // compute name (of both shape and slice nodes)
        String sliceName = createSliceName("interp", sliceIndex);

        // create a node for the shape
        ShapeNode shapeNode = new ShapeNode(sliceName, ring);
        shapeNode.getStyle().setColor(Color.MAGENTA);
        shapeNode.getStyle().setLineWidth(1.0);
    
        // create the slice for interpolated version
        ImageSliceNode sliceNode = new ImageSliceNode(sliceName, sliceIndex);
        sliceNode.addNode(shapeNode);
        
        return sliceNode;
    }
    
    /**
     * Computes the name associated to a slice, based on a user-defined prefix
     * and a slice index.
     * 
     * @param prefix
     *            the prefix used to describe the slice.
     * @param sliceIndex
     *            the index of the slice
     * @return the concatenation of the prefix with a 0-padded string
     *         representation of the slice index
     */
    private String createSliceName(String prefix, int sliceIndex)
    {
        return String.format(Locale.US, prefix + sliceIndexPattern, sliceIndex);
    }
    
    /**
     * Retrieve the LinearRing2D geometry at the specified index from the serial
     * sections node.
     * 
     * ImageSerialSectionsNode -> ImageSliceSection -> ShapeNode -> Geometry.
     * 
     * @param node
     *            the node mapping to ImageSliceSections
     * @param index
     *            the index of the slice
     * @return the LinearRing2D geometry contained in the specified slice.
     */
    private LinearRing2D getLinearRing(ImageSerialSectionsNode cropNode, int sliceIndex)
    {
        // retrieve shape node
        ShapeNode shapeNode = (ShapeNode) cropNode.getSliceNode(sliceIndex).children().iterator().next();
        
        // check class
        if (!(shapeNode.getGeometry() instanceof LinearRing2D))
        {
            throw new RuntimeException("Expect crop geometry to be a LinearRing2D");
        }
        
        // return with correct class cast
        return (LinearRing2D) shapeNode.getGeometry();
    }
    
}
