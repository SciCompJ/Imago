package imago.image.render;

import imago.image.ImageDataRenderer;
import net.sci.array.Array;
import net.sci.array.color.ColorMap;
import net.sci.array.color.ColorMaps;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.Vector;
import net.sci.array.numeric.VectorArray;

public class VectorImageMaxNormRenderer extends IndexedColorMapImageRenderer
{
    /**
     * Default empty constructor.
     */
    public VectorImageMaxNormRenderer()
    {
        // create default color model based on grayscale colormap
        ColorMap lut = ColorMaps.GRAY.createColorMap(256); 
        this.colorModel = ImageDataRenderer.createIndexColorModel(lut);  
    }

    @Override
    public ImageDataRenderer duplicate()
    {
        return new VectorImageMaxNormRenderer()
                .setColorMap(this.colorMap)
                .setDisplayRange(this.displayRange);
    }

    @Override
    public ScalarArray2D<?> computeRenderableArray(Array<?> array)
    {
        if (!Vector.class.isAssignableFrom(array.elementClass()))
        {
            throw new RuntimeException("Vector images must refer to a VectorArray");
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ScalarArray2D<?> norm = ScalarArray2D.wrapScalar2d(VectorArray.maxNorm(VectorArray.wrap((Array<? extends Vector>) array)));
        return norm;
    }
}
