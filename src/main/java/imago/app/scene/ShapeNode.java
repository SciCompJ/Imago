/**
 * 
 */
package imago.app.scene;

import java.io.PrintStream;
import java.util.ArrayList;

import imago.app.shape.Style;
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

    /** 
     * The shape contained in this node. 
     */
    Geometry geometry;

    /**
     * The style used to draw the geometry stored within this node.
     */
	Style style = new Style();
 	
	
    // =============================================================
    // Constructor
	
    /**
     * Creates a new shape node by specifying the name, the geometry and the
     * style.
     * 
     * @param name
     *            the name of this node.
     * @param geometry
     *            the geometry of the shape.
     * @param style
     *            the style used to draw the shape.
     */
	public ShapeNode(String name, Geometry geometry, Style style)
	{
		super(name);
		this.geometry = geometry;
		this.style = style;
	}
	
    /**
     * Creates a new shape node by specifying its name and its geometry.
     * 
     * @param name
     *            the name of this node.
     * @param geometry
     *            the geometry of the shape.
     */
	public ShapeNode(String name, Geometry geometry)
	{
		super(name);
		this.geometry = geometry;
	}
	
    /**
     * Creates a new shape node by specifying only its geometry. An empty name
     * is used by default.
     * 
     * @param geometry
     *            the geometry of the shape.
     */
	public ShapeNode(Geometry geometry)
	{
		this.geometry = geometry;
	}
	
	
    // =============================================================
    // Specific methods

	/**
     * Returns the geometry associated to this shape.
     * 
     * @return the geometry associated to this shape.
     */
	public Geometry getGeometry()
	{
		return this.geometry;
	}
	
    /**
     * Returns the style associated to this shape.
     * 
     * @return the style associated to this shape.
     */
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
		String nameString = (name != null && !name.isEmpty()) ? "\"" + name + "\"" : "(empty)";
		String geomString = getGeometry().getClass().getSimpleName();
		stream.println(str + "[ShapeNode] name=" + nameString + " geometry=" + geomString);
	}
	
	
    /**
     * A main method used for testing purpose.
     * 
     * @param args
     *            optional arguments (not used)
     */
	public static final void main(String... args)
	{
		Geometry2D geom = new Point2D(20, 10);
		ShapeNode node = new ShapeNode("sample Shape", geom);
		
		node.printTree(System.out, 0);
	}
}
