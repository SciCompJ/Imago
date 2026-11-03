/**
 * 
 */
package imago.mesh3d.plugins.file;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.mesh3d.Mesh3DViewer;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.mesh3d.Meshes3D;
import net.sci.geom.mesh3d.TriMesh3D;

/**
 * Creates an icosahedron mesh into a new frame.
 */
public class CreateIcosahedronMesh implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        TriMesh3D mesh = Meshes3D.createIcosahedron();
        Mesh3DViewer newFrame = Mesh3DViewer.create(mesh, frame);
        Point3D centroid = Point3D.centroid(mesh.vertexPositions());
        newFrame.getRenderer().setLookAt(centroid);
        newFrame.setVisible(true);
    }
}
