/**
 * 
 */
package imago.app.shape.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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
public class JsonGeometryWriter extends JsonWriter
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
        try(JsonGeometryWriter writer = new JsonGeometryWriter(new PrintWriter(stringWriter)))
        {
            writer.writeGeometry(geom);
        }
        catch(IOException ex)
        {
            throw ex; 
        }
        return stringWriter.toString();
    }
    
    
    // =============================================================
    // Class members
    
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
    public JsonGeometryWriter(Writer writer)
    {
        super(writer);
    }
    
    
    // =============================================================
    // Public methods
    
    public JsonGeometryWriter setCoordinateNumberFormat(String format)
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
        this.beginObject();
        
        // switch processing depending on geometry type
        switch (geom)
        {
            case Point2D point -> 
            {
                name("type").value("Point2D");
                name("coordinates");
                writeCoordinates(point, numberFormat);
            }
            case Polyline2D poly -> 
            {
                name("type").value(poly.isClosed() ? "LinearRing2D" : "LineString2D");
                name("coordinates");
                writeCoordinatesArray(poly.vertexPositions(), numberFormat);
            }
            case Polygon2D poly -> 
            {
                name("type").value("SimplePolygon2D");
                name("coordinates");
                writeCoordinatesArray(poly.vertexPositions(), numberFormat);
            }
            case LineSegment2D seg -> 
            {
                name("type").value("LineSegment2D");

                Point2D p1 = seg.getP1();
                name("p1").beginObject();
                name("coordinates");
                writeCoordinates(p1, numberFormat);
                endObject();
                
                Point2D p2 = seg.getP2();
                name("p2").beginObject();
                name("coordinates");
                writeCoordinates(p2, numberFormat);
                endObject();
            }
            default -> 
            {
                // Default behavior for unknown geometries
                String geomType = geom.getClass().getSimpleName();
                name("type").value(geomType);
                System.err.println("Warning: can not write data for geometry type " + geomType);
            }
        }
        
        endObject();
    }
    
    private void writeCoordinates(Point2D p, String numberFormat) throws IOException
    {
        String pattern = "[ " + numberFormat + ", " + numberFormat + " ]";
        jsonValue(String.format(Locale.ENGLISH, pattern, p.x(), p.y()));
    }
    
    private void writeCoordinatesArray(Collection<Point2D> coords, String numberFormat) throws IOException
    {
        String pattern = "[ " + numberFormat + ", " + numberFormat + " ]";
        beginArray();
        for (Point2D p : coords)
        {
            jsonValue(String.format(Locale.ENGLISH, pattern, p.x(), p.y()));
        }
        endArray();
    }
}
