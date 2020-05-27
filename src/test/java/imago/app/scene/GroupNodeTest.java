/**
 * 
 */
package imago.app.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import net.sci.geom.geom2d.Point2D;

/**
 * @author dlegland
 *
 */
public class GroupNodeTest
{

	/**
	 * Test method for {@link imago.app.scene.GroupNode#isLeaf()}.
	 */
	@Test
	public void testIsLeaf()
	{
		GroupNode group = new GroupNode("Group");
		group.addNode(new ShapeNode(new Point2D(20, 10)));
		group.addNode(new ShapeNode(new Point2D(30, 10)));
		group.addNode(new ShapeNode(new Point2D(30, 20)));
		group.addNode(new ShapeNode(new Point2D(20, 20)));
		
		assertFalse(group.isLeaf());
	}

	/**
	 * Test method for {@link imago.app.scene.GroupNode#childrenCount()}.
	 */
	@Test
	public void testChildrenCount()
	{
		GroupNode group = new GroupNode("Group");
		group.addNode(new ShapeNode(new Point2D(20, 10)));
		group.addNode(new ShapeNode(new Point2D(30, 10)));
		group.addNode(new ShapeNode(new Point2D(30, 20)));
		group.addNode(new ShapeNode(new Point2D(20, 20)));
		
		assertEquals(4, group.childrenCount());
	}

}
