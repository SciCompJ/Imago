/**
 * 
 */
package imago.app.scene.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import com.google.gson.stream.JsonReader;

import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.shape.Style;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;

/**
 * @author dlegland
 *
 */
public class JsonSceneReaderTest
{

	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneReader#readNode()}.
	 * @throws IOException 
	 */
	@Test
	public void testReadNode_ShapeNode() throws IOException
	{
		// a ring for the slice with index 10
		LinearRing2D ring = LinearRing2D.create();
		ring.addVertex(new Point2D(10, 10));
		ring.addVertex(new Point2D(20, 10));
		ring.addVertex(new Point2D(20, 20));
		ring.addVertex(new Point2D(10, 20));
		
		ShapeNode node = new ShapeNode("ring", ring);
		
		String json = JsonSceneWriter.toJson(node);
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		
		Node res = sceneReader.readNode();
		assertTrue(res instanceof ShapeNode);
	}

	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneReader#readNode()}.
	 * @throws IOException 
	 */
	@Test
	public void testReadNode_GroupNode() throws IOException
	{
		// a ring for the slice with index 10
		LinearRing2D ring10 = LinearRing2D.create();
		ring10.addVertex(new Point2D(10, 10));;
		ring10.addVertex(new Point2D(20, 10));;
		ring10.addVertex(new Point2D(20, 20));;
		ring10.addVertex(new Point2D(10, 20));;

		// another ring for the slice with index 20
		LinearRing2D ring20 = LinearRing2D.create();
		ring20.addVertex(new Point2D(12, 12));;
		ring20.addVertex(new Point2D(22, 12));;
		ring20.addVertex(new Point2D(22, 22));;
		ring20.addVertex(new Point2D(12, 22));;

		// create the slice nodes
		GroupNode group = new GroupNode("group");
		group.addNode(new ShapeNode("ring10", ring10));
		group.addNode(new ShapeNode("ring20", ring20));
		
		String json = JsonSceneWriter.toJson(group);
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		
		Node node = sceneReader.readNode();
		
		assertTrue(node instanceof GroupNode);
		assertFalse(node.isLeaf());
		for (Node child : node.children())
		{
			assertTrue(child instanceof ShapeNode);
		}
	}

	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneReader#readNode()}.
	 * @throws IOException 
	 */
	@Test
	public void testReadNode_ImageSerialSectionsNode() throws IOException
	{
		// a ring for the slice with index 10
		LinearRing2D ring10 = LinearRing2D.create();
		ring10.addVertex(new Point2D(10, 10));;
		ring10.addVertex(new Point2D(20, 10));;
		ring10.addVertex(new Point2D(20, 20));;
		ring10.addVertex(new Point2D(10, 20));;

		// another ring for the slice with index 20
		LinearRing2D ring20 = LinearRing2D.create();
		ring20.addVertex(new Point2D(12, 12));;
		ring20.addVertex(new Point2D(22, 12));;
		ring20.addVertex(new Point2D(22, 22));;
		ring20.addVertex(new Point2D(12, 22));;

		// create the slice nodes
		ImageSliceNode slice10 = new ImageSliceNode("slice10", 10);
		slice10.addNode(new ShapeNode(ring10));
		ImageSliceNode slice20 = new ImageSliceNode("slice20", 20);
		slice20.addNode(new ShapeNode(ring20));

		// create the serial sections node
		ImageSerialSectionsNode groupNode = new ImageSerialSectionsNode("rings");
		groupNode.addSliceNode(slice10);
		groupNode.addSliceNode(slice20);

		String json = JsonSceneWriter.toJson(groupNode);
		System.out.println(json);
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		
		Node node = sceneReader.readNode();
		
		assertTrue(node instanceof ImageSerialSectionsNode);
		assertFalse(node.isLeaf());
		for (Node child : node.children())
		{
			assertTrue(child instanceof ImageSliceNode);
		}
	}

	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneReader#readNode()}.
	 * @throws IOException 
	 */
	@Test
	public void testReadStyle() throws IOException
	{
		Style style = new Style();
		style.setColor(Color.MAGENTA);
		style.setLineWidth(1.4);
		
		StringWriter stringWriter = new StringWriter();
		JsonSceneWriter writer = new JsonSceneWriter(new PrintWriter(stringWriter));
		writer.writeStyle(style);
		String json = stringWriter.toString();
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		Style res = sceneReader.readStyle();
		
		assertEquals(style.getColor(), res.getColor());
		assertEquals(style.getLineWidth(), res.getLineWidth(), .1);
	}
	
	
	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneReader#readGeometry()}.
	 * @throws IOException 
	 */
	@Test
	public void testReadGeometry_Point2D() throws IOException
	{
		Point2D point = new Point2D(40, 30);
		
		StringWriter stringWriter = new StringWriter();
		JsonSceneWriter writer = new JsonSceneWriter(new PrintWriter(stringWriter));
		writer.writeGeometry(point);
		String json = stringWriter.toString();
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		
		Point2D res = (Point2D) sceneReader.readGeometry();
		assertEquals(point.x(), res.x(), .01);
		assertEquals(point.y(), res.y(), .01);
	}

	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneReader#readGeometry()}.
	 * @throws IOException 
	 */
	@Test
	public void testReadGeometry_LinearRing2D() throws IOException
	{
        LinearRing2D ring = LinearRing2D.create();
        ring.addVertex(new Point2D(10, 10));;
        ring.addVertex(new Point2D(20, 10));;
        ring.addVertex(new Point2D(20, 20));;
        ring.addVertex(new Point2D(10, 20));;
		
		StringWriter stringWriter = new StringWriter();
		JsonSceneWriter writer = new JsonSceneWriter(new PrintWriter(stringWriter));
		writer.writeGeometry(ring);
		String json = stringWriter.toString();
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		
		LinearRing2D res = (LinearRing2D) sceneReader.readGeometry();
		assertEquals(4, res.vertexCount());
	}


	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneReader#readColor()}.
	 * @throws IOException 
	 */
	@Test
	public void testReadColor() throws IOException
	{
		Color color = new Color(40, 80, 120);
		
		StringWriter stringWriter = new StringWriter();
		JsonSceneWriter writer = new JsonSceneWriter(new PrintWriter(stringWriter));
		writer.writeColor(color);
		String json = stringWriter.toString();
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		
		Color res = sceneReader.readColor();
		assertEquals(color.getBlue(), res.getBlue());
		assertEquals(color.getGreen(), res.getGreen());
		assertEquals(color.getRed(), res.getRed());
	}

}
