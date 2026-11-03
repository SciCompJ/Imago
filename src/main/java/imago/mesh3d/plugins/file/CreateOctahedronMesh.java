/**
 * 
 */
package imago.mesh3d.plugins.file;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.mesh3d.Mesh3DViewer;
import net.sci.geom.mesh3d.Meshes3D;
import net.sci.geom.mesh3d.TriMesh3D;

/**
 * Creates an icosahedron mesh into a new frame.
 */
public class CreateOctahedronMesh implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        TriMesh3D mesh = Meshes3D.createOctahedron();
        Mesh3DViewer newFrame = Mesh3DViewer.create(mesh, frame);
        newFrame.setVisible(true);
    }
}
