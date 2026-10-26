/**
 * 
 */
package imago.shape.plugins.process;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import imago.view3d.ZBufferRendererFrame;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.Meshes3D;

/**
 * Displays the selected mesh in a new instance of 3D viewer.
 */
public class DisplayMeshInViewer3D implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        GeometryHandle handle = sm.getSelectedHandle();
        if (handle == null) return;
        
        if (handle.getGeometry() instanceof Mesh3D mesh)
        {
            ImagoGui gui = frame.getGui();
            ZBufferRendererFrame newFrame = new ZBufferRendererFrame(gui);
            newFrame.setMesh(Meshes3D.triangulate(mesh));
            gui.updateFrameLocation(newFrame, frame);
            newFrame.setVisible(true);
        }
        else
        {
            ImagoGui.showErrorDialog(frame, "Requires selected geometry to be a Mesh3D", "Wrong data type");
            return;
        }
    }
    
}
