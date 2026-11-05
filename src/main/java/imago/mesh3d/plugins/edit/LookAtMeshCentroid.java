/**
 * 
 */
package imago.mesh3d.plugins.edit;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.mesh3d.Mesh3DViewer;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.mesh3d.Mesh3D;

/**
 * Updates the "lookat" of the current viewer to the centroids of the vertices
 * of the current mesh.
 */
public class LookAtMeshCentroid implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public LookAtMeshCentroid()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        if (frame instanceof Mesh3DViewer meshFrame)
        {
            Mesh3D mesh = meshFrame.getMesh();
            if (mesh == null) return;
            
            Point3D centroid = Point3D.centroid(mesh.vertexPositions());
            meshFrame.getRenderer().setLookAt(centroid);
            meshFrame.getRenderer().reset();
            
            meshFrame.repaint();
        }
    }

}
