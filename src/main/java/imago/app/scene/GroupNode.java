/**
 * 
 */
package imago.app.scene;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import imago.app.shape.Shape;
import net.sci.geom.geom2d.Point2D;

/**
 * A node that contains other nodes.
 * 
 * @author dlegland
 */
public class GroupNode extends Node
{
	ArrayList<Node> children = new ArrayList<Node>();
	

	public GroupNode()
	{
	}
	
	public GroupNode(String name)
	{
		this.name = name;
	}

	
	public int childrenCount()
	{
		return children.size();
	}
	
	public void addNode(Node node)
	{
		children.add(node);
	}
	
	public void removeNode(Node node)
	{
		children.remove(node);
	}
	
	public void clear()
	{
		children.clear();
	}
	
	
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

	
	public static final void main(String... args)
	{
		ShapeNode node1 = new ShapeNode(new Shape(new Point2D(20, 10)));
		ShapeNode node2 = new ShapeNode(new Shape(new Point2D(30, 10)));
		ShapeNode node3 = new ShapeNode(new Shape(new Point2D(30, 20)));
		ShapeNode node4 = new ShapeNode(new Shape(new Point2D(20, 20)));
		
		GroupNode group = new GroupNode("Group");
		group.addNode(node1);
		group.addNode(node2);
		group.addNode(node3);
		group.addNode(node4);
		
		group.printTree(System.out, 0);
	}
}
