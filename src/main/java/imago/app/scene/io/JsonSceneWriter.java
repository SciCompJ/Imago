/**
 * 
 */
package imago.app.scene.io;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gson.stream.JsonWriter;

import imago.app.scene.GroupNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.scene.Style;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.Polyline2D;

/**
 * @author dlegland
 *
 */
public class JsonSceneWriter
{
    // =============================================================
    // Static classes
	
	public static final String toJson(Node node) throws IOException
	{
		StringWriter stringWriter = new StringWriter();
		JsonSceneWriter writer = new JsonSceneWriter(new PrintWriter(stringWriter));
		writer.writeNode(node);
		return stringWriter.toString();
	}
	
    // =============================================================
    // Class members
	
	JsonWriter writer;

	
    // =============================================================
    // Constructors
	
	public JsonSceneWriter(JsonWriter writer)
	{
		this.writer = writer;
	}
	
	public JsonSceneWriter(java.io.Writer out)
	{
		this.writer = new JsonWriter(new PrintWriter(out));
	}
	
    
	// =============================================================
    // Public methods
	
	public void writeNode(Node node) throws IOException
	{
		this.writer.beginObject();
		
		// write common properties
		writeString("name", node.getName());
		writeBoolean("visible", node.isVisible());
		
		if (node instanceof GroupNode)
		{
			// iterate over children
			this.writer.name("children");
			this.writer.beginArray();
			for (Node child : ((GroupNode) node).children())
			{
				writeNode(child);
			}
			this.writer.endArray();
		}
		else if (node instanceof ShapeNode)
		{
			ShapeNode shapeNode = (ShapeNode) node;
			
			// write style
			this.writer.name("style");
			writeStyle(shapeNode.getStyle());

			// write geometry
			this.writer.name("geometry");
			writeGeometry(shapeNode.getGeometry());
		}
		else
		{
			throw new RuntimeException("Could not write node with class: " + node.getClass().getName());
		}
		this.writer.endObject();
	}


    // =============================================================
    // Writing Imago classes
	
	public void writeStyle(Style style) throws IOException
	{
		this.writer.beginObject();
		
		// write common properties
		this.writer.name("color");
		writeColor(style.getColor());
		writeValue("lineWidth", style.getLineWidth());

		this.writer.endObject();
	}

	
    // =============================================================
    // Writing CS4J classes

	public void writeGeometry(Geometry geom) throws IOException
	{
		this.writer.beginObject();
		
		// write common properties
		writeString("type", geom.getClass().getSimpleName());
		if (geom instanceof Point2D)
		{
			Point2D point = (Point2D) geom;
			writer.name("x").value(point.getX());
			writer.name("y").value(point.getY());
		}
		else if (geom instanceof Polyline2D)
		{
			Polyline2D poly = (Polyline2D) geom;

			writer.name("coords");
			writer.beginArray();

			for (Point2D vertex : poly.vertexPositions())
			{
				writer.jsonValue("[ " + vertex.getX() + ", " + vertex.getY() + "]");
			}
			writer.endArray();
		}

		this.writer.endObject();
	}


	// =============================================================
    // Writing java classes

	public void writeColor(Color color) throws IOException
	{
		writer.beginObject();
		writer.name("red").value(color.getRed());
		writer.name("green").value(color.getGreen());
		writer.name("blue").value(color.getBlue());
		writer.endObject();
	}

	
    // =============================================================
    // writing primitive data
	
	private void writeValue(String name, double value) throws IOException
	{
		this.writer.name(name).value(value);
	}

	private void writeBoolean(String name, boolean value) throws IOException
	{
		this.writer.name(name).value(value);
	}

	private void writeString(String name, String value) throws IOException
	{
		this.writer.name(name).value(value);
	}
}
