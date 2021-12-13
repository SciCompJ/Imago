/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;

/**
 * Rename the name of the current image, keeping unicity of its name.
 * 
 * @author dlegland
 *
 */
public class RenameImage implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        System.out.println("rename image");
        
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
        
        String newName = dlg.getNextString();
        if (name.equals(newName))
        {
            return;
        }
        
        newName = frame.getGui().getAppli().createHandleName(newName);
        
        doc.setName(newName);
        ((ImageFrame) frame).updateTitle();
    }
}
