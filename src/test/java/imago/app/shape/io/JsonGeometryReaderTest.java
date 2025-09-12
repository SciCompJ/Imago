/**
 * 
 */
package imago.app.shape.io;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.google.gson.stream.JsonReader;

import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.LinearRing2D;
import net.sci.geom.polygon2d.Polygon2D;

/**
 * 
 */
class JsonGeometryReaderTest
{
    /**
     * Test method for {@link imago.app.shape.io.JsonGeometryReader#readGeometry()}.
     * @throws IOException 
     */
    @Test
    final void test_readGeometry_Point2D() throws IOException
    {
        // a single Point
        Point2D point = new Point2D(30, 20);
        String json = JsonGeometryWriter.toJson(point);
        
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        JsonGeometryReader sceneReader = new JsonGeometryReader(jsonReader);
        
        Geometry res = sceneReader.readGeometry();
        
        assertInstanceOf(Point2D.class, res);
        assertTrue(point.almostEquals((Point2D) res, 0.0001));
    }
    
    /**
     * Test method for {@link imago.app.shape.io.JsonGeometryReader#readGeometry()}.
     * @throws IOException 
     */
    @Test
    final void test_readGeometry_LinearRing2D() throws IOException
    {
        // a ring for the slice with index 10
        LinearRing2D ring = LinearRing2D.create();
        ring.addVertex(new Point2D(10, 10));
        ring.addVertex(new Point2D(20, 10));
        ring.addVertex(new Point2D(20, 20));
        ring.addVertex(new Point2D(10, 20));
        
        String json = JsonGeometryWriter.toJson(ring);
        
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        JsonGeometryReader sceneReader = new JsonGeometryReader(jsonReader);
        
        Geometry res = sceneReader.readGeometry();
        assertInstanceOf(LinearRing2D.class, res);
    }
    
    /**
     * Test method for {@link imago.app.shape.io.JsonGeometryReader#readGeometry()}.
     * @throws IOException 
     */
    @Test
    final void test_readGeometry_Polygon2D() throws IOException
    {
        // a ring for the slice with index 10
        Polygon2D poly = Polygon2D.create(
                new Point2D(10, 10), 
                new Point2D(20, 10), 
                new Point2D(20, 20),
                new Point2D(10, 20));
        
        String json = JsonGeometryWriter.toJson(poly);
        
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        JsonGeometryReader sceneReader = new JsonGeometryReader(jsonReader);
        
        Geometry res = sceneReader.readGeometry();
        assertTrue(res instanceof Polygon2D);
        LinearRing2D ring = ((Polygon2D) res).boundary();
        assertTrue(ring.contains(new Point2D(10, 10), 0.01));
        assertTrue(ring.contains(new Point2D(20, 10), 0.01));
        assertTrue(ring.contains(new Point2D(20, 20), 0.01));
        assertTrue(ring.contains(new Point2D(10, 20), 0.01));
    }
    
}
