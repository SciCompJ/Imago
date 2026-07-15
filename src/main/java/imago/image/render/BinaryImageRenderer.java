package imago.image.render;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import imago.image.ImageDataRenderer;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.color.Color;
import net.sci.array.color.ColorMap;
import net.sci.array.color.RGB8;

public class BinaryImageRenderer implements ImageDataRenderer
{
    IndexColorModel colorModel;
    
    /**
     * default empty constructor.
     */
    public BinaryImageRenderer()
    {
        RGB8 bgColor = RGB8.WHITE;
        RGB8 fgColor = RGB8.RED;

        // Computes the color model
        byte[] red = new byte[256];
        byte[] green = new byte[256];
        byte[] blue = new byte[256];
        red[0]      = (byte) (bgColor.red() * 255);
        green[0]    = (byte) (bgColor.green() * 255);
        blue[0]     = (byte) (bgColor.blue() * 255);
        red[255]    = (byte) (fgColor.red() * 255);
        green[255]  = (byte) (fgColor.green() * 255);
        blue[255]   = (byte) (fgColor.blue() * 255);
        this.colorModel = new IndexColorModel(8, 256, red, green, blue);  
    }
    
    public BinaryImageRenderer setColorMap(ColorMap colorMap)
    {
        this.colorModel = createIndexColorModel(colorMap);  
        return this;   
    }
    
    @Override
    public BufferedImage render(Array<?> array)
    {
        if (array.elementClass() != Binary.class)
        {
            throw new RuntimeException("Binary images must refer to an array of Binary");
        }

        // ensure array is binary class
        BinaryArray2D binaryArray = BinaryArray2D.wrap(BinaryArray.wrap(array)); 

        // retrieve image size
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
                int value = binaryArray.getBoolean(x, y) ? 255 : 0;
                raster.setSample(x, y, 0, value); 
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
    private static IndexColorModel createIndexColorModel(ColorMap colormap)
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

    @Override
    public ImageDataRenderer duplicate()
    {
        BinaryImageRenderer dup = new BinaryImageRenderer();
        return dup;
    }
}
