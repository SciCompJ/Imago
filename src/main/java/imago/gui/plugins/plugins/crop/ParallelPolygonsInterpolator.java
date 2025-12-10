/**
 * 
 */
package imago.gui.plugins.plugins.crop;

import java.util.ArrayList;
import java.util.Collections;

import net.sci.array.numeric.Float64Array2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.polygon2d.LinearRing2D;
import net.sci.geom.polygon2d.Polyline2D.Vertex;

/**
 * Computes an intermediate polygons from two polygons embedded into parallel
 * slices.
 * 
 * @author dlegland
 *
 */
public class ParallelPolygonsInterpolator
{
    /** 
     * The array of (2D) vertices of the first polygon. 
     */
    ArrayList<Point2D> vertices1;
    double z1 = 0.0;

    /** 
     * The array of (2D) vertices of the second polygon. 
     */
    ArrayList<Point2D> vertices2;
    double z2 = 1.0;
    
    /**
     *  The weights for horizontal transitions in the toroidal graph"
     */
    Float64Array2D weightsH;

    /**
     *  The weights for vertical transitions in the toroidal graph"
     */
    Float64Array2D weightsV;
    
    /**
     * The series of transitions that defines the path, and the triangulation.
     * Uses lazy evaluation of the path: it is computed only at first call of
     * the <code>interpolate</code> method. Subsequent calls can re-used the
     * same path / triangulation.
     * 
     * @see #interpolate(double)
     */
    Path path = null;
    
    
    /**
     * Creates a polygon interpolator, assuming default heights for each
     * polygon.
     * 
     * @param ring1
     *            the linear ring representing the first polygon. Its heights is
     *            set to 0.0 by default.
     * @param ring2
     *            the linear ring representing the second polygon. Its heights
     *            is set to 1.0 by default.
     */
    public ParallelPolygonsInterpolator(LinearRing2D ring1, LinearRing2D ring2)
    {
        this(ring1, 0.0, ring2, 1.0);
    }
    
    /**
     * Creates a polygon interpolator, assuming default heights for each
     * polygon.
     * 
     * @param ring1
     *            the linear ring representing the first polygon
     * @param z1
     *            the height (z-coordinate of the embedding plane) associated to
     *            the first polygon
     * @param ring2
     *            the linear ring representing the second polygon
     * @param z2
     *            the height (z-coordinate of the embedding plane) associated to
     *            the second polygon
     */
    public ParallelPolygonsInterpolator(LinearRing2D ring1, double z1, LinearRing2D ring2, double z2)
    {
        this.vertices1 = new ArrayList<Point2D>(ring1.vertexCount());
        for(Vertex v : ring1.vertices())
        {
            this.vertices1.add(v.position());
        }

        this.vertices2 = new ArrayList<Point2D>(ring2.vertexCount());
        for(Vertex v : ring2.vertices())
        {
            this.vertices2.add(v.position());
        }
        
        this.z1 = z1;
        this.z2 = z2;
    }
    
    /**
     * Computes the interpolation of the two inner polygons, by specifiying an
     * height that should be comprised between the heights of the two polygons to
     * interpolate.
     * 
     * @param z
     *            the height of the polygon to compute
     * @return the interpolated polygon
     */
    public LinearRing2D interpolate(double z)
    {
        // check input validity
        if (z < Math.min(z1, z2) || z > Math.max(z1, z2))
        {
            throw new RuntimeException("z position must be comprised between " + z1 + " and " + z2);
        }
        
        // compute relative weight for polygons 1 and 2
        double a1 = (z2 - z) / (z2 - z1);
        double a2 = (z - z1) / (z2 - z1);
        
        // ensure path was computed
        if (path == null)
        {
            path = computeBestPath();
        }
        
        // retrieve vertex counts
        int nv1 = vertices1.size();
        int nv2 = vertices2.size();
        
        // allocate memory
        ArrayList<Point2D> interpolatedVertices = new ArrayList<>(path.indexPairs.size());
        
        // iterate over vertices of the vertex-adjacencies grid
        for (IndexPair pair : path.indexPairs)
        {
            Point2D v1 = vertices1.get(pair.i1 % nv1);
            Point2D v2 = vertices2.get(pair.i2 % nv2);
            
            double x = v1.x() * a1 + v2.x() * a2;  
            double y = v1.y() * a1 + v2.y() * a2;
            interpolatedVertices.add(new Point2D(x, y));
        }
        
        // last vertex is same as first one
        interpolatedVertices.remove(path.indexPairs.size() - 1);
        
        // return as polyline 
        return LinearRing2D.create(interpolatedVertices);
    }
    
