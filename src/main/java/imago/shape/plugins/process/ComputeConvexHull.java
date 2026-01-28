/**
 * 
 */
package imago.shape.plugins.process;

import imago.gui.ImagoFrame;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.PointShape2D;
import net.sci.geom.polygon2d.Polygon2D;
import net.sci.geom.polygon2d.Polygons2D;
import net.sci.geom.polygon2d.Polyline2D;

/**
 * Computes the convex hull of of the selected geometry, and creates a new
 * geometry.
 */
public class ComputeConvexHull implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        GeometryHandle handle = sm.getSelectedHandle();
        if (handle == null) return;
        
        Geometry geom = handle.getGeometry();
        Geometry hull = switch (geom)
        {
            case PointShape2D points -> Polygons2D.convexHull(points.points());
            case Polygon2D poly -> Polygons2D.convexHull(poly.vertexPositions());
            case Polyline2D poly -> Polygons2D.convexHull(poly.vertexPositions());
            default -> 
            {
                System.err.println("Can not compute convex hull for class: " + geom.getClass());
                yield null;
            }
        };
        if (hull == null) return;
        
        // add new geometry to appli
        GeometryHandle newHandle = GeometryHandle.create(frame.getGui().getAppli(), hull, handle);
        newHandle.setName(handle.getName() + "-Hull");
        
        // refresh display
        sm.updateInfoTable();
    }
    
}
