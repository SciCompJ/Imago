/**
 * 
 */
package imago.plugin.edit;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.FramePlugin;

/**
 * @author dlegland
 *
 */
public class ChooseBrushValue implements FramePlugin
{
	/**
	 */
	public ChooseBrushValue()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
	    ImagoGui gui = frame.getGui();
	    double brushValue = gui.userPreferences.brushValue;
	    
	    GenericDialog dlg = new GenericDialog(frame, "Brush Value");
	    dlg.addNumericField("Brush Value", brushValue, 2, "The value used to draw on intensity images");
	    
	    dlg.showDialog();
	    if (dlg.wasCanceled())
	    {
	        return;
	    }
	    
	    double value = dlg.getNextNumber();
	    gui.userPreferences.brushValue = value;

	    System.out.println("brush value changed to: " + gui.userPreferences.brushValue);
	}

}
