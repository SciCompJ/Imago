/**
 * 
 */
package imago.plugin.plugin.crop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.google.gson.stream.JsonReader;

import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.scene.io.JsonSceneReader;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.polygon.Polyline2D;

/**
 * @author dlegland
 *
 */
class Surface3DTest
{

    /**
     * Test method for {@link imago.plugin.plugin.crop.Surface3D#readPolylinesFromJson(java.io.File)}.
     * @throws IOException 
     */
    @Test
    final void testReadPolylinesFromJson() throws IOException
    {
        String fileName = getClass().getResource("/json/H_250_2_MD_crop3_surface3d.json").getFile();
        File file = new File(fileName);
        assertTrue(file.exists());
        assertTrue(file.canRead());
        
        FileReader fileReader = new FileReader(file);
        JsonReader jsonReader = new JsonReader(new BufferedReader(fileReader));
        String name;
        
//        JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
        
        jsonReader.beginObject();
        name = jsonReader.nextName();
        assertEquals(name, "type");
        name = jsonReader.nextString();
        assertEquals(name, "Surface3D");
        
        name = jsonReader.nextName();
        assertEquals(name, "saveDate");
        name = jsonReader.nextString();
//        assertEquals(name, "Surface3D");
        
        name = jsonReader.nextName();
        assertEquals(name, "image");
        jsonReader.skipValue();
//        name = jsonReader.nextString();
        
        name = jsonReader.nextName();
        assertEquals(name, "polylines");
        jsonReader.beginObject();
        
        name = jsonReader.nextName();
        assertEquals(name, "name");
        name = jsonReader.nextString();
        assertEquals(name, "polylines");
        
        name = jsonReader.nextName();
        assertEquals(name, "type");
        name = jsonReader.nextString();
        assertEquals(name, "ImageSerialSectionsNode");
        
        name = jsonReader.nextName();
        assertEquals(name, "visible");
        assertTrue(jsonReader.nextBoolean());
        
        name = jsonReader.nextName();
        assertEquals(name, "children");
        jsonReader.skipValue();

        // closes the "polylines" object
        jsonReader.endObject();
        
        // closes the Surface3D object
        jsonReader.endObject();

    }

    /**
     * Test method for {@link imago.plugin.plugin.crop.Surface3D#readPolylinesFromJson(java.io.File)}.
     * @throws IOException 
     */
    @Test
    final void testReadAnalysisFromJson() throws IOException
    {
        String fileName = getClass().getResource("/json/H_250_2_MD_crop3_surfEpi_polylines.json").getFile();
        File file = new File(fileName);
        assertTrue(file.exists());
        assertTrue(file.canRead());
        
        FileReader fileReader = new FileReader(file);
        JsonReader jsonReader = new JsonReader(new BufferedReader(fileReader));
        
        JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);

        // expect a group node...
        Node node = sceneReader.readNode();
        
        // expect a node with class ImageSerialSectionsNode
        assertTrue(node instanceof ImageSerialSectionsNode);
        ImageSerialSectionsNode sectionsNode = (ImageSerialSectionsNode) node;
        
        // expect each child to contain a shape node with a polyline geometry
        for (ImageSliceNode sliceNode : sectionsNode.children())
        {
            Node child = sliceNode.children().iterator().next();
            assertTrue(child instanceof ShapeNode);
            
            Geometry geom = ((ShapeNode) child).getGeometry();
            assertTrue(geom instanceof Polyline2D);
        }
    }
}
