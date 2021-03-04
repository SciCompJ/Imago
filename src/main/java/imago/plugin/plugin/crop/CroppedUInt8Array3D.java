/**
 * 
 */
package imago.plugin.plugin.crop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import net.sci.array.scalar.UInt8Array2D;
import net.sci.array.scalar.UInt8Array3D;
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
 * @author dlegland
 *
 */
public class CroppedUInt8Array3D extends UInt8Array3D
{
    UInt8Array3D refArray;
    ImageSerialSectionsNode cropSlices;
    
    public CroppedUInt8Array3D(UInt8Array3D refArray, ImageSerialSectionsNode cropSlices)
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
        ImageSliceNode node = cropSlices.getSliceNode(sliceIndex);
        if (node.isLeaf())
        {
            return res;
        }
        
        // get crop polygon
        ShapeNode shapeNode = (ShapeNode) node.children().iterator().next();
        if (!(shapeNode.getGeometry() instanceof LinearRing2D))
        {
            throw new RuntimeException("Expect crop geometry to be a LinearRing2D");
        }
        LinearRing2D ring = (LinearRing2D) shapeNode.getGeometry();
        
        // compute bounds in Y direction
        Bounds2D box = ring.bounds();
        int ymin = (int) Math.max(0, Math.ceil(box.getYMin()));
        int ymax = (int) Math.min(this.size1, Math.floor(box.getYMax()));

        // iterate over lines inside bounding box
        for (int y = ymin; y < ymax; y++)
        {
            StraightLine2D line = new StraightLine2D(new Point2D(0, y), new Vector2D(1, 0));
            Collection<Point2D> points = ring.intersections(line);
            points = sortPointsByX(points);

            int np = points.size();
            if (np % 2 != 0)
            {
                System.err.println("can not manage odd number of intersections bewteen linear ring and straight line");
                continue;
            }
            
            java.util.Iterator<Point2D> iter = points.iterator();
            while (iter.hasNext())
            {
                int x0 = (int) Math.max(0, Math.ceil(iter.next().getX()));
                int x1 = (int) Math.min(this.size0, Math.floor(iter.next().getX() + 1));
                for (int x = x0; x < x1; x++)
                {
                    res.setByte(x, y, this.refArray.getByte(x, y, sliceIndex));
                }
            }
        }
        
        return res;
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


    @Override
    public byte getByte(int... pos)
    {
        // get crop polygon
        int sliceIndex = pos[2];
        ImageSliceNode node = cropSlices.getSliceNode(sliceIndex);
        if (node.isLeaf())
        {
            return 0;
        }
        
        // retrieve geometry of the shape within the slice node
        ShapeNode shapeNode = (ShapeNode) node.children().iterator().next();
        if (!(shapeNode.getGeometry() instanceof LinearRing2D))
        {
            throw new RuntimeException("Expect crop geometry to be a LinearRing2D");
        }
        
        // check if the query point is within the polygon defined by the ring
        LinearRing2D ring = (LinearRing2D) shapeNode.getGeometry();
        if (ring.isInside(new Point2D(pos[0], pos[1])))
        {
            return this.refArray.getByte(pos);
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
