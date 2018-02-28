/**
 * 
 */
package imago.gui;

//import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import net.sci.array.Array;
import net.sci.array.data.ScalarArray;
import net.sci.array.data.UInt8Array;
import net.sci.array.data.VectorArray;
import net.sci.array.data.color.RGB16Array;
import net.sci.array.data.color.RGB8Array;
import net.sci.array.data.scalar2d.BinaryArray2D;
import net.sci.array.data.scalar2d.IntArray2D;
import net.sci.array.data.scalar2d.ScalarArray2D;
import net.sci.array.data.scalar2d.UInt8Array2D;
import net.sci.array.type.RGB16;
import net.sci.array.type.RGB8;
import net.sci.array.type.Color;
import net.sci.image.ColorMap;
import net.sci.image.ColorMaps;
import net.sci.image.Image;
import net.sci.image.process.shape.ImageSlicer;

/**
 * Collection of methods for managing images.
 * 
 * @author dlegland
 *
 */
public class ImageUtils
{

	/**
	 * 
	 */
	private ImageUtils()
	{
	}
	
	public static final java.awt.image.BufferedImage createAwtImage(Image image, int sliceIndex)
	{
	    // extract specified slice from image
	    Image image2d = ImageSlicer.slice2d(image, 0, 1, new int[]{0, 0, sliceIndex});
	    return createAwtImage(image2d);
	}
	
	public static final java.awt.image.BufferedImage createAwtImage(Image image)
	{
		// extract LUT from image, or create one otherwise
		ColorMap lut = image.getColorMap();
		if (lut == null)
		{
		    lut = ColorMaps.GRAY.createColorMap(256); 
		}

		Array<?> array = image.getData();
        if (array.dimensionality() != 2)
        {
            throw new RuntimeException("Requires inner array to be 2-dimensional, not " + array.dimensionality());
        }
		
        // Displatch process depending on image type
		if (image.isBinaryImage())
		{
		    if (!(array instanceof BinaryArray2D))
		    {
		        throw new RuntimeException("Binary images assume inner array implements BinaryArray2D");
		    }
		    
		    // binary images are converted to bi-color images
		    return createAwtImage((BinaryArray2D) array, Color.RED, Color.WHITE);
		} 
		else if (image.isLabelImage())
		{
            if (!(array instanceof IntArray2D))
            {
                throw new RuntimeException("Label images assume inner array implements IntArray2D");
            }
            
		    return labelToAwtImage((IntArray2D<?>) array, lut, image.getBackgroundColor());
		}
		else if (image.isColorImage())
		{
		    if (array instanceof RGB8Array)
	        {
	            // call the standard way for converting planar RGB images
	            return createAwtImageRGB8((RGB8Array) array);
	        } 
	        else if (array instanceof RGB16Array)
	        {
	            // call the standard way for converting planar RGB16 images
	            return createAwtImageRGB16((RGB16Array) array);
	        }
	        else
	        {
	            throw new RuntimeException("Could not process color image with array of class " + array.getClass().getName());
	        }
		}
		
		// Process array depending on its data type
		if (array instanceof ScalarArray2D)
 		{
 			// scalar images use display range and current LUT
 			double[] displayRange = image.getDisplayRange();
 			return createAwtImage((ScalarArray2D<?>) array, displayRange, lut);
 		}
		else if (array instanceof VectorArray)
		{
			// Compute the norm of the vector
			ScalarArray<?> norm = VectorArray.norm((VectorArray<?>) array);
			
			// convert image of the norm to AWT image
			double[] displayRange = image.getDisplayRange();
 			return createAwtImage((ScalarArray2D<?>) norm, displayRange, lut);
		} 

 		throw new RuntimeException("Could not process image of type " + image.getType() +
 		        ", with array of class " + array.getClass().getName());
	}
	
	
	public static final java.awt.image.BufferedImage createAwtImage(UInt8Array2D array, int[][] lut)
	{
		int sizeX = array.getSize(0);
		int sizeY = array.getSize(1);
		
		// Computes the color model
		byte[] red = new byte[256];
		byte[] green = new byte[256];
		byte[] blue = new byte[256];
		for(int i = 0; i < 256; i++) 
		{
			red[i] = (byte) lut[i][0];
			green[i] = (byte) lut[i][1];
			blue[i] = (byte) lut[i][2];
		}
		IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);  
		
		// Create the AWT image
		int type = java.awt.image.BufferedImage.TYPE_BYTE_INDEXED ;
		BufferedImage bufImg = new BufferedImage(sizeX, sizeY, type, cm);
		
