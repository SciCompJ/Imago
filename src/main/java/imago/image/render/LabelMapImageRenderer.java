package imago.image.render;


import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import imago.image.ImageDataRenderer;
import net.sci.array.Array;
import net.sci.array.color.ColorMaps;
import net.sci.array.color.RGB8;
import net.sci.array.numeric.IntArray;
import net.sci.array.numeric.IntArray2D;
import net.sci.array.numeric.ScalarArray2D;

public class LabelMapImageRenderer extends IndexedColorMapImageRenderer
{
    /**
     * Default empty constructor.
     */
    public LabelMapImageRenderer()
    {
        this.colorMap = ColorMaps.GLASBEY_DARK.createColorMap(255); 
        this.backgroundColor = RGB8.WHITE;
        updateColorModel();
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
    public BufferedImage render(Array<?> array)
    {
        if (!(array instanceof IntArray))
        {
            throw new RuntimeException("Label images assume inner array implements IntArray");
        }
        IntArray2D<?> intArray = IntArray2D.wrap((IntArray<?>) array);

        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        // Create the AWT image
        int type = java.awt.image.BufferedImage.TYPE_BYTE_INDEXED ;
        BufferedImage bufImg = new BufferedImage(sizeX, sizeY, type, this.colorModel);
        
        // Populate the raster
        WritableRaster raster = bufImg.getRaster();
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                int index = intArray.getInt(x, y);
                if (index > 0)
                {
                    index = ((index - 1) % 255) + 1;
                }
                raster.setSample(x, y, 0, index); 
            }
        }

        return bufImg;
    }

    @Override
    public LabelMapImageRenderer duplicate()
    {
        LabelMapImageRenderer dup = new LabelMapImageRenderer();
        dup.setColorMap(this.colorMap);
        dup.setBackgroundColor(this.backgroundColor);
        return dup;
    }

    @Override
    public ScalarArray2D<?> computeRenderableArray(Array<?> array)
    {
        if (!(array instanceof IntArray))
        {
            throw new RuntimeException("Label images assume inner array implements IntArray");
        }
        return IntArray2D.wrap((IntArray<?>) array);
    }
}
