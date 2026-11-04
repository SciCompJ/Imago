/**
 * 
 */
package imago.view3d;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.sci.geom.geom3d.Bounds3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.mesh3d.Mesh3D;

/**
 * 
 */
class Cylinder3DTest
{
    /**
     * Test method for {@link imago.view3d.Cylinder3D#createMesh(int)}.
     */
    @Test
    void testCreateMesh_Ox()
    {
        double L = 50;
        double R = 10;
        
        Point3D p1 = new Point3D(0, 0, 0);
        Point3D p2 = new Point3D(L, 0, 0);
        Cylinder3D cyl = new Cylinder3D(p1, p2, R);
        
        Mesh3D mesh = cyl.createMesh(12);
        Bounds3D bounds = mesh.bounds();
        
        assertEquals( 0, bounds.minCoord(0), 0.01);
        assertEquals(+L, bounds.maxCoord(0), 0.01);
        assertEquals(-R, bounds.minCoord(1), 0.01);
        assertEquals(+R, bounds.maxCoord(1), 0.01);
        assertEquals(-R, bounds.minCoord(2), 0.01);
        assertEquals(+R, bounds.maxCoord(2), 0.01);
    }

    /**
     * Test method for {@link imago.view3d.Cylinder3D#createMesh(int)}.
     */
    @Test
    void testCreateMesh_Oy()
    {
        double L = 50;
        double R = 10;
        
        Point3D p1 = new Point3D(0, 0, 0);
        Point3D p2 = new Point3D(0, L, 0);
        Cylinder3D cyl = new Cylinder3D(p1, p2, R);
        
        Mesh3D mesh = cyl.createMesh(12);
        Bounds3D bounds = mesh.bounds();
        
        assertEquals(-R, bounds.minCoord(0), 0.01);
        assertEquals(+R, bounds.maxCoord(0), 0.01);
        assertEquals( 0, bounds.minCoord(1), 0.01);
        assertEquals(+L, bounds.maxCoord(1), 0.01);
        assertEquals(-R, bounds.minCoord(2), 0.01);
        assertEquals(+R, bounds.maxCoord(2), 0.01);
    }

    /**
     * Test method for {@link imago.view3d.Cylinder3D#createMesh(int)}.
     */
    @Test
    void testCreateMesh_Oz()
    {
        double L = 50;
        double R = 10;
        
        Point3D p1 = new Point3D(0, 0, 0);
        Point3D p2 = new Point3D(0, 0, L);
        Cylinder3D cyl = new Cylinder3D(p1, p2, R);
        
        Mesh3D mesh = cyl.createMesh(12);
        Bounds3D bounds = mesh.bounds();
        
        assertEquals(-R, bounds.minCoord(0), 0.01);
        assertEquals(+R, bounds.maxCoord(0), 0.01);
        assertEquals(-R, bounds.minCoord(1), 0.01);
        assertEquals(+R, bounds.maxCoord(1), 0.01);
        assertEquals( 0, bounds.minCoord(2), 0.01);
        assertEquals(+L, bounds.maxCoord(2), 0.01);
    }
}