		// Populate the raster
		WritableRaster raster = bufImg.getRaster();
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				int value = array.getInt(x, y);
				raster.setSample(x, y, 0, value); 
			}
		}

		return bufImg;
	}

//	private static final int[][] createGrayLut()
//	{
//		int[][] lut = new int[256][];
//		for (int i = 0; i < 256; i++)
//		{
//			lut[i] = new int[]{i, i, i};
//		}
//		return lut;
//	}
	
	public static final java.awt.image.BufferedImage createAwtImage(
			BinaryArray2D array, Color fgColor, Color bgColor)
	{
		int sizeX = array.getSize(0);
		int sizeY = array.getSize(1);
		
		// Computes the color model
		byte[] red = new byte[256];
		byte[] green = new byte[256];
		byte[] blue = new byte[256];
		red[0] 		= (byte) (bgColor.red() * 255);
		green[0] 	= (byte) (bgColor.green() * 255);
		blue[0] 	= (byte) (bgColor.blue() * 255);
		red[255] 	= (byte) (fgColor.red() * 255);
		green[255] 	= (byte) (fgColor.green() * 255);
		blue[255] 	= (byte) (fgColor.blue() * 255);
		IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);  
		
		// Create the AWT image
		int type = java.awt.image.BufferedImage.TYPE_BYTE_INDEXED ;
		BufferedImage bufImg = new BufferedImage(sizeX, sizeY, type, cm);
		
		// Populate the raster
		WritableRaster raster = bufImg.getRaster();
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				int value = array.getBoolean(x, y) ? 255 : 0;
				raster.setSample(x, y, 0, value); 
			}
		}

		return bufImg;
	}


    public static final java.awt.image.BufferedImage createAwtImage(
            ScalarArray2D<?> array, double[] displayRange, int[][] colormap)
    {
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        
        // Computes the color model
        IndexColorModel cm = createIndexColorModel(colormap);  
        
        // compute slope for intensity conversions
        double extent = displayRange[1] - displayRange[0];
        
        // Create the AWT image
        int type = java.awt.image.BufferedImage.TYPE_BYTE_INDEXED ;
        BufferedImage bufImg = new BufferedImage(sizeX, sizeY, type, cm);
        
        // Populate the raster
        WritableRaster raster = bufImg.getRaster();
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                double value = array.getValue(x, y);
                int sample = (int) Math.min(Math.max(255 * (value - displayRange[0]) / extent, 0), 255);
                raster.setSample(x, y, 0, sample); 
            }
        }

        return bufImg;
    }

    public static final java.awt.image.BufferedImage createAwtImage(
            ScalarArray2D<?> array, double[] displayRange, ColorMap colormap)
    {
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        
        // Computes the color model
        IndexColorModel cm = createIndexColorModel(colormap);  
        
        // compute slope for intensity conversions
        double extent = displayRange[1] - displayRange[0];
        
        // Create the AWT image
        int type = java.awt.image.BufferedImage.TYPE_BYTE_INDEXED ;
        BufferedImage bufImg = new BufferedImage(sizeX, sizeY, type, cm);
        
        // Populate the raster
        WritableRaster raster = bufImg.getRaster();
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                double value = array.getValue(x, y);
                int sample = (int) Math.min(Math.max(255 * (value - displayRange[0]) / extent, 0), 255);
                raster.setSample(x, y, 0, sample); 
            }
        }

        return bufImg;
    }
	
    public static final java.awt.image.BufferedImage labelToAwtImage(
            IntArray2D<?> array, ColorMap colormap, Color backgroundColor)
    {
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        
        // Computes the color model
        IndexColorModel cm = createIndexColorModel(colormap, backgroundColor);  
        
        // Create the AWT image
        int type = java.awt.image.BufferedImage.TYPE_BYTE_INDEXED ;
        BufferedImage bufImg = new BufferedImage(sizeX, sizeY, type, cm);
        
        // Populate the raster
        WritableRaster raster = bufImg.getRaster();
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                int index = Math.min(array.getInt(x, y), 255);
                raster.setSample(x, y, 0, index); 
            }
        }

        return bufImg;
    }
    
    /**
     * Convert the colormap given as N-by-3 array into an IndexColorModel.
     * 
     * @param colormap the colormap as 256 array of 3 components
     * @return the corresponding IndexColorModel
     */
    private final static IndexColorModel createIndexColorModel(int[][] colormap)
    {
        // Computes the color model
        byte[] red = new byte[256];
        byte[] green = new byte[256];
        byte[] blue = new byte[256];
        for(int i = 0; i < 256; i++) 
        {
            red[i]      = (byte) colormap[i][0];
            green[i]    = (byte) colormap[i][1];
            blue[i]     = (byte) colormap[i][2];
        }
        IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);  
        return cm;
    }
    
    /**
     * Convert the colormap given as N-by-3 array into an IndexColorModel.
     * 
     * @param colormap the colormap as 256 array of 3 components
     * @return the corresponding IndexColorModel
     */
    private final static IndexColorModel createIndexColorModel(ColorMap colormap)
    {
        // Computes the color model
        byte[] red = new byte[256];
        byte[] green = new byte[256];
        byte[] blue = new byte[256];
        int nColors = colormap.size();
        for(int i = 0; i < 256; i++) 
        {
            net.sci.array.type.Color color = colormap.getColor(i % nColors);
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
    private final static IndexColorModel createIndexColorModel(ColorMap colormap, Color background)
    {
        // allocate color components arrays
        byte[] red = new byte[256];
        byte[] green = new byte[256];
        byte[] blue = new byte[256];

        // first color corresponds to background
        red[0] = (byte) (background.red() * 255);
        green[0] = (byte) (background.green() * 255);
        blue[0] = (byte) (background.blue() * 255);

        // convert colormapcolors
        int nColors = Math.min(colormap.size(), 255);
        for(int i = 0; i < nColors; i++) 
        {
            net.sci.array.type.Color color = colormap.getColor(i);
            red[i+1]      = (byte) (color.red() * 255);
            green[i+1]    = (byte) (color.green() * 255);
            blue[i+1]     = (byte) (color.blue() * 255);
        }
        IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);  
        return cm;
    }
    
	public static final java.awt.image.BufferedImage createAwtImageRGB8(
			UInt8Array array)
	{
		int sizeX = array.getSize(0);
		int sizeY = array.getSize(1);
		
		int type = java.awt.image.BufferedImage.TYPE_INT_RGB;
		
		BufferedImage bufImg = new BufferedImage(sizeX, sizeY, type);
		WritableRaster raster = bufImg.getRaster();
		
		int[] pos = new int[3];
		for (int y = 0; y < sizeY; y++)
		{
			pos[1] = y;
			for (int x = 0; x < sizeX; x++)
			{
				pos[0] = x;
				for (int c = 0; c < 3; c++)
				{
					pos[2] = c;
					raster.setSample(x, y, c, array.getInt(pos));
				}
			}
		}
		
		return bufImg;
	}

    public static final java.awt.image.BufferedImage createAwtImageRGB8(RGB8Array array)
    {
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        
        int type = java.awt.image.BufferedImage.TYPE_INT_RGB;
        
        BufferedImage bufImg = new BufferedImage(sizeX, sizeY, type);
        WritableRaster raster = bufImg.getRaster();
        
        int[] pos = new int[3];
        for (int y = 0; y < sizeY; y++)
        {
            pos[1] = y;
            for (int x = 0; x < sizeX; x++)
            {
                pos[0] = x;
                RGB8 rgb = array.get(pos);
                for (int c = 0; c < 3; c++)
                {
                    pos[2] = c;
                    raster.setSample(x, y, c, rgb.getSample(c));
                }
            }
        }
        
        return bufImg;
    }

    public static final java.awt.image.BufferedImage createAwtImageRGB16(RGB16Array array)
    {
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        
        // determines max red, green and blue values
        int rMax = 0, gMax = 0, bMax = 0;
        for (RGB16 rgb : array)
        {
            rMax = Math.max(rMax, rgb.getSample(0));
            gMax = Math.max(gMax, rgb.getSample(1));
            bMax = Math.max(bMax, rgb.getSample(2));
        }
        double k = 255.0 / Math.max(Math.max(rMax,  gMax),  bMax);
 
        // create result AWT image
        int type = java.awt.image.BufferedImage.TYPE_INT_RGB;
        BufferedImage bufImg = new BufferedImage(sizeX, sizeY, type);
        WritableRaster raster = bufImg.getRaster();
        
        int[] pos = new int[3];
        for (int y = 0; y < sizeY; y++)
        {
            pos[1] = y;
            for (int x = 0; x < sizeX; x++)
            {
                pos[0] = x;
                RGB16 rgb = array.get(pos);
                for (int c = 0; c < 3; c++)
                {
                    pos[2] = c;
                    raster.setSample(x, y, c, (int) (rgb.getSample(c) * k));
                }
            }
        }
        
        return bufImg;
    }
}
