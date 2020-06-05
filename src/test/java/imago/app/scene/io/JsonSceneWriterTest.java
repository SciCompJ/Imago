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
