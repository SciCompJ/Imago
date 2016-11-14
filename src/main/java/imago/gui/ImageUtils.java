/**
 * 
 */
package imago.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import net.sci.array.Array;
import net.sci.array.Cursor;
import net.sci.array.data.ScalarArray;
import net.sci.array.data.UInt8Array;
import net.sci.array.data.VectorArray;
import net.sci.array.data.color.RGB8Array;
import net.sci.array.data.scalar2d.BooleanArray2D;
import net.sci.array.data.scalar2d.ScalarArray2D;
import net.sci.array.data.scalar2d.UInt8Array2D;
import net.sci.array.type.RGB8;
import net.sci.image.Image;

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
		// extract LUT from image, or create one otherwise
		int[][] lut = image.getColorMap();
		if (lut == null)
		{
			lut = createGrayLut(); 
		}

		// Ensure data is a planar image
		Array<?> array = getImageSlice(image, sliceIndex);

		// Process array depending on its data type
		if (array instanceof BooleanArray2D)
		{
			// binary images are converted to bi-color images
			return createAwtImage((BooleanArray2D) array, Color.RED, Color.WHITE);
		}
 		else if (array instanceof ScalarArray2D)
 		{
 			// scalar images use display range and current LUT
 			double[] displayRange = image.getDisplayRange();
 			return createAwtImage((ScalarArray2D<?>) array, displayRange, lut);
 		}
		else if (array instanceof RGB8Array)
		{
			// call the standard way for converting planar RGB images
			return createAwtImageRGB8((RGB8Array) array);
		} 
 		else if (image.isColorImage())
 		{
 			// obsolete, and should be removed
 			System.err.println("Color images should implement RGB8Array interface");
 			return createAwtImageRGB8((UInt8Array) array);
 		}
		else if (array instanceof VectorArray)
		{
			// Compute the norm of the vector
			ScalarArray<?> norm = VectorArray.norm((VectorArray<?>) array);
//			double vMax = 0;
//			for (Scalar item : norm)
//			{
//				vMax = Math.max(vMax, item.getValue());
//			}
			
			// convert image of the norm to AWT image
			double[] displayRange = image.getDisplayRange();
 			return createAwtImage((ScalarArray2D<?>) norm, displayRange, lut);
		} 

 		return null;
	}
	
	private static final Array<?> getImageSlice(Image image, int sliceIndex)
	{
		Array<?> array = image.getData();
		if (image.getDimension() == 2)
		{
			return array;
		}

		int nd = array.dimensionality();
		array = createArraySlice(array, nd - 1, sliceIndex);
		return array;
	}
	
//	private static <T> Array<T> createArraySlice(Array<T> array, int sliceIndex)
//	{
//		// check validity of dimension number 
//		if (array.dimensionality() < 3)
//		{
//			throw new IllegalArgumentException("Requires an array with at least three dimensions");
//		}
//		
//		// check validity of slice index
//		int sizeZ = array.getSize(2);
//		if (sliceIndex < 0 || sliceIndex >= sizeZ)
//		{
//			throw new IllegalArgumentException(String.format("Slice index (%d) must be comprised between 0 and %d", sliceIndex, sizeZ-1));
//		}
//
//		// create new array for slice
//		int sizeX = array.getSize(0);
//		int sizeY = array.getSize(1);
//		Array<T> slice = array.newInstance(sizeX, sizeY);
//		
//		// create position cursors
//		int[] pos3d = new int[3];
//		pos3d[2] = sliceIndex;
//		int[] pos2d = new int[2];
//		
//		// iterate over slice pixels
//		for (int y = 0; y < sizeY; y++)
//		{
//			pos3d[1] = y; 
//			pos2d[1] = y;
//			for (int x = 0; x < sizeX; x++)
//			{
//				pos3d[0] = x; 
//				pos2d[0] = x;
//				slice.set(pos2d, array.get(pos3d));
//			}
//		}
//
//		return slice;
//	}
	
	private static <T> Array<T> createArraySlice(Array<T> array, int dim, int sliceIndex)
	{
		// check validity of dimension number 
		if (array.dimensionality() < 3)
		{
			throw new IllegalArgumentException("Requires an array with at least three dimensions");
		}
		
		// check validity of slice index
		int sizeZ = array.getSize(dim);
		if (sliceIndex < 0 || sliceIndex >= sizeZ)
		{
			throw new IllegalArgumentException(String.format("Slice index (%d) must be comprised between 0 and %d", sliceIndex, sizeZ-1));
		}

		// infos of initial array
		int nd = array.dimensionality();
		int[] dims = array.getSize();
		
		// create new array for slice
		int nd2 = nd - 1;
		int[] dims2 = new int[nd2];
		for (int d = 0; d < dim; d++)
		{
			dims2[d] = dims[d];
		}
		for (int d = dim; d < nd2; d++)
		{
			dims2[d] = dims[d+1];
		}
		
		Array<T> slice = array.newInstance(dims2);
		
		// create position cursors
		int[] pos0 = new int[nd];
		pos0[dim] = sliceIndex;
		int[] pos2;
		
		// iterate over slice pixels
		Cursor cursor = slice.getCursor();
		while (cursor.hasNext())
		{
			// get current position on slice
			cursor.forward();
			pos2 = cursor.getPosition();
			
			// convert position on slice to position in original array
			for (int d = 0; d < dim; d++)
			{
				pos0[d] = pos2[d];
			}
			for (int d = dim; d < nd2; d++)
			{
				pos0[d+1] = pos2[d];
			}
			
			slice.set(pos2, array.get(pos0));
		}

		return slice;
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

	private static final int[][] createGrayLut()
	{
		int[][] lut = new int[256][];
		for (int i = 0; i < 256; i++)
		{
			lut[i] = new int[]{i, i, i};
		}
		return lut;
	}
	
	public static final java.awt.image.BufferedImage createAwtImage(
			BooleanArray2D array, Color fgColor, Color bgColor)
	{
		int sizeX = array.getSize(0);
		int sizeY = array.getSize(1);
		
		// Computes the color model
		byte[] red = new byte[256];
		byte[] green = new byte[256];
		byte[] blue = new byte[256];
		red[0] 		= (byte) bgColor.getRed();
		green[0] 	= (byte) bgColor.getGreen();
		blue[0] 	= (byte) bgColor.getBlue();
		red[255] 	= (byte) fgColor.getRed();
		green[255] 	= (byte) fgColor.getGreen();
		blue[255] 	= (byte) fgColor.getBlue();
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
				int value = array.getState(x, y) ? 255 : 0;
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
			red[i] 		= (byte) colormap[i][0];
			green[i] 	= (byte) colormap[i][1];
			blue[i] 	= (byte) colormap[i][2];
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

	public static final java.awt.image.BufferedImage createAwtImageRGB8(
			RGB8Array array)
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

}
