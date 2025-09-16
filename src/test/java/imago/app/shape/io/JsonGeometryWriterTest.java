/**
 * 
 */
package imago.app.shape.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.LinearRing2D;

/**
 * 
 */
class JsonGeometryWriterTest
{
    /**
     * Test method for {@link imago.app.shape.io.JsonGeometryWriter#writeGeometry(net.sci.geom.Geometry)}.
     * @throws IOException 
     */
    @Test
    final void test_writeGeometry_LinearRing2D() throws IOException
    {
        LinearRing2D ring = LinearRing2D.create();
        ring.addVertex(new Point2D(10, 10));;
        ring.addVertex(new Point2D(20, 10));;
        ring.addVertex(new Point2D(20, 20));;
        ring.addVertex(new Point2D(10, 20));;

        StringWriter stringWriter = new StringWriter();
        JsonGeometryWriter writer = new JsonGeometryWriter(new PrintWriter(stringWriter));
        writer.setIndent("  ");
        
        writer.writeGeometry(ring);
        
        System.out.println(stringWriter.toString());
        writer.close();
    }
    
    /**
     * Test method for {@link imago.app.shape.io.JsonGeometryWriter#writeGeometry(net.sci.geom.Geometry)}.
     * @throws IOException 
     */
    @Test
    final void test_writeGeometry_LineSegment2D() throws IOException
    {
        Point2D p1 = new Point2D(40, 20);
        Point2D p2 = new Point2D(80, 50);
        LineSegment2D seg = new LineSegment2D(p1, p2);

        StringWriter stringWriter = new StringWriter();
        JsonGeometryWriter writer = new JsonGeometryWriter(new PrintWriter(stringWriter));
        writer.setIndent("  ");
        
        writer.writeGeometry(seg);
        
        System.out.println(stringWriter.toString());
        writer.close();
    }
}
