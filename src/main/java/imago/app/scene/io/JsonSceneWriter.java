/**
 * 
 */
package imago.app.scene.io;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import com.google.gson.stream.JsonWriter;

import imago.app.scene.*;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.Polyline2D;

/**
 * Writes the contents of nodes into text file using the JSON format.
 * 
 * @see JsonSceneReader
 * @see JsonWriter
 * 
 * @author dlegland
 *
 */
public class JsonSceneWriter
{
    // =============================================================
    // Static classes
	
    /**
     * Utility method that converts a Node into a String representation using
     * the JSON format.
     * 
     * @param node
     *            the Node to convert into JSON
     * @return a String representation of the node in JSON format
     * @throws IOException
     *             if a string conversion problem occurred
     */
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
	
	/**
     * Creates a new SceneWriter in JSON format, using the specified JsonWriter.
     * 
     * @param writer
     *            the instance of JsonWriter to write into.
     */
	public JsonSceneWriter(JsonWriter writer)
	{
		this.writer = writer;
	}
	
    /**
     * Creates a new SceneWriter in JSON format, using the specified Writer.
     * The writer is internally converted into a new JsonWriter.
     * 
     * @param writer
     *            the instance of Writer to write into.
     */
	public JsonSceneWriter(java.io.Writer writer)
	{
		this.writer = new JsonWriter(new PrintWriter(writer));
	}
	
    
	// =============================================================
    // Public methods
	
	/**
     * Writes the content of the specified node into this writer.
     * 
     * @see #writeGeometry(net.sci.geom.Geometry)
     * @see #writeStyle(imago.app.scene.Style)
     * 
     * @param node
     *            an instance of a Node in a Scene.
     * @throws IOException
     *             if a writing problem occurred.
     */
	public void writeNode(Node node) throws IOException
	{
		this.writer.beginObject();
		
		// write common properties
		writeString("name", node.getName());
		writeString("type", node.getClass().getSimpleName());
		writeBoolean("visible", node.isVisible());
		
		if (node instanceof ImageSliceNode)
		{
			this.writer.name("sliceIndex").value(((ImageSliceNode)node).getSliceIndex());
			
			// iterate over children
			this.writer.name("children");
			this.writer.beginArray();
			for (Node child : node.children())
			{
				writeNode(child);
			}
			this.writer.endArray();
		}
        else if (node instanceof ImageSerialSectionsNode)
        {
            // iterate over children
            this.writer.name("children");
            this.writer.beginArray();
            for (Node child : node.children())
            {
                writeNode(child);
            }
            this.writer.endArray();
        }
        else if (node instanceof GroupNode)
        {
            // iterate over children
            this.writer.name("children");
            this.writer.beginArray();
            for (Node child : node.children())
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
	
    /**
     * Writes the content of the specified style into this writer.
     * 
     * @see #writeGeometry(Geometry)
     * @see #writeColor(Color)
     * 
     * @param style
     *            an instance of Style.
     * @throws IOException
     *             if a writing problem occurred.
     */
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
	
    /**
     * Writes the content of the specified geometry into this writer.
     * 
     * @param geom
     *            the geometry instance to save.
     * @throws IOException
     *             if a writing problem occurred.
     */
    public void writeGeometry(Geometry geom) throws IOException
    {
        this.writer.beginObject();
        
        // switch processing depending on geometry type
        if (geom instanceof Point2D)
        {
            Point2D point = (Point2D) geom;
            writeString("type", "Point2D");
            writer.name("x").value(point.x());
            writer.name("y").value(point.y());
        }
        else if (geom instanceof Polyline2D)
        {
            Polyline2D poly = (Polyline2D) geom;
            
            writeString("type", poly.isClosed() ? "LinearRing2D" : "LineString2D");
            writer.name("coords");
            writer.beginArray();

            for (Point2D vertex : poly.vertexPositions())
            {
                writer.jsonValue("[ " + vertex.x() + ", " + vertex.y() + "]");
            }
            writer.endArray();
        }
        else
        {
            // Default behavior for unknown geometries
            String geomType = geom.getClass().getSimpleName();
            writeString("type", geomType);
            System.err.println("Warning: can not write data for geometry type " + geomType);
        }

        this.writer.endObject();
    }

    /**
     * Writes the content of the specified geometry into this writer.
     * 
     * @param geom
     *            the geometry instance to save.
     * @param numberFormat
     *            The format used to write coordinates, e.g. "%7.5f" or "%.3f".
     * @throws IOException
     *             if a writing problem occurred.
     */
    public void writeGeometry(Geometry geom, String numberFormat) throws IOException
    {
        this.writer.beginObject();
        
        // switch processing depending on geometry type
        if (geom instanceof Point2D)
        {
            Point2D point = (Point2D) geom;
            writeString("type", "Point2D");
            writer.name("x").jsonValue(format(numberFormat, point.x()));
            writer.name("y").jsonValue(format(numberFormat, point.y()));
        }
        else if (geom instanceof Polyline2D)
        {
            Polyline2D poly = (Polyline2D) geom;
            
            writeString("type", poly.isClosed() ? "LinearRing2D" : "LineString2D");
            writer.name("coords");
            writer.beginArray();

            String pattern = "[ " + numberFormat + ", " + numberFormat + " ]";
            for (Point2D vertex : poly.vertexPositions())
            {
                writer.jsonValue(String.format(Locale.ENGLISH, pattern, vertex.x(), vertex.y()));
//                writer.jsonValue("[ " + vertex.getX() + ", " + vertex.getY() + "]");
            }
            writer.endArray();
        }
        else
        {
            // Default behavior for unknown geometries
            String geomType = geom.getClass().getSimpleName();
            writeString("type", geomType);
            System.err.println("Warning: can not write data for geometry type " + geomType);
        }

        this.writer.endObject();
    }
    
    private static final String format(String pattern, double value)
    {
        return String.format(Locale.ENGLISH, pattern, value);
    }


	// =============================================================
    // Writing java classes

    /**
     * Writes the content of the specified color into this writer.
     * 
     * @param color
     *            the color to save.
     * @throws IOException
     *             if a writing problem occurred.
     */
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
	
    /**
     * Saves a numeric tag identified by a name.
     * 
     * @param name
     *            the name of the tag.
     * @param value
     *            the numeric value of the tag.
     * @throws IOException
     *             if a writing problem occurred.
     */
	private void writeValue(String name, double value) throws IOException
	{
		this.writer.name(name).value(value);
	}

    /**
     * Saves a boolean tag identified by a name.
     * 
     * @param name
     *            the name of the tag.
     * @param value
     *            the boolean value of the tag.
     * @throws IOException
     *             if a writing problem occurred.
     */
	private void writeBoolean(String name, boolean value) throws IOException
	{
		this.writer.name(name).value(value);
	}

    /**
     * Saves a string identified by a name.
     * 
     * @param name
     *            the name of the tag.
     * @param value
     *            the value of the tag as a string.
     * @throws IOException
     *             if a writing problem occurred.
     */
	private void writeString(String name, String value) throws IOException
	{
		this.writer.name(name).value(value);
	}
}
