/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;

/**
 * Rename the name of the current image, ensuring unicity of its name.
 * 
 * @author dlegland
 *
 */
public class RenameImage implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        String name = doc.getName();
        
        GenericDialog dlg = new GenericDialog(frame, "Rename Image");
        dlg.addTextField("New Name:", name);
        
        // wait for user validation or cancellation
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // retrieve new name of image
        String newName = dlg.getNextString();
        
        // if name is same as before, do nothing
        if (name.equals(newName))
        {
            return;
        }
        
        // ensure the name is unique within the set of open image handles
        newName = frame.getGui().getAppli().createHandleName(newName);
        
        // setup name for image, handle, and frame.
        doc.getImage().setName(newName);
        doc.getImage().setExtension("");
        doc.setName(newName);
        ((ImageFrame) frame).updateTitle();
    }
}
