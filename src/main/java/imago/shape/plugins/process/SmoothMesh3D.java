/**
 * 
 */
package imago.shape.plugins.process;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import net.sci.algo.ConsoleAlgoListener;
import net.sci.geom.mesh3d.Mesh3D;

/**
 * Applies smoothing to a 3D selected within the ShapeManager.
 */
public class SmoothMesh3D implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        GeometryHandle handle = sm.getSelectedHandle();
        if (handle == null) return;
        
        if (handle.getGeometry() instanceof Mesh3D mesh)
        {
            net.sci.geom.mesh3d.process.Smooth algo = new net.sci.geom.mesh3d.process.Smooth();
            ConsoleAlgoListener.monitor(algo);
            Mesh3D res = algo.process(mesh);
            System.out.println("finishes mesh smoothing");
            
            // add new geometry to appli
            GeometryHandle newHandle = GeometryHandle.create(frame.getGui().getAppli(), res, handle);
            newHandle.setName(handle.getName() + "-smooth");
            
            // refresh display
            sm.updateInfoTable();
        }
        else
        {
            ImagoGui.showErrorDialog(frame, "Requires selected geometry to be a Mesh3D", "Wrong data type");
            return;
        }
    }
    
}
