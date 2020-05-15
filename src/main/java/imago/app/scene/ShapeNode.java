/**
 * 
 */
package imago.app.scene;

import java.io.PrintStream;
import java.util.ArrayList;

import imago.app.shape.Shape;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;

/**
 * A Node that contains a shape.
 * 
 * @author dlegland
 */
public class ShapeNode extends Node
{
    // =============================================================
    // Class members

	/** The shape contained in this node. */
	Shape shape;

	
    // =============================================================
    // Constructor

	public ShapeNode(String name, Shape shape)
	{
		super(name);
		this.shape = shape;
	}
	
	public ShapeNode(Shape shape)
	{
		this.shape = shape;
	}
	
	
    // =============================================================
    // Specific methods

	public Shape getShape()
	{
		return this.shape;
	}
	
	
    // =============================================================
    // Implementation of Node methods

	@Override
	public Iterable<Node> children()
	{
		return new ArrayList<Node>(0);
	}

	@Override
	public boolean isLeaf()
	{
		return true;
	}


    // =============================================================
    // Utility methods

	@Override
	public void printTree(PrintStream stream, int nIndents)
	{
		String str = "";
		for (int i = 0; i < nIndents; i++)
		{
			str = str + "  ";
		}
		String nameString = (name != null && !name.isEmpty()) ? name : "(no name)";
		stream.println(str + "[ShapeNode] " + nameString);
	}
	
	
	public static final void main(String... args)
	{
		Geometry2D geom = new Point2D(20, 10);
		Shape shape = new Shape(geom);
		ShapeNode node = new ShapeNode(shape);
		
		node.printTree(System.out, 0);
	}
}
