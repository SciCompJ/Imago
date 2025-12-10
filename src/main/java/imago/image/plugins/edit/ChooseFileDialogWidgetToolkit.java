/**
 * 
 */
package imago.image.plugins.edit;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.app.UserPreferences;
import imago.gui.FramePlugin;

/**
 * Choose whether file dialogs must use Native widget toolkit (AWT), or the more
 * recent graphical framework (Swing).
 * 
 * @author dlegland
 *
 */
public class ChooseFileDialogWidgetToolkit implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String args)
    {
        ImagoGui gui = frame.getGui();
        UserPreferences prefs = gui.getAppli().userPreferences;
        
        // open a dialog initialized with current preferences
        GenericDialog dlg = new GenericDialog(frame, "File Dialog Widgets");
        dlg.addCheckBox("Native Dialog for Opening files", prefs.useFileOpenSystemDialog);
        dlg.addCheckBox("Native Dialog for Saving files", prefs.useSaveFileSystemDialog);

        // wait for user
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }

        // update preferences
        prefs.useFileOpenSystemDialog = dlg.getNextBoolean();
        prefs.useSaveFileSystemDialog = dlg.getNextBoolean();
    }
}
