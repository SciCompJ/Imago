/**
 * 
 */
package imago.app.scene;

/**
 * Abstract superclass for all nodes in a scene graph.
 * 
 * @see GroupNode
 * @see ShapeNode
 * 
 * @author dlegland
 */
public abstract class Node
{
    // =============================================================
    // Class fields

    /**
     * The optional name of this node, that can be used to identify it in GUI
     * widgets or when saved in text files.
     */
    String name = "";
    
    /** 
     * Visibility flag (default true). 
     */
    boolean visible = true;

    
    // =============================================================
    // Constructor
    
    protected Node()
    {
    }

    protected Node(String name)
    {
    	this.name = name;
    }
    
   
    // =============================================================
    // General methods

    /**
     * @return the set of children in this node (if this node is not a leaf).
     */
    public abstract Iterable<? extends Node> children();
    
    /**
     * @return true if this node is a leaf, i.e. it does not contains any other node.
     */
    public abstract boolean isLeaf();
    
   
    
    // =============================================================
    // Getters and setters
    
    /**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	
    // =============================================================
    // Utility methods

	/**
	 * Displays the contents of this node, and of its children if any, on the
	 * specified stream.
	 * 
	 * @param stream
	 *            the stream to print the tree on
	 * @param nIndents
	 *            the initial number of indentations, that is increased at each
	 *            sub-level of the tree.
	 */
    public abstract void printTree(java.io.PrintStream stream, int nIndents);
}
