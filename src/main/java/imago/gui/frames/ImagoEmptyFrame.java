/**
 * 
 */
package imago.gui.frames;


import java.awt.Dimension;

import imago.gui.GuiBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;


/**
 * @author David Legland
 *
 */
public class ImagoEmptyFrame extends ImagoFrame {

	// ===================================================================
	// Static class variables
	
	// ===================================================================
	// Constructor

	public ImagoEmptyFrame(ImagoGui gui) 
	{
		super(gui, "Image Frame");
		
        GuiBuilder builder = new GuiBuilder(this);
        builder.createMenuBar();
		
		putFrameTopLeft();
	}
	
	/** 
	 * Set up frame size depending on screen size
	 */
	private void putFrameTopLeft()
	{
		this.jFrame.setMinimumSize(new Dimension(300, 0));
		this.jFrame.pack();
		
		// set up frame position in the upper left corner
		int posX = 300;
		int posY = 50;
		this.jFrame.setLocation(posX, posY);
	}
}
