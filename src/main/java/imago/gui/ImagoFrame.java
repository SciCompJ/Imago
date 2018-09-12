/**
 * 
 */
package imago.gui;

import javax.swing.JFrame;

/**
 * The superclass of all Imago frames. 
 * Keeps a reference to the current ImagoGui instance.
 * 
 * @author David Legland
 *
 */
public abstract class ImagoFrame  
{
	/**
	 * Reference to the global GUI, for retrieving other frames open within the
	 * current application
	 */
	ImagoGui gui;

	ImagoFrame parentFrame = null;
	
	/**
	 * The Swing widget used to display this frame.
	 */
	JFrame jFrame;
	
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
	    this();
		this.gui = gui;
	}

	/**
	 * Creates a new frame by specifying the parent frame.
	 * 
	 * @param frame the parent frame
	 */
	protected ImagoFrame(ImagoFrame parent)
	{
	    this.parentFrame = parent;
		this.gui = parent.gui;
	}

	/**
	 * Creates a new frame and specifies the title.
	 */
	protected ImagoFrame(ImagoGui gui, String name)
	{
		this(name);
		this.gui = gui;
	}

	/**
	 * Creates a new frame and specifies the title.
	 */
	protected ImagoFrame(ImagoFrame parent, String name)
	{
		this(name);
		this.gui = parent.gui;
	}
	
    private ImagoFrame()
    {
        this.jFrame = new JFrame();
    }
    
    private ImagoFrame(String name)
    {
        this.jFrame = new JFrame(name);
    }
    
	
	// ===================================================================
	// General methods

	public void showErrorDialog(String message) 
	{
        ImagoGui.showErrorDialog(this, message, "Error");
	}
	
	public void showErrorDialog(String message, String title) 
	{
        ImagoGui.showErrorDialog(this, message, title);
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
	
	public ImagoGui getGui()
	{
	    return this.gui;
	}
	
    /**
     * Returns the widget instance used to represent this frame.
     * 
     * @return an instance of the Window Toolkit (here, a JFrame)
     */
	public JFrame getWidget()
	{
	    return this.jFrame;
	}
	

	// ===================================================================
    // Management of parent / children frames
	
	public ImagoFrame getParentFrame()
	{
	    return this.parentFrame;
	}


    // ===================================================================
    // Overload some methods from the inner JFrame

	public void repaint()
	{
	    this.jFrame.repaint();
	}
	
    public void setVisible(boolean b)
    {
        this.jFrame.setVisible(b);
    }

    public void setTitle(String title)
    {
        this.jFrame.setTitle(title);
    }
    
    public void close()
    {
        this.jFrame.dispose();
    }
}
