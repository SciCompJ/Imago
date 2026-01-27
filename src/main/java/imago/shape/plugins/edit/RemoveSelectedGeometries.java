/**
 * 
 */
package imago.shape.plugins.edit;

import imago.app.ImagoApp;
import imago.gui.ImagoFrame;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;

/**
 * Removes the selected geometries from the workspace.
 */
public class RemoveSelectedGeometries implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ImagoApp appli = frame.getGui().getAppli();

        ShapeManager sm = (ShapeManager) frame;
        for (GeometryHandle handle : sm.getSelectedHandles())
        {
            appli.removeHandle(handle);
        }
        
        sm.updateInfoTable();
    }
    
}
