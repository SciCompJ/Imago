/**
 * 
 */
package imago.shape.plugins.edit;


import imago.gui.ImagoFrame;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;

/**
 * Displays some data about the selected geometry. The nature of data depends on
 * the geometry.
 */
public class PrintGeometryInfo implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        GeometryHandle handle = sm.getSelectedHandle();
        if (handle == null) return;
        
        net.sci.geom.Geometry geom = handle.getGeometry();
        System.out.println("Geometry with type: " + handle.getItemClassName() + " (class: "
                + geom.getClass().getSimpleName() + ") and name: \"" + handle.getName() + "\"");
        switch (handle.getGeometry())
        {
            case net.sci.geom.geom2d.Point2D p -> System.out.printf("%s\n", p.toString());
            case net.sci.geom.polygon2d.Polygon2D poly -> System.out.printf("polygon with %d vertices\n", poly.vertexCount());
            case net.sci.geom.polygon2d.Polyline2D poly -> System.out.printf("polyline with %d vertices\n", poly.vertexCount());
            case net.sci.geom.mesh3d.Mesh3D mesh -> System.out.printf("mesh with %d vertices and %f faces\n", mesh.vertexCount(), mesh.faceCount());
            default -> System.out.printf("%s\n", geom.toString()); 
        }
        sm.updateInfoTable();
    }
    
}
