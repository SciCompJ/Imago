/**
 * 
 */
package imago.mesh3d.plugins.edit;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.mesh3d.Mesh3DViewer;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import net.sci.geom.mesh3d.Mesh3D;

/**
 * Adds the current mesh to the global shape manager.
 */
public class AddMeshToShapeManager implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        if (frame instanceof Mesh3DViewer meshFrame)
        {
            ImagoApp app = frame.getGui().getAppli();
            Mesh3D mesh = meshFrame.getMesh();
            GeometryHandle geomHandle = GeometryHandle.create(app, mesh);

            
            // opens a dialog to choose name
            String name = geomHandle.getName();
            name = ImagoGui.showInputDialog(frame, "Name of Mesh:", "Choose Mesh Name", name);
            geomHandle.setName(name);
            
            // ensure ShapeManager is visible
            ShapeManager manager = ShapeManager.getInstance(frame.getGui());
            manager.repaint();
            manager.setVisible(true);
        }
    }

}
