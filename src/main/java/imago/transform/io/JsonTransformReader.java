/**
 * 
 */
package imago.transform.io;

import java.io.IOException;
import java.io.Reader;

import com.google.gson.stream.JsonReader;

import net.sci.geom.Transform;
import net.sci.geom.geom2d.Point2D;
import net.sci.register.transform.CenteredMotion2D;
import net.sci.register.transform.CenteredSimilarity2D;
import net.sci.register.transform.TranslationModel2D;

/**
 * Reads a geometric transform from a JSON file.
 * 
 * {@snippet lang="java" :
 *  // File file = ...
 *  JSonTransformReader reader = new JSonTransformReader(new FileReader(file));
 *  Transform transfo = reader.readTransform();
 * }
 */
public class JsonTransformReader extends JsonReader
{

    // =============================================================
    // Constructor
    
    /**
     * Creates a new JSonTransformReader from an instance of JsonReader.
     * 
     * @param reader
     *            an instance of JsonReader.
     */
    public JsonTransformReader(Reader reader)
    {
        super(reader);
    }
    
    public Transform readTransform() throws IOException
    {
        beginObject();
        while (this.hasNext())
        {
            // read type
            String name = nextName();

            if (name.compareToIgnoreCase("Type") == 0)
            {
                String type = nextString();
                Transform transfo = readTransform(type);
                endObject();
                return transfo;
            }
            else if (name.compareToIgnoreCase("className") == 0)
            {
                // no specific processing for class name
                nextString();
            }
            else
            {
                System.out.println("Unknown name: " + name);
                // assume item is a string
                nextString();
            }
        }
        throw new RuntimeException("Unable to parse transform from json");
    }
    
    private Transform readTransform(String transformType) throws IOException
    {
        return switch (transformType.toLowerCase())
        {
            case "translation2d" -> 
            {
                // read shift X and shift Y
                nextName(); double x = this.nextDouble();
                nextName(); double y = this.nextDouble();
                yield new TranslationModel2D(x, y);
            }
            case "centeredmotion2d" -> 
            {
                // read shift X and shift Y
                nextName(); double centerX = this.nextDouble();
                nextName(); double centerY = this.nextDouble();
                nextName(); double angle = this.nextDouble();
                nextName(); double shiftX = this.nextDouble();
                nextName(); double shiftY = this.nextDouble();
                Point2D center = new Point2D(centerX, centerY);
                yield new CenteredMotion2D(center, angle, shiftX, shiftY);
            }
            case "centeredsimilarity2d" -> 
            {
                // read shift X and shift Y
                nextName(); double centerX = this.nextDouble();
                nextName(); double centerY = this.nextDouble();
                nextName(); double angle = this.nextDouble();
                nextName(); double logScale = this.nextDouble();
                nextName(); double shiftX = this.nextDouble();
                nextName(); double shiftY = this.nextDouble();
                Point2D center = new Point2D(centerX, centerY);
                yield new CenteredSimilarity2D(center, angle, logScale, shiftX, shiftY);
            }
            default -> throw new RuntimeException("Unknown Transform type: " + transformType);
        };
    }
}
