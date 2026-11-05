/**
 * 
 */
package imago.mesh3d.plugins.process;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.mesh3d.Mesh3DViewer;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.Meshes3D;
import net.sci.geom.mesh3d.TriMesh3D;
import net.sci.geom.mesh3d.process.Smooth;

/**
 * Applies mesh smoothing to the current mesh.
 */
public class SmoothMesh implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public SmoothMesh()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        if (frame instanceof Mesh3DViewer meshFrame)
        {
            Mesh3D mesh = meshFrame.getMesh();
            if (mesh == null) return;
            
            Smooth op = new Smooth();
            op.addAlgoListener(meshFrame);

            long t0 = System.nanoTime();
            mesh = op.process(mesh);

            // ensure mesh is a triangle mesh
            if (!(mesh instanceof TriMesh3D))
            {
                mesh = Meshes3D.triangulate(mesh);
            }

            long t1 = System.nanoTime();
            double dt = (t1 - t0) / 1_000_000_000.0;

            meshFrame.getStatusBar().setProgressBarPercent(0);
            String msg = String.format("Read OFF Mesh: %.3f seconds", dt);
            meshFrame.getStatusBar().setCurrentStepLabel(msg);
            
            // replace mesh in current frame
            meshFrame.setMesh((TriMesh3D) mesh);
            meshFrame.repaint();
        }
    }
}
