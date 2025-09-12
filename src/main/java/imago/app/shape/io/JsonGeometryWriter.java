/**
 * 
 */
package imago.app.shape.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import com.google.gson.stream.JsonWriter;

import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.Polygon2D;
import net.sci.geom.polygon2d.Polyline2D;

/**
 * Writes an instance of {@code Geometry} into a text file using the JSON
 * format.
 * 
 * @see JsonGeometryReader
 * @see JsonWriter
 * 
 * @author dlegland
 *
 */
public class JsonGeometryWriter
{
    // =============================================================
    // Static classes
    
    /**
     * Utility method that converts an instance of {@code Geometry} into a
     * String representation using the JSON format.
     * 
     * @param geom
     *            the Geometry to convert into JSON
     * @return a String representation of the node in JSON format
     * @throws IOException
     *             if a string conversion problem occurred
     */
    public static final String toJson(Geometry geom) throws IOException
    {
        StringWriter stringWriter = new StringWriter();
        JsonGeometryWriter writer = new JsonGeometryWriter(new PrintWriter(stringWriter));
        writer.writeGeometry(geom);
        return stringWriter.toString();
    }
    
    
    // =============================================================
    // Class members
    
    JsonWriter writer;
    
    String coordinateNumberFormat = "%.3f";
    
    
    // =============================================================
    // Constructors
    
    /**
     * Creates a new {@code JsonGeometryWriter} in JSON format, using the
     * specified instance of {@code JsonWriter}.
     * 
     * @param writer
     *            the instance of JsonWriter to write into.
     */
    public JsonGeometryWriter(JsonWriter writer)
    {
        this.writer = writer;
    }
    
    /**
     * Creates a new {@code JsonGeometryWriter} in JSON format, using the
     * specified instance of {@code Writer}. The writer is internally converted
     * into a new {@code JsonWriter}.
     * 
     * @param writer
     *            the instance of Writer to write into.
     */
    public JsonGeometryWriter(java.io.Writer writer)
    {
        this.writer = new JsonWriter(new PrintWriter(writer));
    }
    
    
    // =============================================================
    // Public methods
    
    public JsonGeometryWriter coordinateNumberFormat(String format)
    {
        this.coordinateNumberFormat = format;
        return this;
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
        writeGeometry(geom, this.coordinateNumberFormat);
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
        if (geom instanceof Point2D point)
        {
            writeString("type", "Point2D");
            writer.name("x").jsonValue(format(numberFormat, point.x()));
            writer.name("y").jsonValue(format(numberFormat, point.y()));
        }
        else if (geom instanceof Polyline2D poly)
        {
            writeString("type", poly.isClosed() ? "LinearRing2D" : "LineString2D");
            writer.name("coords");
            writer.beginArray();
            
            String pattern = "[ " + numberFormat + ", " + numberFormat + " ]";
            for (Point2D p : poly.vertexPositions())
            {
                writer.jsonValue(String.format(Locale.ENGLISH, pattern, p.x(), p.y()));
                // writer.jsonValue("[ " + vertex.getX() + ", " + vertex.getY()
                // + "]");
            }
            writer.endArray();
        }
        else if (geom instanceof Polygon2D poly)
        {
            writeString("type", "SimplePolygon2D");
            writer.name("coords");
            writer.beginArray();
            
            String pattern = "[ " + numberFormat + ", " + numberFormat + " ]";
            for (Point2D p : poly.vertexPositions())
            {
                writer.jsonValue(String.format(Locale.ENGLISH, pattern, p.x(), p.y()));
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