    public Path computeBestPath()
    {
        // ensure weights are up-to-date
        computeWeights();
        
        // need to compute as many paths as the number of vertices in first polygon
        int nv1 = vertices1.size();
        Path[] paths = new Path[nv1];
        
        // compute the paths starting from each vertex of polygon1
        for (int iv1 = 0; iv1 < nv1; iv1++)
        {
            paths[iv1] = computePath(iv1);
        }
        
        // find the path with minimal weight
        double minWeight = Double.POSITIVE_INFINITY;
        Path bestPath = paths[0];
        for (int iv1 = 0; iv1 < nv1; iv1++)
        {
            if (paths[iv1].weight < minWeight)
            {
                bestPath = paths[iv1];
                minWeight = bestPath.weight;
            }
        }
        
        return bestPath;
    }

    private void computeWeights()
    {
        // retrieve vertex counts
        int nv1 = vertices1.size();
        int nv2 = vertices2.size();
        
        // compute weights for horizontal transitions between graph vertices
        // corresponding to triangles with two vertices in poly2
        weightsH = Float64Array2D.create(2*nv1+1, nv2);
        for (int iv1 = 0; iv1 < nv1; iv1++)
        {
            Point3D v1 = translateZ(vertices1.get(iv1), z1);
            
            for (int iv2 = 0; iv2 < nv2; iv2++)
            {
                // new vertex in poly2
                Point2D vp2 = vertices2.get((iv2 + 1) % nv2);
                Point3D v2 = translateZ(vp2, z2);
                
                double d12 = v1.distance(v2);
                
                weightsH.setValue(iv1, iv2, d12);
                weightsH.setValue(iv1 + nv1, iv2, d12);
            }
        }
        
        // duplicate last line
        for (int iv2 = 0; iv2 < nv2; iv2++)
        {
            weightsH.setValue(2 * nv1, iv2, weightsH.getValue(0, iv2));
        }
        
        // compute weights for vertical transitions between graph vertices
        // corresponding to triangles with two vertices in poly1
        weightsV = Float64Array2D.create(2*nv1, nv2+1);
        for (int iv1 = 0; iv1 < nv1; iv1++)
        {
            // new vertex in poly1
            Point2D vp1 = vertices1.get((iv1 + 1) % nv1);
            Point3D v1 = translateZ(vp1, z1);
            
            for (int iv2 = 0; iv2 < nv2; iv2++)
            {
                Point3D v2 = translateZ(vertices2.get(iv2), z2);
                double d12 = v1.distance(v2);
                weightsV.setValue(iv1, iv2, d12);
                weightsV.setValue(iv1 + nv1, iv2, d12);
            }
        }
        
        // duplicate last column
        for (int iv1 = 0; iv1 < 2*nv1; iv1++)
        {
            weightsV.setValue(iv1, nv2, weightsV.getValue(iv1, 0));
        }
    }
    
    /**
     * Converts coordinates of a 2D point into 3D coordinates by adding the
     * specified z coordinate.
     * 
     * @param point
     *            the 2D point
     * @param z
     *            the z-coordinate
     * @return the translated 3D point
     */
    private Point3D translateZ(Point2D point, double z)
    {
        return new Point3D(point.x(), point.y(), z);
    }

