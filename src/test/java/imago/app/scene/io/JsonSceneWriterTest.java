/**
 * 
 */
package imago.app.scene.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import com.google.gson.stream.JsonWriter;

import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;

/**
 * @author dlegland
 *
 */
public class JsonSceneWriterTest
{

	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneWriter#writeNode(imago.app.scene.Node)}.
	 * @throws IOException 
	 */
	@Test
	public void testWriteNode() throws IOException
	{
        LinearRing2D ring = new LinearRing2D();
        ring.addVertex(new Point2D(10, 10));;
        ring.addVertex(new Point2D(20, 10));;
        ring.addVertex(new Point2D(20, 20));;
        ring.addVertex(new Point2D(10, 20));;


		GroupNode root = new GroupNode("root");
		root.addNode(new ShapeNode("point01", new Point2D(40, 30)));
		root.addNode(new ShapeNode("ring01", ring));
		
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(new PrintWriter(stringWriter));
		jsonWriter.setIndent("  ");
		JsonSceneWriter writer = new JsonSceneWriter(jsonWriter);
		
		writer.writeNode(root);
		
		System.out.println(stringWriter.toString());
	}

	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneWriter#writeNode(imago.app.scene.Node)}.
	 * @throws IOException 
	 */
	@Test
	public void testWriteNode_ImageSerialSectionsNode() throws IOException
	{
		// a ring for the slice with index 10
        LinearRing2D ring10 = new LinearRing2D();
        ring10.addVertex(new Point2D(10, 10));
        ring10.addVertex(new Point2D(20, 10));
        ring10.addVertex(new Point2D(20, 20));
        ring10.addVertex(new Point2D(10, 20));

		// another ring for the slice with index 20
        LinearRing2D ring20 = new LinearRing2D();
        ring20.addVertex(new Point2D(12, 12));
        ring20.addVertex(new Point2D(22, 12));
        ring20.addVertex(new Point2D(22, 22));
        ring20.addVertex(new Point2D(12, 22));

        // create the slice nodes
		ImageSliceNode slice10 = new ImageSliceNode("slice10", 10);
		slice10.addNode(new ShapeNode(ring10));
		ImageSliceNode slice20 = new ImageSliceNode("slice20", 20);
		slice20.addNode(new ShapeNode(ring20));

		// create the serial sections node
		ImageSerialSectionsNode groupNode = new ImageSerialSectionsNode("rings");
		groupNode.addSliceNode(slice10);
		groupNode.addSliceNode(slice20);

		// add to the root node
		GroupNode root = new GroupNode("root");
		root.addNode(groupNode);


		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(new PrintWriter(stringWriter));
		jsonWriter.setIndent("  ");
		JsonSceneWriter writer = new JsonSceneWriter(jsonWriter);
		
		writer.writeNode(root);
		
		System.out.println(stringWriter.toString());
	}


	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneWriter#writeNode(imago.app.scene.Node)}.
	 * @throws IOException 
	 */
	@Test
	public void testWriteNode_file() throws IOException
	{
        LinearRing2D ring = new LinearRing2D();
        ring.addVertex(new Point2D(10, 10));;
        ring.addVertex(new Point2D(20, 10));;
        ring.addVertex(new Point2D(20, 20));;
        ring.addVertex(new Point2D(10, 20));;


		GroupNode root = new GroupNode("root");
		root.addNode(new ShapeNode("point01", new Point2D(40, 30)));
		root.addNode(new ShapeNode("ring01", ring));
		
		FileWriter fileWriter = new FileWriter("demoJson.json");
//		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(new PrintWriter(fileWriter));
		jsonWriter.setIndent("  ");
		JsonSceneWriter writer = new JsonSceneWriter(jsonWriter);
		
		writer.writeNode(root);
		fileWriter.close();
//		System.out.println(stringWriter.toString());
	}

}
