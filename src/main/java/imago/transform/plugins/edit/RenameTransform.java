/**
 * 
 */
package imago.transform.plugins.edit;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.transform.TransformHandle;
import imago.transform.TransformManager;
import imago.transform.plugins.TransformManagerPlugin;

/**
 * Renames the selected transform. Requires selection to have only one element.
 */
public class RenameTransform implements TransformManagerPlugin
{

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        TransformManager tm = (TransformManager) frame;
     
        TransformHandle handle = tm.getSelectedHandle();
        if (handle == null) return;
        
        String newName = ImagoGui.showInputDialog(tm, "Rename transform", "Enter new name:", handle.getName());
        handle.setName(newName);
        
        tm.updateInfoTable();
    }
}