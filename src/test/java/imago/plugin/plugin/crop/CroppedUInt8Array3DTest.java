/**
 * 
 */
package imago.plugin.plugin.crop;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import net.sci.array.scalar.UInt8Array2D;
import net.sci.array.scalar.UInt8Array3D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.Polygon2D;

/**
 * @author dlegland
 *
 */
public class CroppedUInt8Array3DTest
{

    /**
     * Test method for {@link imago.plugin.plugin.crop.CroppedUInt8Array3D#slice(int)}.
     */
    @Test
    public void testSliceInt()
    {
        UInt8Array3D refArray = createArray();
//        UInt8Array2D slice = refArray.slice(15);
//        slice.print(System.out);
        
        ImageSerialSectionsNode cropNode = createCropNode();
        
        UInt8Array3D cropArray = new CroppedUInt8Array3D(refArray, cropNode);
        UInt8Array2D cropSlice = cropArray.slice(15);
        
//        cropSlice.print(System.out);
        assertEquals( 0.0, cropSlice.getValue( 0,  0), 0.01);
        assertEquals(60.0, cropSlice.getValue(25, 20), 0.01);
    }

    /**
     * Test method for {@link imago.plugin.plugin.crop.CroppedUInt8Array3D#getByte(int[])}.
     */
    @Test
    public void testGetByte()
    {
        UInt8Array3D refArray = createArray();

        assertEquals(refArray.getValue(0,0,0), 0.0, 0.01);

        ImageSerialSectionsNode cropNode = createCropNode();

        UInt8Array3D cropArray = new CroppedUInt8Array3D(refArray, cropNode);
        
        assertEquals( 0.0, cropArray.getValue( 0,  0, 15), 0.01);
        assertEquals(60.0, cropArray.getValue(25, 20, 15), 0.01);
        
    }
    
    private UInt8Array3D createArray()
    {
        UInt8Array3D array = UInt8Array3D.create(50, 40, 30);
        array.fillValues((x, y, z) -> (double) x + y + z);
        return array;
    }

    private ImageSerialSectionsNode createCropNode()
    {
        ImageSerialSectionsNode node = new ImageSerialSectionsNode("crop");
        
        int sliceIndex = 15;
        String sliceName = String.format(Locale.US, "slice%02d", sliceIndex);
        ImageSliceNode sliceNode = new ImageSliceNode(sliceName, sliceIndex);
        node.addSliceNode(sliceNode);
        
        Polygon2D poly = Polygon2D.create(new Point2D(10, 10), new Point2D(40, 10), new Point2D(40, 30), new Point2D(10, 30));
        ShapeNode shapeNode = new ShapeNode(sliceName, poly.rings().iterator().next());
        
        sliceNode.addNode(shapeNode);
        
        return node;
    }
}
