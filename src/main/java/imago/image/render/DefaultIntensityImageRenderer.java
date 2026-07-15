package imago.image.render;

import net.sci.array.Array;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;

public class DefaultIntensityImageRenderer extends IndexedColorMapImageRenderer
{
    /**
     * Default empty constructor.
     */
    public DefaultIntensityImageRenderer()
    {
    }
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ScalarArray2D<?> computeRenderableArray(Array<?> array)
    {
        // Check if the array contains UInt8 or UInt16 data
        Class<?> dataClass = array.elementClass();
        if (!Scalar.class.isAssignableFrom(dataClass))
        {
            throw new RuntimeException("intensity images must refer to an array of scalar elements, not " + dataClass.getName());
        }
        
        // convert to ScalarArray2D either by class cast or by wrapping
        return ScalarArray2D.wrapScalar2d(ScalarArray.wrap((Array<Scalar>) array));
    }


    @Override
    public IndexedColorMapImageRenderer duplicate()
    {
        DefaultIntensityImageRenderer dup = new DefaultIntensityImageRenderer();
        dup.setDisplayRange(this.displayRange);
        dup.setColorMap(this.colorMap);
        return dup;
    }
}
