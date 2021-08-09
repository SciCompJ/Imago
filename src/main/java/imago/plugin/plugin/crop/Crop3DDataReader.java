/**
 * 
 */
package imago.plugin.plugin.crop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.stream.JsonReader;

import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.Node;
import imago.app.scene.io.JsonSceneReader;

/**
 * Reads Crop3D data from a file in JSON format.
 * 
 * @author dlegland
 *
 */
public class Crop3DDataReader
{
    JsonReader reader;

    public Crop3DDataReader(File file) throws FileNotFoundException
    {
        FileReader fileReader = new FileReader(file);
        this.reader = new JsonReader(new BufferedReader(fileReader));
    }
    
    public Crop3DData readCrop3DData() throws IOException
    {
        // Default values to load
        ImageInfo imageInfo = null;
        ImageSerialSectionsNode polygonsNode = null;

        // start parsing Crop3D object
        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();

            if (name.equalsIgnoreCase("type"))
            {
                // check the file start with the Crop3D data header
                String string = reader.nextString();
                if (!string.equalsIgnoreCase("Crop3D"))
                {
                    throw new RuntimeException("Expect a file containing a Crop3D type data");
                }
            }
            else if(name.equalsIgnoreCase("saveDate"))
            {
                reader.skipValue();
            } 
            else if(name.equalsIgnoreCase("image"))
            {
                imageInfo = readImageInfo();
            } 
            else if(name.equalsIgnoreCase("models"))
            {
                // In the future we may envision several models, but in current version we manage only one.
                reader.beginArray();
                reader.beginObject();
                String name2 = reader.nextName();
                if (!name2.equalsIgnoreCase("crop"))
                {
                    throw new RuntimeException("Expect the file to contain a \"models\"/\"crop\" node");
                }
                reader.beginObject();
                
                String name3 = reader.nextName();
                if(name3.equalsIgnoreCase("polygons"))
                {
                    // create a scene reader to parse polyline
                    JsonSceneReader sceneReader = new JsonSceneReader(reader);
                    Node node = sceneReader.readNode();

                    // expect a group node...
                    if (!(node instanceof ImageSerialSectionsNode))
                    {
                        throw new RuntimeException("JSON file should contains a single ImageSerialSectionsNode instance.");
                    }

                    polygonsNode = (ImageSerialSectionsNode) node;
                } 
                else
                {
                    System.out.println("Unknown field name when reading Crop3D: " + name3);
                    reader.skipValue();
                }
                reader.endObject(); // crop object
                reader.endObject(); // array item
                
                // drop all remaining models
                if (reader.hasNext())
                {
                    System.err.println("Warning: additional models are specified in Crop3D, but will be discarded." + name);
                    while (reader.hasNext())
                    {
                        reader.skipValue();
                    }
                }
                reader.endArray();
            }
        }
        reader.endObject();

        // create and populate the data object
        Crop3DData data = new Crop3DData();
        data.imageInfo = imageInfo;
        data.polygons = polygonsNode;
        
        // return data
        return data;
    }
    
    private ImageInfo readImageInfo() throws IOException
    {
        // initialize image info object with default values
        ImageInfo imageInfo = new ImageInfo();
    
        // parse object fields
        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();
            if (name.equalsIgnoreCase("type"))
            {
                // check the file start with the Crop3D data header
                if (!reader.nextString().equalsIgnoreCase("Image3D"))
                {
                    throw new RuntimeException("Expect a file containing a Image3D type data");
                }
            }
            else if(name.equalsIgnoreCase("name"))
            {
                imageInfo.name = reader.nextString();
            }
            else if(name.equalsIgnoreCase("filePath"))
            {
                imageInfo.filePath = reader.nextString();
            }
            else if(name.equalsIgnoreCase("nDims"))
            {
                imageInfo.nDims = reader.nextInt();
            }
            else if(name.equalsIgnoreCase("size"))
            {
                imageInfo.size = readIntArray(imageInfo.nDims);
            }
            else
            {
                System.out.println("Unknown field name when reading Image3D: " + name);
                reader.skipValue();
            }
    
        }
        reader.endObject();
    
        return imageInfo;
    }

    private int[] readIntArray(int nItems) throws IOException
    {
        int[] res = new int[nItems];
        reader.beginArray();
        for (int d = 0; d < nItems; d++)
        {
            res[d] = reader.nextInt();
        }
        reader.endArray();
        return res;
    }
}
