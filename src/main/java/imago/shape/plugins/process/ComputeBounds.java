/**
 * 
 */
package imago.shape.plugins.process;

import imago.gui.ImagoFrame;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom3d.Geometry3D;
import net.sci.geom.mesh3d.Meshes3D;
import net.sci.geom.polygon2d.Polygon2D;

/**
 * Computes the bounds of of the selected geometry, and creates a new geometry
 * from them.
 */
public class ComputeBounds implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        GeometryHandle handle = sm.getSelectedHandle();
        if (handle == null) return;
        
        Geometry bounds = switch (handle.getGeometry())
        {
            case Geometry2D geom2d -> Polygon2D.fromBounds(geom2d.bounds());
            case Geometry3D geom3d -> Meshes3D.fromBounds(geom3d.bounds());
            default -> throw new RuntimeException(
                    "Unable to manage geometry with class " + handle.getGeometry().getClass().getName());
        };
        
        // add new geometry to appli
        GeometryHandle newHandle = GeometryHandle.create(frame.getGui().getAppli(), bounds, handle);
        newHandle.setName(handle.getName() + "-Bounds");
        
        // refresh display
        sm.updateInfoTable();
    }
    
}
