/**
 * 
 */
package imago.gui.plugin.plugin.crop;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.sci.array.numeric.UInt8Array2D;
import net.sci.array.numeric.UInt8Array3D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.LinearRing2D;

/**
 * @author dlegland
 *
 */
public class CroppedUInt8Array3DTest
{

    /**
     * Test method for {@link imago.gui.plugin.plugin.crop.CroppedUInt8Array3D#slice(int)}.
     */
    @Test
    public void testSliceInt()
    {
        UInt8Array3D refArray = createArray();
//        UInt8Array2D slice = refArray.slice(15);
//        slice.print(System.out);
        
        Map<Integer, LinearRing2D> polygons = createPolygonMap();
        
        UInt8Array3D cropArray = new CroppedUInt8Array3D(refArray, polygons);
        UInt8Array2D cropSlice = cropArray.slice(15);
        
//        cropSlice.print(System.out);
        assertEquals( 0.0, cropSlice.getValue( 0,  0), 0.01);
        assertEquals(60.0, cropSlice.getValue(25, 20), 0.01);
    }

    /**
     * Test method for {@link imago.gui.plugin.plugin.crop.CroppedUInt8Array3D#getByte(int[])}.
     */
    @Test
    public void testGetByte()
    {
        UInt8Array3D refArray = createArray();

        assertEquals(refArray.getValue(0,0,0), 0.0, 0.01);

        Map<Integer, LinearRing2D> polygons = createPolygonMap();
        
        UInt8Array3D cropArray = new CroppedUInt8Array3D(refArray, polygons);
        
        assertEquals( 0.0, cropArray.getValue( 0,  0, 15), 0.01);
        assertEquals(60.0, cropArray.getValue(25, 20, 15), 0.01);
        
    }
    
    private UInt8Array3D createArray()
    {
        UInt8Array3D array = UInt8Array3D.create(50, 40, 30);
        array.fillValues((x, y, z) -> (double) x + y + z);
        return array;
    }
    
    private Map<Integer, LinearRing2D> createPolygonMap()
    {
        Map<Integer, LinearRing2D> map = new HashMap<>();
        
        int sliceIndex = 15;
        
        LinearRing2D poly = LinearRing2D.create(new Point2D(10, 10), new Point2D(40, 10), new Point2D(40, 30), new Point2D(10, 30));
        
        map.put(sliceIndex, poly);
        
        return map;
    }
}
