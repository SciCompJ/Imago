/**
 * 
 */
package imago.image.render;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import imago.image.ImageDataRenderer;
import net.sci.array.Array;
import net.sci.array.color.RGB16;
import net.sci.array.color.RGB16Array;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;

/**
 * 
 */
public class RGBArrayRenderer implements ImageDataRenderer
{
    double[] displayRange = new double[] {0, Short.MAX_VALUE};
    
    public RGBArrayRenderer()
    {
    }

    public RGBArrayRenderer setDisplayRange(double[] displayRange)
    {
        if (displayRange == null || displayRange.length != 2)
        {
            throw new RuntimeException("display range must be an array with two values");
        }
        this.displayRange = displayRange;
        return this;
    }
    
    @Override
    public BufferedImage render(Array<?> array)
    {
        // Check if the array contains RGB8 data
        if (array.elementClass() == RGB8.class)
        {
             return createAwtImageRGB8(RGB8Array.wrap(array));
        }
        
        // convert RBG16 image to AWT image, using display range
        if (array.elementClass() == RGB16.class)
        {
            return createAwtImageRGB16(RGB16Array.wrap(array), this.displayRange);
        }
        
        throw new RuntimeException("Could not process color image with array of class " + array.getClass().getName());
    }

    private static final java.awt.image.BufferedImage createAwtImageRGB8(RGB8Array array)
    {
        // retrieve array size
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        BufferedImage bufImg = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = bufImg.getRaster();
        
        int[] pos = new int[2];
        for (int y = 0; y < sizeY; y++)
        {
            pos[1] = y;
            for (int x = 0; x < sizeX; x++)
            {
                pos[0] = x;
                RGB8 rgb = array.get(pos);
                for (int c = 0; c < 3; c++)
                {
                    raster.setSample(x, y, c, rgb.getSample(c));
                }
            }
        }
        
        return bufImg;
    }

    /**
     * Converts the RGB16 array into an instance of BufferedImage, by converting all
     * color components into 0 and 255, taking into account the specified display
     * range.
     * 
     * @param array        the array to convert, only the first two dimensions are
     *                     processed.
     * @param displayRange the values that will be mapped to 0 and 255 in each
     *                     channel.
     * @return an instance of BufferedImage that can be easily displayed.
     */
    private static final java.awt.image.BufferedImage createAwtImageRGB16(RGB16Array array, double displayRange[])
    {
        // compute color adjustment factor
        if (displayRange.length < 2)
        {
            throw new IllegalArgumentException("Display range must have two elements");
        }
        double v0 = displayRange[0];
        double k = 255.0 / (displayRange[1] - v0);
 
        // retrieve array size
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        // allocate memory for result AWT image
        BufferedImage bufImg = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = bufImg.getRaster();
        
        // prepare for iteration
        int[] pos = new int[2];
        int[] samples = new int[3];
        
        // iterate over positions within array
        for (int y = 0; y < sizeY; y++)
        {
            pos[1] = y;
            for (int x = 0; x < sizeX; x++)
            {
                pos[0] = x;
                array.getSamples(pos, samples);

                // process each channel of current pixel
                for (int c = 0; c < 3; c++)
                {
                    int v = (int) Math.min(Math.max((samples[c] - v0) * k, 0), 255);
                    raster.setSample(x, y, c, v);
                }
            }
        }
        
        return bufImg;
    }

    @Override
    public RGBArrayRenderer duplicate()
    {
        RGBArrayRenderer dup = new RGBArrayRenderer();
        dup.setDisplayRange(displayRange);
        return dup;
    }
}
