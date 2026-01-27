/**
 * 
 */
package imago.shape.plugins.edit;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;

/**
 * Changes the name of the selected geometry.
 */
public class RenameGeometry implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        GeometryHandle handle = sm.getSelectedHandle();
        if (handle == null) return;
        
        String newName = ImagoGui.showInputDialog(sm, "Rename geometry", "Enter new name:", handle.getName());
        handle.setName(newName);
        
        sm.updateInfoTable();
    }
    
}
