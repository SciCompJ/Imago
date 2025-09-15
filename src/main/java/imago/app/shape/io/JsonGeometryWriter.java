/**
 * 
 */
package imago.app.shape.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;

import com.google.gson.stream.JsonWriter;

import net.sci.geom.Geometry;
import net.sci.geom.geom2d.LineSegment2D;
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
    
    String coordinateNumberFormat = "%.1f";
    
    
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
    private void writeGeometry(Geometry geom, String numberFormat) throws IOException
    {
        this.writer.beginObject();
        
        // switch processing depending on geometry type
        switch (geom)
        {
            case Point2D point -> 
            {
                writer.name("type").value("Point2D");
                writer.name("coordinates");
                writeCoordinates(point, numberFormat);
            }
            case Polyline2D poly -> 
            {
                writer.name("type").value(poly.isClosed() ? "LinearRing2D" : "LineString2D");
                writer.name("coordinates");
                writeCoordinatesArray(poly.vertexPositions(), numberFormat);
            }
            case Polygon2D poly -> 
            {
                writer.name("type").value("SimplePolygon2D");
                writer.name("coordinates");
                writeCoordinatesArray(poly.vertexPositions(), numberFormat);
            }
            case LineSegment2D seg -> 
            {
                writer.name("type").value("LineSegment2D");

                Point2D p1 = seg.getP1();
                writer.name("p1").beginObject();
                writer.name("coordinates");
                writeCoordinates(p1, numberFormat);
                writer.endObject();
                
                Point2D p2 = seg.getP2();
                writer.name("p2").beginObject();
                writer.name("coordinates");
                writeCoordinates(p2, numberFormat);
                writer.endObject();
            }
            default -> 
            {
                // Default behavior for unknown geometries
                String geomType = geom.getClass().getSimpleName();
                writer.name("type").value(geomType);
                System.err.println("Warning: can not write data for geometry type " + geomType);
            }
        }
        
        this.writer.endObject();
    }
    
    private void writeCoordinates(Point2D p, String numberFormat) throws IOException
    {
        String pattern = "[ " + numberFormat + ", " + numberFormat + " ]";
        writer.jsonValue(String.format(Locale.ENGLISH, pattern, p.x(), p.y()));
    }
    
    private void writeCoordinatesArray(Collection<Point2D> coords, String numberFormat) throws IOException
    {
        String pattern = "[ " + numberFormat + ", " + numberFormat + " ]";
        writer.beginArray();
        for (Point2D p : coords)
        {
            writer.jsonValue(String.format(Locale.ENGLISH, pattern, p.x(), p.y()));
        }
        writer.endArray();
    }
}
