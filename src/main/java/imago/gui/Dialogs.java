/**
 * 
 */
package imago.gui;

import imago.app.ImagoApp;
import imago.image.ImageHandle;

/**
 * A collection of general purpose dialogs.
 */
public class Dialogs
{
    /**
     * Opens a dialog to choose an image from a list of name, and returns the
     * corresponding {@code ImageHandle}.
     * 
     * @param frame
     *            the parent frame of the dialog
     * @param title
     *            the title of the dialog
     * @param label
     *            the label used to populate dialog widget
     * @return a handle to the selected image, or null of dialog was canceled.
     */
    public static final ImageHandle chooseImage(ImagoFrame frame, String title, String label)
    {
        ImagoApp appli = frame.getGui().getAppli();
        String[] imageNames = ImageHandle.getAllNames(appli).toArray(new String[0]);
        if (imageNames.length == 0)
        {
            ImagoGui.showErrorDialog(frame, "Requires at least one image to be open", "No Image Error");
            return null;
        }
        
        GenericDialog dlg = new GenericDialog(frame, title);
        dlg.addChoice(label, imageNames, imageNames[0]);
        dlg.showDialog();
        
        if (dlg.wasCanceled()) 
        {
            return null;
        }
        
        // Parse dialog options
        return ImageHandle.findFromName(appli, dlg.getNextChoice());
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private Dialogs()
    {
    }
}
