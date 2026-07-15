/**
 * 
 */
package imago.image;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import imago.image.render.BinaryImageRenderer;
import imago.image.render.DefaultIntensityImageRenderer;
import imago.image.render.DistanceMapImageRenderer;
import imago.image.render.IndexedColorMapImageRenderer;
import imago.image.render.LabelMapImageRenderer;
import imago.image.render.RGBArrayRenderer;
import imago.image.render.VectorImageChannelRenderer;
import imago.image.render.VectorImageNormRenderer;
import net.sci.array.Array;
import net.sci.array.color.Color;
import net.sci.array.color.ColorMap;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.image.DisplaySettings;
import net.sci.image.Image;
import net.sci.image.ImageType;

/**
 * Converts image data into a renderable AWT Bufferedimage.
 */
public interface ImageDataRenderer
{
    /**
     * Creates an image renderer based on image type, using image settings as
     * initialization values whenever possible.
     * 
     * @param image
     *            the image to render
     * @return a new instance of {@code ImageDataRenderer} tailored for the
     *         specified image.
     */
    public static ImageDataRenderer createRenderer(Image image)
    {
        // create renderer according to image type
        ImageDataRenderer renderer = createRenderer(image.getType());
        
        // initialize with image display settings when possible
        if (renderer instanceof IndexedColorMapImageRenderer renderer2)
        {
            DisplaySettings settings = image.getDisplaySettings();
            renderer2.setDisplayRange(settings.getDisplayRange());
            if (settings.getColorMap() != null) renderer2.setColorMap(settings.getColorMap());
            if (settings.getBackgroundColor() != null) renderer2.setBackgroundColor(settings.getBackgroundColor());
        }
        
        return renderer;
    }
    
    private static ImageDataRenderer createRenderer(ImageType type)
    {
        // integer based image types
        if (type == ImageType.BINARY) return new BinaryImageRenderer();
        if (type == ImageType.LABEL) return new LabelMapImageRenderer();
        if (type == ImageType.DISTANCE) return new DistanceMapImageRenderer();
        if (type == ImageType.GRAYSCALE) return new DefaultIntensityImageRenderer();

        // intensity based image types
        if (type == ImageType.ANGLE) return new DefaultIntensityImageRenderer();
        if (type == ImageType.DIVERGING) return new DefaultIntensityImageRenderer();
        if (type == ImageType.INTENSITY) return new DefaultIntensityImageRenderer();

        // color type(s)
        if (type == ImageType.COLOR) return new RGBArrayRenderer();
        
        // vector image types
        if (type == ImageType.GRADIENT) return new VectorImageChannelRenderer();
        if (type == ImageType.COMPLEX) return new VectorImageNormRenderer();
        if (type == ImageType.VECTOR) return new VectorImageChannelRenderer();
        
        System.err.printf("Unable to create renderer for image type %s, use default intensity renderer\n", type);
        return new DefaultIntensityImageRenderer();
    }
    
    /**
     * Factorization of the method used by grayscale and intensity images. Also used by vector image after co
     * 
     * @param image
     *            the image to convert
     * @return and instance of awt BufferedImage that can be displayed
     */
    public static BufferedImage renderScalarArray2D(ScalarArray2D<?> array, double[] displayRange,
            IndexColorModel colorModel)
    {
        // compute slope for intensity conversions
        double extent = displayRange[1] - displayRange[0];

        int sizeX = array.size(0);
        int sizeY = array.size(1);

        // Create the AWT image
        BufferedImage bufImg = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_BYTE_INDEXED, colorModel);

        // Populate the raster
        WritableRaster raster = bufImg.getRaster();
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                double value = array.getValue(x, y);
                if (!Double.isFinite(value))
                {
                    continue;
                }
                int index = (int) Math.min(Math.max(255 * (value - displayRange[0]) / extent, 0), 255);

                raster.setSample(x, y, 0, index); 
            }
        }

        return bufImg;
    }


    /**
     * Convert the specified colormap into an IndexColorModel.
     * 
     * @param colormap the colormap as 256 array of 3 components
     * @return the corresponding IndexColorModel
     */
    public static IndexColorModel createIndexColorModel(ColorMap colormap)
    {
        // Computes the color model
        byte[] red = new byte[256];
        byte[] green = new byte[256];
        byte[] blue = new byte[256];
        int nColors = colormap.size();
        for(int i = 0; i < 256; i++) 
        {
            Color color = colormap.getColor(i % nColors);
            red[i]      = (byte) (color.red() * 255);
            green[i]    = (byte) (color.green() * 255);
            blue[i]     = (byte) (color.blue() * 255);
        }
        IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);  
        return cm;
    }
    
    /**
     * Convert the colormap given as N-by-3 array into an IndexColorModel.
     * 
     * @param colormap the colormap instance
     * @param background the background color
     * @return the corresponding IndexColorModel, with 256 colors max.
     */
    public static IndexColorModel createIndexColorModel(ColorMap colormap, Color background)
    {
        // allocate color components arrays
        byte[] red = new byte[256];
        byte[] green = new byte[256];
        byte[] blue = new byte[256];

        // first color corresponds to background
        red[0] = (byte) (background.red() * 255);
        green[0] = (byte) (background.green() * 255);
        blue[0] = (byte) (background.blue() * 255);

        // convert colormap colors
        int nColors = Math.min(colormap.size(), 255);
        for(int i = 0; i < nColors; i++) 
        {
            Color color = colormap.getColor(i);
            red[i+1]    = (byte) (color.red() * 255);
            green[i+1]  = (byte) (color.green() * 255);
            blue[i+1]   = (byte) (color.blue() * 255);
        }
        IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);  
        return cm;
    }

    public BufferedImage render(Array<?> array);
    
    public abstract ImageDataRenderer duplicate();
}
