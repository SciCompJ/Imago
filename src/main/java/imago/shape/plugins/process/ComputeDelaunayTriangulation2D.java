/**
 * 
 */
package imago.shape.plugins.process;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.MultiPoint2D;
import net.sci.geom.mesh2d.Mesh2D;
import net.sci.geom.mesh2d.process.ConvexHullDelaunayTriangulation;

/**
 * Computes the Delaunay triangulation of the selected shape, that must be a set
 * of points.
 */
public class ComputeDelaunayTriangulation2D implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        GeometryHandle handle = sm.getSelectedHandle();
        if (handle == null) return;
        
        Geometry geom = handle.getGeometry();
        if (geom instanceof MultiPoint2D points)
        {
            ConvexHullDelaunayTriangulation algo = new ConvexHullDelaunayTriangulation();
            Mesh2D dt = algo.process(points.points());
            
            // add new geometry to appli
            GeometryHandle newHandle = GeometryHandle.create(frame.getGui().getAppli(), dt, handle);
            newHandle.setName(handle.getName() + "-DelaunayTriangulation");
            
            // refresh display
            sm.updateInfoTable();
        }
        else
        {
            ImagoGui.showErrorDialog(frame, "Delaunay triangulation requires input to be a collection of points", "Wrong data type");
            return;
        }
    }
}
