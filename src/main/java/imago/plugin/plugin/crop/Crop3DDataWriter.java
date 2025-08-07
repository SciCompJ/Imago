package imago.plugin.plugin.crop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.google.gson.stream.JsonWriter;

import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.Node;
import imago.app.scene.io.JsonSceneWriter;
import net.sci.geom.polygon2d.LinearRing2D;
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
        
        // list of authors for current file
        jsonWriter.name("authors").beginArray();
        jsonWriter.beginObject();
        jsonWriter.name("name").value(System.getProperty("user.name"));
        jsonWriter.endObject();
        jsonWriter.endArray();
        
        // one node for the 3D image
        jsonWriter.name("image");
        writeImageInfo(crop3d.imageHandle.getImage());
        
        // one node for the collection of crop regions
        jsonWriter.name("regions").beginArray();
        for (Crop3DRegion region : crop3d.data.regions)
        {
            writeCrop3DRegion(region);
        }
        jsonWriter.endArray();
        
        // close Crop3D
        jsonWriter.endObject();
    }
    
    private void writeCrop3DRegion(Crop3DRegion region) throws IOException
    {
        // one node for the default crop polygons
        jsonWriter.beginObject();
        jsonWriter.name("name").value(region.name);
        jsonWriter.name("polygons");
        writePolygons(region.polygons);
      
        jsonWriter.endObject(); // array item
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
            jsonWriter.name("polylines");
            writeSceneNode(polyNode);
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
    
    public void writePolygons(Map<Integer, LinearRing2D> polygons) throws IOException
    {
        // one node for the collection of XY-slices
        jsonWriter.beginArray();
        for (int sliceIndex : polygons.keySet())
        {
            LinearRing2D ring = polygons.get(sliceIndex);
            
            jsonWriter.beginObject();
            jsonWriter.name("sliceIndex").value(sliceIndex);
            jsonWriter.name("geometry");
            
            JsonSceneWriter writer = new JsonSceneWriter(jsonWriter);
            writer.writeGeometry(ring, "%.2f");
            jsonWriter.endObject();        
        }
        jsonWriter.endArray();
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
