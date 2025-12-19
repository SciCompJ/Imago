/**
 * 
 */
package imago.imagepair.composite;

import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array;

/**
 * Display the combination of two input images by computing the difference of
 * intensities, and rescaling the result between 0 and 255.
 */
public class AbsoluteDifferenceOfIntensitiesComposite implements ImagePairComposite
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

    public UInt8Array processScalars(ScalarArray<?> refArray, ScalarArray<?> otherArray)
    {
        UInt8Array res = UInt8Array.create(refArray.size());
        for (int[] pos : res.positions())
        {
            double v1 = refArray.getValue(pos);
            double v2 = ImagePairComposite.containsPosition(otherArray, pos) ? otherArray.getValue(pos) : 0.0;
            res.setInt(pos, (int) (Math.abs(v1 - v2) * 2.0));
        }
        return res;
    }
}
