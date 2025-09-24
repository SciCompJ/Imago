/**
 * 
 */
package imago.gui.plugin.plugin.crop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.LinearRing2D;
import net.sci.geom.polygon2d.Polyline2D;
import net.sci.geom.polygon2d.Polyline2D.Vertex;

/**
 * @author dlegland
 *
 */
public class ParallelPolygonsInterpolatorTest
{
    /**
     * Test method for {@link imago.gui.plugin.plugin.crop.ParallelPolygonsInterpolator#computeBestPath()}.
     */
    @Test
    public final void testInterpolate_TwoSquares()
    {
        LinearRing2D ring1 = LinearRing2D.create(4);
        ring1.addVertex(new Point2D(10, 10));
        ring1.addVertex(new Point2D(50, 10));
        ring1.addVertex(new Point2D(50, 50));
        ring1.addVertex(new Point2D(10, 50));
        
        LinearRing2D ring2 = LinearRing2D.create(4);
        ring2.addVertex(new Point2D( 0,  0));
        ring2.addVertex(new Point2D(70,  0));
        ring2.addVertex(new Point2D(70, 70));
        ring2.addVertex(new Point2D( 0, 70));
        
        ParallelPolygonsInterpolator interp = new ParallelPolygonsInterpolator(ring1, 0.0, ring2, 10.0);
        
        LinearRing2D res = interp.interpolate(2.0);
        
        // Expect nv1+nv2 vertices 
        assertEquals(8, res.vertexCount());

//        System.out.println("Vertices:");
//        for (Vertex v : res.vertices())
//        {
//            System.out.println(v.position());
//        }
        
        // Expect to contains some specific vertices
        assertTrue(polylineContainsVertex(res, new Point2D( 8.0,  8.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(40.0,  8.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(54.0,  8.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(54.0, 40.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(54.0, 54.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(40.0, 54.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D( 8.0, 54.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D( 8.0, 40.0), 0.01));
    }
    
    /**
     * Test method for {@link imago.gui.plugin.plugin.crop.ParallelPolygonsInterpolator#computeBestPath()}.
     */
    @Test
    public final void testInterpolate_TwoSquares_reverse()
    {
        LinearRing2D ring1 = LinearRing2D.create(4);
        ring1.addVertex(new Point2D(10, 10));
        ring1.addVertex(new Point2D(50, 10));
        ring1.addVertex(new Point2D(50, 50));
        ring1.addVertex(new Point2D(10, 50));
        
        LinearRing2D ring2 = LinearRing2D.create(4);
        ring2.addVertex(new Point2D( 0,  0));
        ring2.addVertex(new Point2D(70,  0));
        ring2.addVertex(new Point2D(70, 70));
        ring2.addVertex(new Point2D( 0, 70));
        
        ParallelPolygonsInterpolator interp = new ParallelPolygonsInterpolator(ring2, 10.0, ring1, 0.0);
                
        LinearRing2D res = interp.interpolate(2.0);
        
        // Expect nv1+nv2 vertices 
        assertEquals(8, res.vertexCount());

//        System.out.println("Vertices:");
//        for (Vertex v : res.vertices())
//        {
//            System.out.println(v.position());
//        }
        
        // Expect to contains some specific vertices
        assertTrue(polylineContainsVertex(res, new Point2D( 8.0,  8.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(40.0,  8.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(54.0,  8.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(54.0, 40.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(54.0, 54.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D(40.0, 54.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D( 8.0, 54.0), 0.01));
        assertTrue(polylineContainsVertex(res, new Point2D( 8.0, 40.0), 0.01));
    }
    
    private boolean polylineContainsVertex(Polyline2D poly, Point2D pos, double distTol)
    {
        for (Vertex v : poly.vertices())
        {
            if (v.position().distance(pos) <= distTol)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Test method for {@link imago.gui.plugin.plugin.crop.ParallelPolygonsInterpolator#computeBestPath()}.
     */
    @Test
    public final void testComputeBestPath_TwoSquares()
    {
        LinearRing2D ring1 = LinearRing2D.create(4);
        ring1.addVertex(new Point2D(10, 10));
        ring1.addVertex(new Point2D(50, 10));
        ring1.addVertex(new Point2D(50, 50));
        ring1.addVertex(new Point2D(10, 50));
        
        LinearRing2D ring2 = LinearRing2D.create(4);
        ring2.addVertex(new Point2D( 0,  0));
        ring2.addVertex(new Point2D(70,  0));
        ring2.addVertex(new Point2D(70, 70));
        ring2.addVertex(new Point2D( 0, 70));
        
        ParallelPolygonsInterpolator interp = new ParallelPolygonsInterpolator(ring1, ring2);
        
        ParallelPolygonsInterpolator.Path path = interp.computeBestPath();
        // Expect nv1+nv2+1 vertices, as there are nv1+nv2 edges (last vertex corresponds to first vertex) 
        assertEquals(9, path.indexPairs.size());

//        System.out.println("Path:");
//        for(ParallelPolygonsInterpolator.IndexPair pair : path.indexPairs)
//        {
//            System.out.println(pair);
//        }
    }

}
