/**
 * 
 */
package imago.image.plugin.edit;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.shapemanager.ShapeManager;

/**
 * Shows the current ShapeManager. 
 * 
 * @see ShapeManager
 *
 */
public class ShowShapeManagerFrame implements FramePlugin
{
    /**
     * Default empty constructor.
     */
	public ShowShapeManagerFrame()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
	    ShapeManager manager = ShapeManager.getInstance(frame.getGui());
	    
	    manager.setVisible(true);
	}
}
