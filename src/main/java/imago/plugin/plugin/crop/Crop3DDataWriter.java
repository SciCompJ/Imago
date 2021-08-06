package imago.plugin.plugin.crop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.stream.JsonWriter;

import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.Node;
import imago.app.scene.io.JsonSceneWriter;
import net.sci.image.Image;

/**
 * Writes Crop3D or Surface3D data into a json file.
 * 
 * @author dlegland
 *
 */
public class Crop3DDataWriter
{
    JsonWriter jsonWriter;
    
    public Crop3DDataWriter(File file) throws IOException
    {
        // open the writer
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        this.jsonWriter = new JsonWriter(new PrintWriter(fileWriter));
        
        // configure
        jsonWriter.setIndent("  ");
    }
    
    public void writeCrop3D(Crop3D crop3d) throws IOException
    {
        // open Crop3D node
        jsonWriter.beginObject();
        jsonWriter.name("type").value("Crop3D");
        String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
        jsonWriter.name("saveDate").value(dateString);
        
        // one node for the 3D image
        jsonWriter.name("image");
        writeImageInfo(crop3d.imageHandle.getImage());
        
        // one node for the collection of models
        jsonWriter.name("models").beginArray();
        
        // one node for the default crop polygons
        jsonWriter.beginObject();
        jsonWriter.name("crop").beginObject();
        // write crop polygon data
        ImageSerialSectionsNode polyNode = crop3d.getPolygonsNode();
        if (polyNode != null)
        {
            JsonSceneWriter sceneWriter = new JsonSceneWriter(jsonWriter);
            jsonWriter.name("polygons");
            sceneWriter.writeNode(polyNode);
        }
      
        jsonWriter.endObject(); // crop object
        jsonWriter.endObject(); // array item
        jsonWriter.endArray(); // array of models

        // close Crop3D
        jsonWriter.endObject();
    }

    public void writeSurface3D(Surface3D surf3d) throws IOException
    {
        // open Surface3D node
        jsonWriter.beginObject();
        jsonWriter.name("type").value("Surface3D");
        String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
        jsonWriter.name("saveDate").value(dateString);
        
        // one node for the 3D image
        jsonWriter.name("image");
        writeImageInfo(surf3d.imageHandle.getImage());
        
        // one node for the polyline
        ImageSerialSectionsNode polyNode = surf3d.getPolylinesNode();
        if (polyNode != null)
        {
            JsonSceneWriter sceneWriter = new JsonSceneWriter(jsonWriter);
            jsonWriter.name("polylines");
            sceneWriter.writeNode(polyNode);
        }
      
        // close Surface3D
        jsonWriter.endObject();
    }
    
    private void writeImageInfo(Image image) throws IOException
    {
        jsonWriter.beginObject();
        jsonWriter.name("type").value("Image3D");
        jsonWriter.name("name").value(image.getName());
        jsonWriter.name("filePath").value(image.getFilePath());
        int[] dims = image.getData().size();
        jsonWriter.name("nDims").value(dims.length);
        jsonWriter.name("size");
        writeIntArray(dims);
        jsonWriter.endObject();        
    }
    
    private void writeIntArray(int[] array) throws IOException
    {
        jsonWriter.beginArray();
        for (int d : array)
        {
            jsonWriter.value(d);
        }
        jsonWriter.endArray();
    }
    
    public void writePolygons(ImageSerialSectionsNode polyNode) throws IOException
    {
        JsonSceneWriter writer = new JsonSceneWriter(jsonWriter);
        writer.writeNode(polyNode);
    }
    
    public void writeSceneNode(Node node) throws IOException
    {
        JsonSceneWriter writer = new JsonSceneWriter(jsonWriter);
        writer.writeNode(node);
    }
    
    public void close() throws IOException
    {
        this.jsonWriter.close();
    }
}
