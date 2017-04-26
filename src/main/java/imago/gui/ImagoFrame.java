/**
 * 
 */
package imago.gui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The superclass of all Imago frames. 
 * Keeps a reference to the current ImagoGui instance.
 * 
 * @author David Legland
 *
 */
public abstract class ImagoFrame extends JFrame 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Reference to the global GUI, for retrieving other frames open within the
	 * current application
	 */
	ImagoGui gui;

	/**
	 * The path to the directory that was used to save this document.
	 */
	String lastOpenPath = "";
	

	// ===================================================================
	// Constructors

	/**
	 * Creates a new frame by specifying the parent GUI.
	 * 
	 * @param gui
	 */
	protected ImagoFrame(ImagoGui gui)
	{
		this.gui = gui;
	}

	/**
	 * Creates a new frame by specifying the parent frame.
	 * 
	 * @param frame the parent frame
	 */
	protected ImagoFrame(ImagoFrame parent)
	{
		this.gui = parent.gui;
	}

	/**
	 * Creates a new frame and specifies the title.
	 */
	protected ImagoFrame(ImagoGui gui, String name)
	{
		super(name);
		this.gui = gui;
	}

	/**
	 * Creates a new frame and specifies the title.
	 */
	protected ImagoFrame(ImagoFrame parent, String name)
	{
		super(name);
		this.gui = parent.gui;
	}
	
	// ===================================================================
	// General methods

	public void showErrorDialog(String message) 
	{
		JOptionPane.showMessageDialog(
				this, message, "Error", 
				JOptionPane.ERROR_MESSAGE);
	}
	
	public void showErrorDialog(String message, String title) 
	{
		JOptionPane.showMessageDialog(
				this, message, title,
				JOptionPane.ERROR_MESSAGE);
	}
	
	
	// ===================================================================
	// Getters and setters

	public String getLastOpenPath()
	{
		return lastOpenPath;
	}

	public void setLastOpenPath(String lastOpenPath)
	{
		this.lastOpenPath = lastOpenPath;
	}
	
}
