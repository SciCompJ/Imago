package imago.image.render;

import imago.image.ImageDataRenderer;
import net.sci.array.Array;
import net.sci.array.color.ColorMap;
import net.sci.array.color.ColorMaps;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.Vector;
import net.sci.array.numeric.VectorArray;

public class VectorImageChannelRenderer extends IndexedColorMapImageRenderer
{
    int channelIndex;
    
    /**
     * default empty constructor.
     */
    public VectorImageChannelRenderer()
    {
        this.channelIndex = 0;
        
        // create default color model based on grayscale colormap
        ColorMap lut = ColorMaps.GRAY.createColorMap(256); 
        this.colorModel = ImageDataRenderer.createIndexColorModel(lut);  
    }
    
    public VectorImageChannelRenderer setChannel(int channelIndex)
    {
        this.channelIndex = channelIndex;  
        return this;   
    }
    
    @Override
    public VectorImageChannelRenderer duplicate()
    {
        return (VectorImageChannelRenderer) new VectorImageChannelRenderer()
                .setChannel(this.channelIndex)
                .setColorMap(this.colorMap)
                .setDisplayRange(this.displayRange);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ScalarArray2D<?> computeRenderableArray(Array<?> array)
    {
        if (!Vector.class.isAssignableFrom(array.elementClass()))
        {
            throw new RuntimeException("Vector images must refer to a VectorArray");
        }
        @SuppressWarnings({ "rawtypes" })
        VectorArray vectorArray = VectorArray.wrap((Array<? extends Vector>) array);
        return ScalarArray2D.wrapScalar2d(vectorArray.channel(channelIndex));
    }
}
