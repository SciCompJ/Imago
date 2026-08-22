/**
 * 
 */
package imago.image.plugins.edit;

import java.util.prefs.Preferences;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;

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
        Preferences prefs = Preferences.userRoot().node("imago/file");

        // open a dialog initialized with current preferences
        GenericDialog dlg = new GenericDialog(frame, "File Dialog Widgets");
        dlg.addCheckBox("Native Dialog for Opening files", prefs.getBoolean("UseFileOpenSystemDialog", false));
        dlg.addCheckBox("Native Dialog for Saving files", prefs.getBoolean("UseFileSaveSystemDialog", false));

        // wait for user
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }

        // update preferences
        prefs.putBoolean("UseFileOpenSystemDialog", dlg.getNextBoolean());
        prefs.putBoolean("UseFileSaveSystemDialog", dlg.getNextBoolean());
    }
}
