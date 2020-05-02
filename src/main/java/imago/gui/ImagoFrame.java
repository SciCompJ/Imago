/**
 * 
 */
package imago.gui;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JFrame;

/**
 * The superclass of all Imago frames. 
 * Keeps a reference to the current ImagoGui instance.
 * 
 * @see ImageFrame.gui.ImagoDocViewer
 * @see TableFrame.gui.ImagoTableFrame
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

	ImagoFrame parent = null;
	
	ArrayList<ImagoFrame> children = new ArrayList<>(0);
	
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
     * Creates a new frame and specifies the title.
     */
    protected ImagoFrame(ImagoGui gui, String name)
    {
        this(name);
        this.gui = gui;
    }

	/**
	 * Creates a new frame by specifying the parent frame.
	 * 
	 * @param frame the parent frame
	 */
	protected ImagoFrame(ImagoFrame parent)
	{
	    this.parent = parent;
	    this.parent.addChild(this);
		this.gui = parent.gui;
		
		// default positioning of the frame
		Point pos = this.parent.jFrame.getLocation();
		this.jFrame.setLocation(pos.x + 40, pos.y + 25);
	}

	/**
	 * Creates a new frame and specifies the title.
	 */
	protected ImagoFrame(ImagoFrame parent, String name)
	{
		this(name);
        this.parent = parent;
        this.parent.addChild(this);
		this.gui = parent.gui;
        
        // default positioning of the frame
        Point pos = this.parent.jFrame.getLocation();
        this.jFrame.setLocation(pos.x + 40, pos.y + 25);
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
	
	public ImagoFrame getParent()
	{
	    return this.parent;
	}

    public void addChild(ImagoFrame frame)
    {
        this.children.add(frame);
    }

    public void removeChild(ImagoFrame frame)
	{
        this.children.remove(frame);	    
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
        // remove from GUI
        gui.removeFrame(this);
        
        // remove from children if any
        for (ImagoFrame child : children)
        {
            child.parent = null;
        }
        
        // remove widget
        this.jFrame.dispose();
    }
    
    public void closeChildren()
    {
        // close children
        for (ImagoFrame child : this.children)
        {
            child.closeChildren();
            child.close();
        }
        
        this.children.clear();
    }
}
