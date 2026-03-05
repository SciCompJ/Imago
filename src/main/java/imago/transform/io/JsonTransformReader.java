/**
 * 
 */
package imago.transform.io;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.stream.JsonReader;

import net.sci.geom.Transform;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom3d.Point3D;
import net.sci.register.transform.BSplineTransformModel2D;
import net.sci.register.transform.BSplineTransformModel3D;
import net.sci.register.transform.CenteredMotion2D;
import net.sci.register.transform.CenteredSimilarity2D;
import net.sci.register.transform.TranslationModel2D;
import net.sci.util.MathUtils;

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
    static HashMap<String, Function<JsonReader, Transform>> readers;
    static 
    {
        readers = new HashMap<>();
        readers.put("Translation2D", JsonTransformReader::readTranslation2D);
        readers.put("CenteredMotion2D", JsonTransformReader::readCenteredMotion2D);
        readers.put("CenteredSimilarity2D", JsonTransformReader::readCenteredSimilarity2D);
        readers.put("BSplineTransformModel2D", JsonTransformReader::readBSplineTransformModel2D);
        readers.put("BSplineTransformModel3D", JsonTransformReader::readBSplineTransformModel3D);
    }
    
    /**
     * Adds a new transform reader to the list of readers managed by this class.
     * A transform reader accepts a {@code JsonReader} as input argument, and
     * returns an instance of {@code Transform}. The reader should have read the
     * "type" name and value within the JSON file. The name given as first
     * argument corresponds the value of the "type" field.
     * 
     * @param name
     *            the name of the transform in the "type" field
     * @param reader
     *            a function that will parse additional arguments from a
     *            {@code JsonReader} to create a new {@code Transform}.
     */
    public static final void addTransformReader(String name, Function<JsonReader, Transform> reader)
    {
        readers.put(name, reader);
    }
    
    
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
        for (Map.Entry<String, Function<JsonReader, Transform>> entry : readers.entrySet())
        {
            if (entry.getKey().equalsIgnoreCase(transformType)) return entry.getValue().apply(this);
        }
            
        throw new RuntimeException("Unknown Transform type: " + transformType);
    }
    
    private static final Transform readTranslation2D(JsonReader reader)
    {
        try
        {
            // read shift X and shift Y
            reader.nextName(); double shiftX = reader.nextDouble();
            reader.nextName(); double shiftY = reader.nextDouble();
            return new TranslationModel2D(shiftX, shiftY);
        }
        catch (IOException ex)
        {
            throw (new RuntimeException(ex));
        }
    }
    
    private static final Transform readCenteredMotion2D(JsonReader reader)
    {
        try
        {
            reader.nextName(); double centerX = reader.nextDouble();
            reader.nextName(); double centerY = reader.nextDouble();
            reader.nextName(); double angle = reader.nextDouble();
            reader.nextName(); double shiftX = reader.nextDouble();
            reader.nextName(); double shiftY = reader.nextDouble();
            Point2D center = new Point2D(centerX, centerY);
            return new CenteredMotion2D(center, angle, shiftX, shiftY);
        }
        catch (IOException ex)
        {
            throw (new RuntimeException(ex));
        }
    }
    
    private static final Transform readCenteredSimilarity2D(JsonReader reader)
    {
        try
        {
            reader.nextName(); double centerX = reader.nextDouble();
            reader.nextName(); double centerY = reader.nextDouble();
            reader.nextName(); double angle = reader.nextDouble();
            reader.nextName(); double logScale = reader.nextDouble();
            reader.nextName(); double shiftX = reader.nextDouble();
            reader.nextName(); double shiftY = reader.nextDouble();
            Point2D center = new Point2D(centerX, centerY);
            return new CenteredSimilarity2D(center, angle, logScale, shiftX, shiftY);
        }
        catch (IOException ex)
        {
            throw (new RuntimeException(ex));
        }
    }
    
    private static final Transform readBSplineTransformModel2D(JsonReader reader)
    {
        try
        {
            reader.nextName(); int[] gridSize = readIntArray(reader, 2);
            reader.nextName(); double[] spacings = readDoubleArray(reader, 2);
            reader.nextName(); double[] origins = readDoubleArray(reader, 2);
            Point2D origin = new Point2D(origins[0], origins[1]);
            int nParams = (int) MathUtils.prod(gridSize) * 2;
            reader.nextName();
            double[] params = readDoubleArray(reader, nParams);
            
            BSplineTransformModel2D transfo = new BSplineTransformModel2D(gridSize, spacings, origin);
            transfo.setParameters(params);
            return transfo;
        }
        catch (IOException ex)
        {
            throw (new RuntimeException(ex));
        }
    }
    
    private static final Transform readBSplineTransformModel3D(JsonReader reader)
    {
        try
        {
            reader.nextName(); int[] gridSize = readIntArray(reader, 3);
            reader.nextName(); double[] spacings = readDoubleArray(reader, 3);
            reader.nextName(); double[] origins = readDoubleArray(reader, 3);
            Point3D origin = new Point3D(origins[0], origins[1], origins[2]);
            int nParams = (int) MathUtils.prod(gridSize) * 3;
            reader.nextName();
            double[] params = readDoubleArray(reader, nParams);
            
            BSplineTransformModel3D transfo = new BSplineTransformModel3D(gridSize, spacings, origin);
            transfo.setParameters(params);
            return transfo;
        }
        catch (IOException ex)
        {
            throw (new RuntimeException(ex));
        }
    }
    
    private static final int[] readIntArray(JsonReader reader, int n) throws IOException
    {
        int[] array = new int[n];
        reader.beginArray();
        for (int i = 0; i < n; i++)
        {
            array[i] = reader.nextInt();
        }
        reader.endArray();
        return array;
    }
    
    private static final double[] readDoubleArray(JsonReader reader, int n) throws IOException
    {
        double[] array = new double[n];
        reader.beginArray();
        for (int i = 0; i < n; i++)
        {
            array[i] = reader.nextDouble();
        }
        reader.endArray();
        return array;
    }
}
