/**
 * 
 */
package imago.gui.plugins.plugins.crop;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.google.gson.stream.JsonReader;

import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.scene.io.JsonSceneReader;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.numeric.UInt8Array2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.LinearRing2D;
import net.sci.image.Image;

/**
 * @author dlegland
 *
 */
public class Crop3DTest
{

    /**
     * Test method for {@link imago.gui.plugins.plugins.crop.Crop3D#addPolygon(int, net.sci.geom.polygon2d.Polygon2D)}.
     */
    @Test
    public final void testAddPolygon()
    {
        UInt8Array2D array = UInt8Array2D.create(400, 400);
        Image image = new Image(array);
        
        ImageFrame frame = ImageFrame.create(image, null);
        ImageHandle handle = frame.getImageHandle();
        
        // create node for polygons
        GroupNode rootNode = ((GroupNode) handle.getRootNode());
        GroupNode polyNode = new GroupNode("polygons");
        rootNode.addNode(polyNode);
        
        LinearRing2D ring1 = LinearRing2D.create(4);
        ring1.addVertex(new Point2D( 50,  50));
        ring1.addVertex(new Point2D(350,  50));
        ring1.addVertex(new Point2D(350, 350));
        ring1.addVertex(new Point2D( 50, 350));
        ShapeNode shape1 = new ShapeNode("Blue", ring1);
        shape1.getStyle().setLineColor(Color.BLUE);
        
        LinearRing2D ring2 = LinearRing2D.create(4);
        ring2.addVertex(new Point2D( 80,  20));
        ring2.addVertex(new Point2D(380,  80));
        ring2.addVertex(new Point2D(320, 380));
        ring2.addVertex(new Point2D( 20, 320));
        ShapeNode shape2 = new ShapeNode("Red", ring2);
        shape2.getStyle().setLineColor(Color.RED);
        
        polyNode.addNode(shape1);
        polyNode.addNode(shape2);
        
        frame.getImageViewer().refreshDisplay(); 
        frame.getImageViewer().repaint();
        
        // compute projection points of current poly over next poly
        LinearRing2D ring1rs = ring1.resampleBySpacing(2.0).smooth(3);
        int nv = ring1rs.vertexCount();
        LinearRing2D nextPoly = LinearRing2D.create(nv);
        for (Point2D point : ring1rs.vertexPositions())
        {
            nextPoly.addVertex(ring2.projection(point));
        }
        
        double t0 = 0.3;
        double t1 = 0.7;
        
        LinearRing2D interpPoly = LinearRing2D.create(nv);
        for (int iv = 0; iv < nv; iv++)
        {
            Point2D p1 = ring1rs.vertexPosition(iv);
            Point2D p2 = nextPoly.vertexPosition(iv);
            
            double x = p1.x() * t1 + p2.x() * t0;
            double y = p1.y() * t1 + p2.y() * t0;
            interpPoly.addVertex(new Point2D(x, y));
        }

        
        ShapeNode shapeProj = new ShapeNode("Green", interpPoly);
        shapeProj.getStyle().setLineColor(Color.GREEN);
        polyNode.addNode(shapeProj);

        frame.getImageViewer().refreshDisplay(); 
        frame.getImageViewer().repaint();
    }

    /**
     * Test method for {@link imago.gui.plugins.plugins.crop.Crop3D#readPolygonsFromJson(java.io.File)}.
     */
    @Test
    public final void testReadPolygonsFromJson() throws IOException
    {
        String fileName = getClass().getResource("/json/tomStackLinearRings.json").getFile();
        File file = new File(fileName);
        assertTrue(file.exists());
        assertTrue(file.canRead());
        
        FileReader fileReader = new FileReader(file);
        JsonReader jsonReader = new JsonReader(new BufferedReader(fileReader));
        
        JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);

        // expect a group node...
        Node node = sceneReader.readNode();

        assertTrue(node instanceof ImageSerialSectionsNode);
    }

}
