/**
 * 
 */
package imago.image.plugins.convert;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.plugins.ImageFramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.color.ColorMap;
import net.sci.array.color.ColorMaps;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.image.DisplaySettings;
import net.sci.image.Image;

/**
 * Convert an image into 24 bits RGB using current display settings (display
 * range and color map).
 * 
 * @author dlegland
 * @see imago.image.ImageDataRenderer
 */
public class ConvertImageToRGB8 implements ImageFramePlugin
{
    /**
     * Empty default constructor.
     */
    public ConvertImageToRGB8()
    {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        
        if (image == null)
        {
            return;
        }
        Array<?> array = image.getData();
        if (array == null)
        {
            return;
        }
        
        // retrieve display settings of current viewer
        DisplaySettings settings = imageFrame.getImageViewer().getDisplaySettings(); 
        double[] displayRange = settings.getDisplayRange();
        ColorMap colorMap = settings.getColorMap();
        
        RGB8Array rgb;
        if (array.elementInstanceOf(RGB8.class))
        {
            @SuppressWarnings("unchecked")
            RGB8Array tmp = RGB8Array.wrap((Array<RGB8>) array);
            rgb = tmp;
        }
        if (array.elementInstanceOf(Binary.class))
        {
            @SuppressWarnings({ "unchecked" })
            BinaryArray binaryArray = BinaryArray.wrap((Array<Binary>) array); 
            rgb = convertBinaryArray(binaryArray, RGB8.WHITE, RGB8.RED);
        }
        else if (array.elementInstanceOf(Scalar.class))
        {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            ScalarArray scalarArray = ScalarArray.wrap((Array<Scalar>) array); 
            rgb = convertScalarArray(scalarArray, displayRange, colorMap);
        }
        else
        {
            throw new IllegalArgumentException("Can not convert to RGB from array with element class " + array.elementClass().getName());
        };

        Image rgbImage = new Image(rgb, image);
        rgbImage.setName(image.getName() + "-rgb");
                
        // add the image document to GUI
        ImageFrame.create(rgbImage, frame);
    }
    
    private RGB8Array convertBinaryArray(BinaryArray array, RGB8 bgColor, RGB8 fgColor)
    {
        // allocate result array
        RGB8Array res = RGB8Array.create(array.size());
        
        // iterate over positions
        for (int[] pos : res.positions())
        {
            res.set(pos, array.getBoolean(pos) ? fgColor : bgColor);
        }
        
        return res;
    }
    
    
    /**
     * Converts an array of scalars into an array of RGB8, using specified
     * display range and colormap.renderScalarArray2D(ScalarArray2D<?>, double[], IndexColorModel)
     * 
     * @see imago.image.ImageDataRenderer#renderScalarArray2D(net.sci.array.numeric.ScalarArray2D, double[], java.awt.image.IndexColorModel)
     * 
     * @param array
     *            the array to convert
     * @param displayRange
     *            the range of values within array
     * @param colorMap
     *            the colormap
     * @return the converted array
     */
    private RGB8Array convertScalarArray(ScalarArray<?> array, double[] displayRange, ColorMap colorMap)
    {
        // compute slope for intensity conversions
        double extent = displayRange[1] - displayRange[0];
        
        RGB8[] colors = createRGB8Array(colorMap);
        
        // allocate result array
        RGB8Array res = RGB8Array.create(array.size());
        
        // iterate over positions
        for (int[] pos : res.positions())
        {
            double value = array.getValue(pos);
            if (!Double.isFinite(value))
            {
                continue;
            }
            int index = (int) Math.min(Math.max(255 * (value - displayRange[0]) / extent, 0), 255);

            res.set(pos, colors[index]);
        }
        
        return res;
    }
    
    private static final RGB8[] createRGB8Array(ColorMap colorMap)
    {
        // make sure colormap is valid and contains 256 values
        if (colorMap == null)
        {
            colorMap = ColorMaps.GRAY.createColorMap(256);
        }
        else if (colorMap.size() != 256)
        {
            colorMap = ColorMaps.interpolate(colorMap, 256);
        }
        
        // create array of RGB8 values corresponding to the color map
        RGB8[] colors = new RGB8[256];
        for (int i = 0; i < 256; i++)
        {
            colors[i] = RGB8.fromColor(colorMap.getColor(i));
        }
        return colors;
    }
}
