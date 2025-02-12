/**
 * 
 */
package imago.plugin.plugin.crop;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import net.sci.array.numeric.UInt8Array2D;
import net.sci.array.numeric.UInt8Array3D;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.StraightLine2D;
import net.sci.geom.geom2d.Vector2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;

/**
 * Virtual array that combines a reference 3D array and a series of crop
 * polygons. This array returns the value of the original array only when the
 * query voxel lies within the polygon of the slice indexed by the Z-coordinate.
 * 
 * This implementation considers polygons provided as map of LinearRing2D
 * instances, indexed with (unsigned) integers keys that correspond to the slice
 * indices.
 * 
 * @author dlegland
 *
 */
public class CroppedUInt8Array3D extends UInt8Array3D
{
    UInt8Array3D refArray;
    Map<Integer, LinearRing2D> cropSlices;
    
    public CroppedUInt8Array3D(UInt8Array3D refArray, Map<Integer, LinearRing2D> cropSlices)
    {
        super(refArray.size(0), refArray.size(1), refArray.size(2));
        this.refArray = refArray;
        this.cropSlices = cropSlices;
    }

    /**
     * Returns the array corresponding to the slice data.
     */
    public UInt8Array2D slice(int sliceIndex)
    {
        // allocate memory for slice
        UInt8Array2D res = UInt8Array2D.create(this.size0, this.size1);
        
        // check node validity
        LinearRing2D ring = cropSlices.get(sliceIndex);
        if (ring == null)
        {
            return res;
        }
        
        // compute bounds in Y direction
        Bounds2D box = ring.bounds();
        int ymin = (int) Math.max(0, Math.ceil(box.yMin()));
        int ymax = (int) Math.min(this.size1, Math.floor(box.yMax()));

        // iterate over lines inside bounding box
        for (int y = ymin; y < ymax; y++)
        {
            StraightLine2D line = new StraightLine2D(new Point2D(0, y), new Vector2D(1, 0));
            Collection<Point2D> points = ring.intersections(line);

            int np = points.size();
            if (np % 2 != 0)
            {
                System.err.println("can not manage odd number of intersections bewteen linear ring and straight line");
                continue;
            }
            
            double[] xCoords = new double[np];

            java.util.Iterator<Point2D> iter = points.iterator();
            for (int i = 0; i < np; i++)
            {
                xCoords[i] = iter.next().x();
            }
            
            Arrays.sort(xCoords);
            for (int i = 0; i < np; i += 2)
            {
                int x0 = (int) Math.max(0, Math.ceil(xCoords[i]));
                int x1 = (int) Math.min(this.size0, Math.floor(xCoords[i+1] + 1));
                for (int x = x0; x < x1; x++)
                {
                    res.setByte(x, y, this.refArray.getByte(x, y, sliceIndex));
                }
            }
        }
        
        return res;
    }

    @Override
    public byte getByte(int x, int y, int z)
    {
        // get crop polygon
        int sliceIndex = z;
        LinearRing2D ring = cropSlices.get(sliceIndex);
        if (ring == null)
        {
            return 0;
        }
        
        // check if the query point is within the polygon defined by the ring
        if (ring.isInside(x, y))
        {
            return this.refArray.getByte(x, y, z);
        }

        // return background value
        return 0;
    }

    @Override
    public void setByte(int x, int y, int z, byte b)
    {
        throw new RuntimeException("Unauthorized operation");
    }
}
