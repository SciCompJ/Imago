/**
 * 
 */
package imago.plugin.edit;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;

/**
 * Choose the radiusofthebrush.
 * 
 * @author dlegland
 *
 */
public class ChooseBrushRadius implements Plugin
{
	/**
	 */
	public ChooseBrushRadius()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
	    ImagoGui gui = frame.getGui();
	    double brushRadius = gui.settings.brushRadius;
	    
	    GenericDialog dlg = new GenericDialog(frame, "Brush Radius");
	    dlg.addNumericField("Brush Radius", brushRadius, 2, "The radius ofthe brush used to draw on images");
	    
	    dlg.showDialog();
	    if (dlg.wasCanceled())
	    {
	        return;
	    }
	    
	    double value = dlg.getNextNumber();
	    gui.settings.brushRadius = value;

	    System.out.println("brush radius changed to: " + gui.settings.brushRadius);
	}

}