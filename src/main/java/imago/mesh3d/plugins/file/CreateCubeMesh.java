/**
 * 
 */
package imago.mesh3d.plugins.file;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.mesh3d.Mesh3DViewer;
import net.sci.geom.mesh3d.Meshes3D;
import net.sci.geom.geom3d.Bounds3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.mesh3d.Mesh3D;

/**
 * Creates a 3D curve and display it into a new frame.
 */
public class CreateCubeMesh implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        Bounds3D bounds = new Bounds3D(Point3D.of(0, 0, 0), Point3D.of(100, 100, 100));
        Mesh3D mesh = Meshes3D.fromBounds(bounds);
        Mesh3DViewer newFrame = Mesh3DViewer.create(mesh, frame);
        newFrame.setVisible(true);
    }
}
