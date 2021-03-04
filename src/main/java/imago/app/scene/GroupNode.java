/**
 * 
 */
package imago.app.scene;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import net.sci.geom.geom2d.Point2D;

/**
 * A node that contains other nodes.
 * 
 * @author dlegland
 */
public class GroupNode extends Node
{
	// ===================================================================
	// Class members

    /**
     * The children node within this group node.
     */
	ArrayList<Node> children = new ArrayList<Node>();
	

	// ===================================================================
	// Constructors

	/**
	 * Creates a new empty Group Node.
	 */
	public GroupNode()
	{
	}
	
    /**
     * Creates a new empty Group Node by specifying its name.
     * 
     * @param name
     *            the name of the group node.
     */
	public GroupNode(String name)
	{
		super(name);
	}

	
	// ===================================================================
	// New methods
	
    /**
     * Retrieves a child node from this group node based on the name of the
     * child.
     * 
     * @param name
     *            the name of a child node
     * @return the child node with the specified name, or null if no child has
     *         this name.
     */
	public Node getChild(String name)
	{
		for (Node child : children)
		{
			if (name.equals(child.name))
			{
				return child;
			}
		}
		return null;
	}
	
	/**
     * Checks if this grouping node a contains a child node with the specified
     * name.
     * 
     * @param name
     *            the name of the child node
     * @return true if this group contains a child with the specified name.
     */
	public boolean hasChildWithName(String name)
	{
		for (Node child : children)
		{
			if (name.equals(child.name))
			{
				return true;
			}
		}
		return false;
	}

	/**
     * Returns the number of children in this group.
     * 
     * @return the number of children in this group.
     */
	public int childrenCount()
	{
		return children.size();
	}
	
	/**
     * Adds a new child node to this group.
     * 
     * @param node
     *            the child node to add.
     */
	public void addNode(Node node)
	{
		children.add(node);
	}
	
    /**
     * Removes a new child node from this group.
     * 
     * @param node
     *            the child node to remove.
     */
	public void removeNode(Node node)
	{
		children.remove(node);
	}
	
    /**
     * Removes all the child nodes from this group.
     */
	public void clear()
	{
		children.clear();
	}
	
	
	// ===================================================================
	// Implementation of Node methods

	@Override
	public Iterable<Node> children()
	{
		return Collections.unmodifiableList(children);
	}

	@Override
	public boolean isLeaf()
	{
		return children.isEmpty();
	}

	
	@Override
	public void printTree(PrintStream stream, int nIndents)
	{
		String str = "";
		for (int i = 0; i < nIndents; i++)
		{
			str = str + "  ";
		}
		String nameString = (name != null && !name.isEmpty()) ? name : "(no name)";
		stream.println(str + "[GroupNode] " + nameString);
		
		for (Node node : children)
		{
			node.printTree(stream, nIndents + 1);
		}
	}

	/**
     * A main method used for testing purpose.
     * 
     * @param args
     *            optional arguments (not used)
     */
	public static final void main(String... args)
	{
		ShapeNode node1 = new ShapeNode(new Point2D(20, 10));
		ShapeNode node2 = new ShapeNode(new Point2D(30, 10));
		ShapeNode node3 = new ShapeNode(new Point2D(30, 20));
		ShapeNode node4 = new ShapeNode(new Point2D(20, 20));
		
		GroupNode group = new GroupNode("Group");
		group.addNode(node1);
		group.addNode(node2);
		group.addNode(node3);
		group.addNode(node4);
		
		group.printTree(System.out, 0);
	}
}
