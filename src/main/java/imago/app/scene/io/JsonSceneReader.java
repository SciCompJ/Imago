/**
 * 
 */
package imago.app.scene.io;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.stream.JsonReader;

import imago.app.scene.Node;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.LineString2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;

/**
 * @author dlegland
 *
 */
public class JsonSceneReader
{
    // =============================================================
    // Class member
	
	JsonReader reader;
	
	
    // =============================================================
    // Constructor

	public JsonSceneReader(JsonReader reader)
	{
		this.reader = reader;
	}

	
    // =============================================================
    // Read Imago items
	
	public Node readNode() throws IOException
	{
		Node node = null;
		reader.beginObject();
		
		reader.endObject();
		return node;
	}


	// =============================================================
    // Read CS4J items
	
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
			geom = new LineString2D(coords);
		}
		else if (type.equalsIgnoreCase("LinearRing2D"))
		{
			// read coords field
			reader.nextName();
			ArrayList<Point2D> coords = readPoint2DArray(); 
			geom = new LinearRing2D(coords);
		}
		else
		{
			throw new RuntimeException("Unable to parse geometry wityh type: " + type);
		}
		reader.endObject();
		
		return geom;
	}
	
	private ArrayList<Point2D> readPoint2DArray() throws IOException
	{
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		
		reader.beginArray();
		while(reader.hasNext())
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
	
	
	// =============================================================
    // Read java items
	
	public Color readColor() throws IOException
	{
		reader.beginObject();
		reader.nextName();
		int red = reader.nextInt();
		reader.nextName();
		int green = reader.nextInt();
		reader.nextName();
		int blue = reader.nextInt();
		reader.endObject();
		return new Color(red, green, blue);
	}
	
	public void close() throws IOException
	{
		this.reader.close();
	}
}
