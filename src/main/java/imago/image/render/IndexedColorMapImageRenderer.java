/**
 * 
 */
package imago.image.render;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import imago.image.ImageDataRenderer;
import net.sci.array.Array;
import net.sci.array.color.Color;
import net.sci.array.color.ColorMap;
import net.sci.array.color.ColorMaps;
import net.sci.array.color.RGB8;
import net.sci.array.numeric.ScalarArray2D;

/**
 * Implementation stub for all classes that need to render an intensity image.
 * Default behavior is to map intensity values within the interval [0 255] based
 * on the display range, and to generate an indexed buffered image using a
 * colormodel computed from the colormap.
 */
public abstract class IndexedColorMapImageRenderer implements ImageDataRenderer
{
    // =============================================================
    // Class fields

    /**
     * The range of values that will be mapped to the first and last color of
     * the color map.
     */
    double[] displayRange = new double[] {0, 255};
    
    /**
     * The color map, used to build the colormodel.
     */
    ColorMap colorMap;
    
    /**
     * An optional background color, that can used to render zero values or
     * NaN-values.
     */
    Color backgroundColor;
    
    /**
     * The colormodel used to compute the Buffered image. Need to be updated
     * when colormap is updated.
     */
    protected IndexColorModel colorModel;
    
    
    // =============================================================
    // Constructor

    /**
     * Default empty constructor.
     */
    protected IndexedColorMapImageRenderer()
    {
        this.colorMap = ColorMaps.GRAY.createColorMap(256); 
        this.backgroundColor = RGB8.BLACK;
        updateColorModel();
    }
    
    
    // =============================================================
    // Getters / Setters

    public double[] getDisplayRange()
    {
        return this.displayRange;
    }
    
    public IndexedColorMapImageRenderer setDisplayRange(double[] displayRange)
    {
        if (displayRange == null || displayRange.length != 2)
        {
            throw new RuntimeException("display range must be an array with two values");
        }
        this.displayRange = displayRange;
        return this;   
    }
    
    public ColorMap getColorMap()
    {
        return this.colorMap;   
    }
    
    public IndexedColorMapImageRenderer setColorMap(ColorMap colorMap)
    {
        this.colorMap = colorMap;
        updateColorModel();
        return this;   
    }
    
    public Color getBackgroundColor()
    {
        return this.backgroundColor;
    }
    
    public IndexedColorMapImageRenderer setBackgroundColor(Color bgColor)
    {
        this.backgroundColor = bgColor;
        updateColorModel();
        return this;   
    }
    

    // =============================================================
    // Computation methods

    /**
     * Creates the 256-colors color model based on current colormap. Can be
     * overridden to provide a more specific behavior.
     */
    public void updateColorModel()
    {
        this.colorModel = ImageDataRenderer.createIndexColorModel(this.colorMap);
    }
    
    @Override
    public BufferedImage render(Array<?> array)
    {
        ScalarArray2D<?> array2d = computeRenderableArray(array);
        return ImageDataRenderer.renderScalarArray2D(array2d, displayRange, colorModel);
    }
    
    public abstract ScalarArray2D<?> computeRenderableArray(Array<?> array);
}