    private Path computePath(int initialV1)
    {
        // retrieve vertex counts
        int nv1 = vertices1.size();
        int nv2 = vertices2.size();
        
        // compute final index for index in first dimension
        int i1Last = initialV1 + nv1;
        
        // Initialize matrix of cumulated weights

        // create matrix of cumulated  weights
        Float64Array2D cumWeights = Float64Array2D.create(2 * nv1 + 1, nv2 + 1);
        cumWeights.fillValue(Double.POSITIVE_INFINITY);
        
        // init first row
        double w = 0.0;
        cumWeights.setValue(initialV1, 0, w);
        for (int i2 = 0; i2 < nv2; i2++)
        {
            w += weightsH.getValue(initialV1, i2);
            cumWeights.setValue(initialV1, i2+1, w);
        }
        
        // init each subsequent row
        for (int i1 = initialV1+1; i1 <= i1Last; i1++)
        {
            // first vertex in row is initialized from the vertex above
            cumWeights.setValue(i1, 0, cumWeights.getValue(i1-1, 0) + weightsV.getValue(i1-1, 0));
            // other vertices minimize weights from left or top vertices
            for (int i2 = 1; i2 <= nv2; i2++)
            {
                double wH = cumWeights.getValue(i1, i2 - 1) + weightsH.getValue(i1, i2 - 1);
                double wV = cumWeights.getValue(i1 - 1, i2) + weightsV.getValue(i1 - 1, i2);
                cumWeights.setValue(i1, i2, Math.min(wH, wV));
            }
        }
        double pathWeight = cumWeights.getValue(i1Last, nv2);

        // Backpropagate to find path

        // the list of path positions, from the end (max cumulated weight) to the origin
        // (cumulated weight equal zero).
        ArrayList<IndexPair> posList = new ArrayList<IndexPair>(nv1 + nv2);
        posList.add(new IndexPair(i1Last, nv2));
        int i1 = i1Last;
        int i2 = nv2;
        for (int iPath = nv1 + nv2 - 1; iPath >= 0; iPath--)
        {
            // determine the weights associated to a move in the horizontal or
            // vertical direction
            boolean moveLeft = true;
            if (i2 == 0)
            {
                moveLeft = false;
            }
            else if (i1 > initialV1)
            {
                double wH = cumWeights.getValue(i1, i2 - 1);
                double wV = cumWeights.getValue(i1 - 1, i2);
                moveLeft = wH < wV;
            }
            
            // update position of current grid vertex
            if (moveLeft)
            {
                i2--;
            }
            else
            {
                i1--;
            }
            posList.add(new IndexPair(i1, i2));
        }
        // reverse the list of positions
        Collections.reverse(posList);
        
        // create result Path
        Path path = new Path(posList, pathWeight);
        return path;
    }
    
    /**
     * A path within a toroidal graph, simply represented by a series of
     * coordinate pairs. The total weight associated to the path is also stored
     * to facilitate retrieval of the best (with smallest weight) path.
     */
    public class Path
    {
        /**
         * The series of coordinates identifying the path.
         */
        ArrayList<IndexPair> indexPairs;
        
        /**
         * The total weight of the path.
         */
        double weight;
        
        /**
         * Creates a new path.
         * 
         * @param posList
         *            the series of coordinates identifying the path.
         * @param weight
         *            the total weight of the path.
         */
        public Path(ArrayList<IndexPair> posList, double weight)
        {
            this.indexPairs = posList;
            this.weight = weight;
        }
    }
    
    /**
     * Identifies an element within a 2D array.
     */
    class IndexPair
    {
        /** The first index (assumed to be larger than zero) */
        public final int i1;
        /** The second index (assumed to be larger than zero) */
        public final int i2;
 
        /**
         * Creates a new IndexPair.
         * 
         * @param i1
         *            the first index
         * @param i2
         *            the second index
         */
        public IndexPair(int i1, int i2)
        {
            this.i1 = i1;
            this.i2 = i2;
        }
        
        @Override
        public String toString()
        {
            return "(" + i1 + "," + i2 + ")";
        }
    }
}
