/**
 * 
 */
package imago.gui.plugin.plugin.crop;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoStub;
import net.sci.geom.polygon2d.LinearRing2D;

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
//    private String sliceIndexPattern = "%04d";

    /**
     * Empty constructor.
     */
    public Crop3DRegionInterpolator()
    {
    }

    /**
     * Updates the "interpolatedPolygons" field of the specified crop region 3D.
     * 
     * @param region
     *            the region to update.
     */
    public void interpolatePolygons(Crop3DRegion region)
    {
        region.interpolatedPolygons = interpolatePolygons(region.polygons);
    }
    
    /**
     * Computes interpolated polygons from the original polygons, using Fuchs'
     * algorithm.
     */
    public Map<Integer, LinearRing2D> interpolatePolygons(Map<Integer, LinearRing2D> polygonsNode)
    {
        Map<Integer, LinearRing2D> interpNode = new TreeMap<Integer, LinearRing2D>();
        
        // get iterator on polygon slices
        Collection<Integer> indices = polygonsNode.keySet();
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
        for (int ind : polygonsNode.keySet())
        {
            lastIndex = ind;
        }
        int indexCount = lastIndex - minSliceIndex + 1;
        
        LinearRing2D currentPoly = polygonsNode.get(currentSliceIndex);
        
        // iterate over pairs of indices
        while (sliceIndexIter.hasNext())
        {
            // create shape for current manual polygon (equivalent to interpolated polygon by definition)
            interpNode.put(currentSliceIndex, currentPoly.duplicate());

            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            this.fireStatusChanged(new AlgoEvent(this, "process slice range " + currentSliceIndex + " - " + nextSliceIndex));
            
            // Extract polygon of upper slice
            LinearRing2D nextPoly = polygonsNode.get(nextSliceIndex);
            
            // Create interpolation algorithm
            ParallelPolygonsInterpolator algo = new ParallelPolygonsInterpolator(currentPoly, currentSliceIndex, nextPoly, nextSliceIndex);

            // iterate over slices in-between bottom and upper
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex < nextSliceIndex; sliceIndex++)
            {
                System.out.println("  interpolate slice " + sliceIndex);
                this.fireProgressChanged(new AlgoEvent(this, sliceIndex - minSliceIndex, indexCount));
                
                LinearRing2D interpPoly = algo.interpolate(sliceIndex);
                
                // create shape for interpolated polygon
                interpNode.put(sliceIndex, interpPoly);
            }
            
            // prepare for next pair of indices
            currentSliceIndex = nextSliceIndex;
            currentPoly = nextPoly;
        }

        // create a shape for the last manual polygon (equivalent to interpolated polygon by definition)
        interpNode.put(currentSliceIndex, currentPoly.duplicate());

        this.fireProgressChanged(new AlgoEvent(this, 1, 1));
        
        return interpNode;
    }
}
