/**
 * 
 */
package imago.plugin.table.edit;

import imago.app.TableHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.table.TableFrame;

/**
 * Rename the name of the current image, ensuring unicity of its name.
 */
public class RenameTable implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame and table
        TableHandle handle = ((TableFrame) frame).getTableHandle();
        String name = handle.getName();
        
        GenericDialog dlg = new GenericDialog(frame, "Rename Table");
        dlg.addTextField("New Name:", name, 20);
        
        // wait for user validation or cancellation
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // retrieve new name of image
        String newName = dlg.getNextString();
        
        // if name is same as before, do nothing
        if (name.equals(newName) || name.isBlank())
        {
            return;
        }
        
        // ensure the name is unique within the set of open image handles
        newName = frame.getGui().getAppli().createHandleName(newName);
        
        // setup name for image, handle, and frame.
        handle.setName(newName);
        handle.getTable().setName(newName);
        ((TableFrame) frame).updateTitle();
    }
}
