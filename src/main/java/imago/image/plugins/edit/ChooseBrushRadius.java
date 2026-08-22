/**
 * 
 */
package imago.image.plugins.edit;

import java.util.prefs.Preferences;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;

/**
 * Choose the radius of the brush.
 * 
 * @author dlegland
 *
 */
public class ChooseBrushRadius implements FramePlugin
{
    /**
     * Default constructor.
     */
    public ChooseBrushRadius()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        Preferences prefs = Preferences.userRoot().node("imago/image/tools");
        double brushRadius = prefs.getDouble("BrushRadius", 5);
        
        GenericDialog dlg = new GenericDialog(frame, "Brush Radius");
        dlg.addNumericField("Brush Radius", brushRadius, 2,
                "The radius of the brush used to draw on images");

        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }

        double value = dlg.getNextNumber();
        prefs.putDouble("BrushRadius", value);

        System.out.println("brush radius changed to: " + value);
    }
}
