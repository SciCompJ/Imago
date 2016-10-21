/**
 * 
 */
package imago.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The superclass of Imago frames. Makes reference to a GUI. 
 * @author David Legland
 *
 */
public abstract class ImagoFrame extends JFrame implements WindowListener 
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
	 * Creates a new frame.
	 * 
	 * @param gui
	 */
	protected ImagoFrame(ImagoGui gui)
	{
		this.gui = gui;
	}

	/**
	 * Creates a new frame.
	 * 
	 * @param gui
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

	
	// ===================================================================
	// Implementation of WindowListener

	@Override
	public void windowActivated(WindowEvent arg0)
	{
	}

	@Override
	public void windowClosed(WindowEvent arg0)
	{
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent arg0)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
	}

	@Override
	public void windowIconified(WindowEvent arg0)
	{
	}

	@Override
	public void windowOpened(WindowEvent arg0)
	{
	}
}
