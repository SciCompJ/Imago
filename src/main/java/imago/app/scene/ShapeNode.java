/**
 * 
 */
package imago.app.scene;

import java.io.PrintStream;
import java.util.ArrayList;

import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;

/**
 * A Node that contains a shape, and drawing information.
 * 
 * @author dlegland
 */
public class ShapeNode extends Node
{
    // =============================================================
    // Class members

	/** The shape contained in this node. */
    Geometry geometry;

	Style style = new Style();
 	
	
    // =============================================================
    // Constructor

	public ShapeNode(String name, Geometry geometry, Style style)
	{
		super(name);
		this.geometry = geometry;
		this.style = style;
	}
	
	public ShapeNode(String name, Geometry geometry)
	{
		super(name);
		this.geometry = geometry;
	}
	
	public ShapeNode(Geometry geometry)
	{
		this.geometry = geometry;
	}
	
	
    // =============================================================
    // Specific methods

	public Geometry getGeometry()
	{
		return this.geometry;
	}
	
	public Style getStyle()
	{
		return this.style;
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
		ShapeNode node = new ShapeNode(geom);
		
		node.printTree(System.out, 0);
	}
}
