/**
 * 
 */
package imago.gui;


import java.awt.Dimension;
import java.awt.Toolkit;


/**
 * @author David Legland
 *
 */
public class ImagoEmptyFrame extends ImagoFrame {

	// ===================================================================
	// Static class variables

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	// ===================================================================
	// Constructor

	public ImagoEmptyFrame(ImagoGui gui) 
	{
		super(gui, "Image Frame");
		
		GuiBuilder builder = new GuiBuilder(this);
		builder.createMenuBar();
		
		putFrameTopRight();
	}
	
	/** 
	 * Set up frame size depending on screen size
	 */
	private void putFrameTopRight()
	{
		// Get screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		this.setMinimumSize(new Dimension(300, 0));
		this.pack();
		
		// set up frame position depending on frame size
		Dimension size = this.getSize();
		int posX = (screenSize.width - size.width - 100);
		int posY = 50;
		this.setLocation(posX, posY);
	}
}
