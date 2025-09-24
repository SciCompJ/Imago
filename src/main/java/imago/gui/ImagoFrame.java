/**
 * 
 */
package imago.gui;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JFrame;

import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;

/**
 * The superclass of all Imago frames. 
 * Keeps a reference to the current ImagoGui instance.
 * 
 * Most frames implementations are located in the {@link imago.gui.frames} package.

 * @see imago.image.ImageFrame
 * @see imago.table.TableFrame
 * @see imago.gui.frames.ImagoTextFrame
 * 
 * @author David Legland
 *
 */
public abstract class ImagoFrame implements AlgoListener
{
	/**
	 * Reference to the global GUI, for retrieving other frames open within the
	 * current application
	 */
    protected ImagoGui gui;

	protected ImagoFrame parent = null;
	
	protected ArrayList<ImagoFrame> children = new ArrayList<>(0);
	
	/**
	 * The Swing widget used to display this frame.
	 */
	protected JFrame jFrame;
		

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
	 * @param imageFrame the parent frame
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

    public void showMessage(String message, String title) 
    {
        ImagoGui.showMessage(this, message, title);
    }
    
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
    	if (!this.children.contains(frame))
    	{
    		this.children.add(frame);
    	}
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
    

    // ===================================================================
    // Implementation of the AlgoListener interface
    
    /**
     * Default (empty) implementation for monitoring changes in the progression
     * of an algorithm.
     * 
     * In the current implementation, this method is empty, and management of
     * algorithm events is left to overriding classes.
     * 
     * @param evt
     *            the algorithm event
     */
    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
    }
    
    /**
     * Default (empty) implementation for monitoring changes in the status of an
     * algorithm.
     * 
     * In the current implementation, this method is empty, and management of
     * algorithm events is left to overriding classes.
     * 
     * @param evt
     *            the algorithm event
     */
    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
    }
}
