/**
 * 
 */
package imago.app.scene;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
		ShapeNode node = new ShapeNode(geom);
		
		assertTrue(node.isLeaf());
	}

}
