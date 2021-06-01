/**
 * 
 */
package imago.app.scene.io;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.stream.JsonReader;

import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.scene.Style;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.LineString2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;

/**
 * Reads the content of a scene from a text file in JSON format.
 * 
 * @see JsonSceneWriter
 * 
 * @author dlegland
 *
 */
public class JsonSceneReader
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
     * Creates a new SceneReader from an instance of JsonReader.
     * 
     * @param reader
     *            an instance of JsonReader.
     */
	public JsonSceneReader(JsonReader reader)
	{
		this.reader = reader;
	}

	
    // =============================================================
    // Read Imago items
	
	/**
     * Read the content of a Scene node.
     * 
     * @see Node
     * 
     * @return a new Node instance.
     * @throws IOException
     *             if a reading problem occurred.
     */
	public Node readNode() throws IOException
	{
		Node node = null;
		reader.beginObject();
		
		// read name
		reader.nextName();
		String name = reader.nextString();
		// read type
		reader.nextName();
		String type = reader.nextString();
		
		switch(type)
		{
		case "ShapeNode":
			node = readShapeNode(name);
			break;
			
		case "ImageSerialSectionsNode":
			node = readImageSerialSectionsNode(name);
			break;
			
		case "GroupNode":
			node = readGroupNode(name);
			break;
		}
		
		// cleanup
		reader.endObject();
		return node;
	}

    /**
     * Read the content of a ShapeNode.
     * 
     * @see ShapeNode
     * 
     * @param name
     *            the name of the node to be created.
     * @return a new ShapeNode instance.
     * @throws IOException
     *             if a reading problem occurred.
     */
	private ShapeNode readShapeNode(String name) throws IOException
	{
		// initial values
		boolean visible = true;
		Style style = null;
		Geometry geometry = null;
		
		// iterate over keys
		while(reader.hasNext())
		{
			String key = reader.nextName();
			switch(key)
			{
			case "style":
				style = readStyle();
				break;
			case "geometry":
				geometry = readGeometry();
				break;
			case "visible":
				visible = reader.nextBoolean();
				break;
			default:
				throw new IOException("Unknown key \"" + key + "\" when parsing a ShapeNode");
			}
		}
		
		// create the node
		ShapeNode node = new ShapeNode(name, geometry, style);
		node.setVisible(visible);
		return node;
	}
	
    /**
     * Read the content of an ImageSliceNode.
     * 
     * @see ImageSliceNode
     * 
     * @param name
     *            the name of the node to be created.
     * @return a new ImageSliceNode instance.
     * @throws IOException
     *             if a reading problem occurred.
     */
	private ImageSliceNode readImageSliceNode(String name) throws IOException
	{
		ImageSliceNode sliceNode = new ImageSliceNode(name);
		while(reader.hasNext())
		{
			String key = reader.nextName(); 
			switch(key)
			{
			case "visible":
				sliceNode.setVisible(reader.nextBoolean());
				break;
			case "sliceIndex":
				sliceNode.setSliceIndex(reader.nextInt());
				break;
			case "children":
				reader.beginArray();
				while(reader.hasNext())
				{
					sliceNode.addNode(readNode());
				}
				reader.endArray();
				break;
			default:
				throw new IOException("Unknown key \"" + key + "\" when parsing ImageSliceNode");
			}
		}
		return sliceNode;
	}

    /**
     * Read the content of an ImageSerialSectionsNode.
     * 
     * @see ImageSerialSectionsNode
     * 
     * @param name
     *            the name of the node to be created.
     * @return a new ImageSerialSectionsNode instance.
     * @throws IOException
     *             if a reading problem occurred.
     */
	private ImageSerialSectionsNode readImageSerialSectionsNode(String name) throws IOException
	{
		ImageSerialSectionsNode group = new ImageSerialSectionsNode(name);
		while(reader.hasNext())
		{
			String key = reader.nextName(); 
			switch(key)
			{
			case "visible":
				group.setVisible(reader.nextBoolean());
				break;
			case "children":
				reader.beginArray();
				while(reader.hasNext())
				{
					reader.beginObject();
					
					// read name
					reader.nextName();
					String childName = reader.nextString();
					// read type
					reader.nextName();
					String type = reader.nextString();
					if (!type.equalsIgnoreCase("ImageSliceNode"))
					{
						throw new IOException("Children of an ImageSerialSectionsNode must be instances of ImageSliceNode");
					}

					group.addSliceNode(readImageSliceNode(childName));
					reader.endObject();
				}
				reader.endArray();
				break;
			default:
				throw new IOException("Unknown key \"" + key + "\" when parsing GroupNode");
			}
		}
		return group;
	}

    /**
     * Read the content of an GroupNode.
     * 
     * @see GroupNode
     * 
     * @param name
     *            the name of the node to be created.
     * @return a new GroupNode instance.
     * @throws IOException
     *             if a reading problem occurred.
     */
	private GroupNode readGroupNode(String name) throws IOException
	{
		GroupNode group = new GroupNode(name);
		while(reader.hasNext())
		{
			String key = reader.nextName(); 
			switch(key)
			{
			case "visible":
				group.setVisible(reader.nextBoolean());
				break;
			case "children":
				reader.beginArray();
				while(reader.hasNext())
				{
					group.addNode(readNode());
				}
				reader.endArray();
				break;
			default:
				throw new IOException("Unknown key \"" + key + "\" when parsing GroupNode");
			}
		}
		return group;
	}

	/**
     * Read the content of a Style saved in JSON format.
     * 
     * @return a new Style.
     * @throws IOException
     *             if a reading problem occurred.
     */
	public Style readStyle() throws IOException
	{
		Style style = new Style();
		
		reader.beginObject();
		while(reader.hasNext())
		{
			String key = reader.nextName();
			switch(key)
			{
			case "color":
				style.setColor(readColor());
				break;
			case "lineWidth":
				style.setLineWidth(reader.nextDouble());
				break;
			default:
				throw new IOException("Unknown key \"" + key + "\" when parsing Style");
			}
		}
		reader.endObject();
		
		return style;
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
		else
		{
			throw new RuntimeException("Unable to parse geometry wityh type: " + type);
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
	

    /**
     * Read a java color saved in JSON format.
     * 
     * @return a new Color.
     * @throws IOException
     *             if a reading problem occurred.
     */
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
