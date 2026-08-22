/**
 * 
 */
package imago.image.plugins.edit;

import java.util.prefs.Preferences;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;

/**
 * Choose the intensity value of the brush.
 * 
 * @author dlegland
 *
 */
public class ChooseBrushValue implements FramePlugin
{
    /**
     * Default constructor.
     */
	public ChooseBrushValue()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
        Preferences prefs = Preferences.userRoot().node("imago/image/tools");
        double brushValue = prefs.getDouble("BrushValue", 255);
	    
	    GenericDialog dlg = new GenericDialog(frame, "Brush Value");
	    dlg.addNumericField("Brush Value", brushValue, 2, "The value used to draw on intensity images");
	    
	    dlg.showDialog();
	    if (dlg.wasCanceled())
	    {
	        return;
	    }
	    
	    double value = dlg.getNextNumber();
	    prefs.putDouble("BrushValue", value);

	    System.out.println("brush value changed to: " + value);
	}

}
