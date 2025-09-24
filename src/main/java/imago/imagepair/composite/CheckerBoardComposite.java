/**
 * 
 */
package imago.imagepair.composite;

import net.sci.array.Array;
import net.sci.array.numeric.UInt8Array2D;

/**
 * Display the combination of two input images using a checker board display.
 * Black tiles are associated to first image, white tiles are associated to
 * second image.
 */
public class CheckerBoardComposite implements ImagePairComposite
{
    int tileSize;
    
    public CheckerBoardComposite(int tileSize)
    {
        this.tileSize = tileSize;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> Array<?> process(Array<T> refArray, Array<?> otherArray)
    {
        if (!refArray.elementClass().isAssignableFrom(otherArray.elementClass()))
        {
            throw new IllegalArgumentException("Data type of second array is not compatible with that of reference array");
        }
        
        Array<T> res = refArray.duplicate();
        for (int[] pos : res.positions())
        {
            // determine which tile category the current pixel belongs to
            boolean image1 = true;
            for (int pos_i : pos)
            {
                image1 = image1 ^ (pos_i / tileSize) % 2 == 1;
            }
            
            // in case of tile associated to image2, update pixel content
            if (!image1 && ImagePairComposite.containsPosition(otherArray, pos))
            {
                res.set(pos, (T) otherArray.get(pos));
            }
        }
        return res;
    }
    
    public static final void main(String... args)
    {
        CheckerBoardComposite merge = new CheckerBoardComposite(4);
        
        UInt8Array2D array1 = UInt8Array2D.create(20, 20);
        array1.fillInts((x,y) -> x);
        UInt8Array2D array2 = UInt8Array2D.create(20, 20);
        array2.fillInts((x,y) -> y);
        
        Array<?> res = merge.process(array1, array2);
        System.out.println(res);
    }
}
