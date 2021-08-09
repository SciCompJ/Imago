/**
 * 
 */
package imago.plugin.plugin.crop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
        // initialize the data object
        Crop3DData data = new Crop3DData();
        
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
                data.imageInfo = readImageInfo();
            } 
            else if(name.equalsIgnoreCase("regions"))
            {
                data.regions = readCropRegions();
            }
            else
            {
                throw new RuntimeException("Can not interpret field name: " + name);
            }
        }
        reader.endObject();
        
        // temporary workaround
        data.polygons = data.regions.get(0).sections;
        
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
    
    private ArrayList<Crop3DRegion> readCropRegions() throws IOException
    {
        ArrayList<Crop3DRegion> regions = new ArrayList<Crop3DRegion>();
        
        reader.beginArray();
        while(reader.hasNext())
        {
            regions.add(readCropRegion());
        }
        reader.endArray();

        return regions;
    }
    
    private Crop3DRegion readCropRegion() throws IOException
    {
        Crop3DRegion model = new Crop3DRegion(); 
        
        reader.beginObject();
        while(reader.hasNext())
        {
            String name = reader.nextName();
            if (name.equalsIgnoreCase("name"))
            {
                model.name = reader.nextString();
            }
            else if (name.equalsIgnoreCase("polygons"))
            {
                model.sections = readPolygons();
            }
            else
            {
                System.out.println("Unknown field name when reading Crop3D Region: " + name);
                reader.skipValue();
            }
        }
        
        reader.endObject(); // array item
        return model;
    }
    
    private ImageSerialSectionsNode readPolygons() throws IOException
    {
        // create a scene reader to geometry data
        JsonSceneReader sceneReader = new JsonSceneReader(reader);
        Node node = sceneReader.readNode();

        // expect a group node...
        if (!(node instanceof ImageSerialSectionsNode))
        {
            throw new RuntimeException("JSON file should contains a single ImageSerialSectionsNode instance.");
        }
        return (ImageSerialSectionsNode) node;
    }
}
