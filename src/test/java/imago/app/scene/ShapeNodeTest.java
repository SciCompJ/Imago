/**
 * 
 */
package imago.app.scene;

import static org.junit.Assert.*;

import org.junit.Test;

import imago.app.shape.Shape;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;

/**
 * @author dlegland
 *
 */
public class ShapeNodeTest
{

	/**
	 * Test method for {@link imago.app.scene.ShapeNode#isLeaf()}.
	 */
	@Test
	public void testIsLeaf()
	{
		Geometry2D geom = new Point2D(20, 10);
		Shape shape = new Shape(geom);
		ShapeNode node = new ShapeNode(shape);
		
		assertTrue(node.isLeaf());
	}

}
