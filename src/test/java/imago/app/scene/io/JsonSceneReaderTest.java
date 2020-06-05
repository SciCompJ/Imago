/**
 * 
 */
package imago.app.scene.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import com.google.gson.stream.JsonReader;

import imago.app.scene.io.JsonSceneReader;
import imago.app.scene.io.JsonSceneWriter;
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
	 */
	@Test
	public void testReadNode()
	{
		fail("Not yet implemented"); // TODO
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
		
		System.out.println(json);
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		
		Point2D res = (Point2D) sceneReader.readGeometry();
		assertEquals(point.getX(), res.getX(), .01);
		assertEquals(point.getY(), res.getY(), .01);
	}

	/**
	 * Test method for {@link imago.app.scene.io.JsonSceneReader#readGeometry()}.
	 * @throws IOException 
	 */
	@Test
	public void testReadGeometry_LinearRing2D() throws IOException
	{
        LinearRing2D ring = new LinearRing2D();
        ring.addVertex(new Point2D(10, 10));;
        ring.addVertex(new Point2D(20, 10));;
        ring.addVertex(new Point2D(20, 20));;
        ring.addVertex(new Point2D(10, 20));;
		
		StringWriter stringWriter = new StringWriter();
		JsonSceneWriter writer = new JsonSceneWriter(new PrintWriter(stringWriter));
		writer.writeGeometry(ring);
		String json = stringWriter.toString();
		
		System.out.println(json);
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		
		LinearRing2D res = (LinearRing2D) sceneReader.readGeometry();
//		assertEquals(color.getBlue(), res.getBlue());
//		assertEquals(color.getGreen(), res.getGreen());
//		assertEquals(color.getRed(), res.getRed());
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
		
		System.out.println(json);
		
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		
		Color res = sceneReader.readColor();
		assertEquals(color.getBlue(), res.getBlue());
		assertEquals(color.getGreen(), res.getGreen());
		assertEquals(color.getRed(), res.getRed());
	}

}
