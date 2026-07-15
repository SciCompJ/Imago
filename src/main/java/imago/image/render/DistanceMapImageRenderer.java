package imago.image.render;

import imago.image.ImageDataRenderer;
import net.sci.array.Array;
import net.sci.array.color.Color;
import net.sci.array.color.ColorMap;
import net.sci.array.color.ColorMaps;
import net.sci.array.color.RGB8;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;

/**
 * An implementation of ImageDataRenderer for distance maps. Uses a mapping of
 * values based on display range, but uses the background color to render values
 * equal to zero.
 */
public class DistanceMapImageRenderer extends IndexedColorMapImageRenderer
{
    /**
     * Default empty constructor.
     */
    public DistanceMapImageRenderer()
    {
        // create default color model using a colored map and a white color for background
        this.colorMap = ColorMaps.JET.createColorMap(255);
        this.backgroundColor = RGB8.WHITE;
        updateColorModel();
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
    
    public DistanceMapImageRenderer setColorMap(ColorMap colorMap)
    {
        this.colorMap = colorMap;
        updateColorModel();
        return this;   
    }
    
    public DistanceMapImageRenderer setBackgroundColor(Color bgColor)
    {
        this.backgroundColor = bgColor;
        updateColorModel();
        return this;   
    }
    
    /**
     * Creates the 256-colors color model based on current colormap and
     * background color.
     * 
     * @return the corresponding IndexColorModel, with 256 colors max.
     */
    public void updateColorModel()
    {
        this.colorModel = ImageDataRenderer.createIndexColorModel(colorMap, backgroundColor);
    }

    @Override
    public DistanceMapImageRenderer duplicate()
    {
        DistanceMapImageRenderer dup = new DistanceMapImageRenderer();
        dup.setDisplayRange(this.displayRange);
        dup.setColorMap(this.colorMap);
        dup.setBackgroundColor(this.backgroundColor);
        return dup;
    }
}
