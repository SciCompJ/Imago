/**
 * 
 */
package imago.app.shape.io;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.stream.JsonReader;

import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.LineString2D;
import net.sci.geom.polygon2d.LinearRing2D;
import net.sci.geom.polygon2d.Polygon2D;

/**
 * Reads the content of a {@code Geometry} from a text file in JSON format.
 * 
 * @see JsonGeometryWriter
 * 
 * @author dlegland
 *
 */
public class JsonGeometryReader
{
    // =============================================================
    // Class member
    
    /**
     * The JsonReader instance used to parse data from.
     */
    JsonReader reader;
    
    
    // =============================================================
    // Constructor
    
    /**
     * Creates a new JsonGeometryReader from an instance of JsonReader.
     * 
     * @param reader
     *            an instance of JsonReader.
     */
    public JsonGeometryReader(JsonReader reader)
    {
        this.reader = reader;
    }
    
    
    // =============================================================
    // Read CS4J items
    
    /**
     * Read the content of a Geometry saved in JSON format.
     * 
     * @return a new Geometry.
     * @throws IOException
     *             if a reading problem occurred.
     */
    public Geometry readGeometry() throws IOException
    {
        Geometry geom;
        reader.beginObject();
        
        // read type
        reader.nextName();
        String type = reader.nextString();
        
        if (type.equalsIgnoreCase("Point2D"))
        {
            // read x field
            reader.nextName();
            double x = reader.nextDouble();
            // read y field
            reader.nextName();
            double y = reader.nextDouble();
            geom = new Point2D(x, y);
        }
        else if (type.equalsIgnoreCase("LineString2D"))
        {
            // read coords field
            reader.nextName();
            ArrayList<Point2D> coords = readPoint2DArray();
            geom = LineString2D.create(coords);
        }
        else if (type.equalsIgnoreCase("LinearRing2D"))
        {
            // read coords field
            reader.nextName();
            ArrayList<Point2D> coords = readPoint2DArray();
            geom = LinearRing2D.create(coords);
        }
        else if (type.equalsIgnoreCase("SimplePolygon2D"))
        {
            // read coords field
            reader.nextName();
            ArrayList<Point2D> coords = readPoint2DArray();
            geom = Polygon2D.create(coords);
        }
        else
        {
            throw new RuntimeException("Unable to parse geometry with type: " + type);
        }
        reader.endObject();
        
        return geom;
    }
    
    /**
     * Read the content of an array of Point2D saved in JSON format.
     * 
     * @return a new Array of Point2D.
     * @throws IOException
     *             if a reading problem occurred.
     */
    private ArrayList<Point2D> readPoint2DArray() throws IOException
    {
        ArrayList<Point2D> points = new ArrayList<Point2D>();
        
        reader.beginArray();
        while (reader.hasNext())
        {
            reader.beginArray();
            double x = reader.nextDouble();
            double y = reader.nextDouble();
            reader.endArray();
            points.add(new Point2D(x, y));
        }
        reader.endArray();
        
        return points;
    }
    
    /**
     * Close the underlying reader.
     * 
     * @throws IOException
     *             if a problem occurred when closing the reader.
     */
    public void close() throws IOException
    {
        this.reader.close();
    }
}
