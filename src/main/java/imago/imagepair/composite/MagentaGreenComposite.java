/**
 * 
 */
package imago.imagepair.composite;

import net.sci.array.Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array2D;

/**
 * Display a pair of registered images as a color image, the first one in red
 * and blue channels (resulting in magenta), the second one in green channel.
 * 
 * This choice of color pair is more adequate for color-blind people than the
 * red-green pair.
 */
public class MagentaGreenComposite implements ImagePairComposite
{
    @Override
    public <T> Array<?> process(Array<T> refArray, Array<?> otherArray)
    {
        // check types
        if (!(refArray instanceof ScalarArray) || !(otherArray instanceof ScalarArray))
        {
            throw new IllegalArgumentException("Both arrays must have scalar arrays");
        }
        return processScalars((ScalarArray<?>) refArray, (ScalarArray<?>) otherArray);
    }
    
    public RGB8Array processScalars(ScalarArray<?> refArray, ScalarArray<?> otherArray)
    {
        RGB8Array res = RGB8Array.create(refArray.size());
        for (int[] pos : res.positions())
        {
            int v1 = (int) refArray.getValue(pos);
            int v2 =  ImagePairComposite.containsPosition(otherArray, pos) ? (int) otherArray.getValue(pos) : 0;
            res.setIntCode(pos, intCode(v1, v2));
        }
        return res;
    }
    
    private static final int intCode(int v1, int v2)
    {
        return (v1 & 0x00FF) << 16 | (v2 & 0x00FF) << 8 | (v1 & 0x00FF); 
    }

    public static final void main(String... args)
    {
        MagentaGreenComposite merge = new MagentaGreenComposite();
        
        UInt8Array2D array1 = UInt8Array2D.create(20, 20);
        array1.fillInts((x,y) -> x*10);
        UInt8Array2D array2 = UInt8Array2D.create(20, 20);
        array2.fillInts((x,y) -> y*10);
        
        Array<?> res = merge.process(array1, array2);
        System.out.println(res);
    }
}
