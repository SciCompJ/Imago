/**
 * 
 */
package imago.plugin.image.file;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A collection of commonly used file filters.
 * 
 * @author dlegland
 */
public class ImageFileFilters
{
    /**
     * File filter for most common image file extensions.
     */
    public static final FileFilter COMMON = new FileNameExtensionFilter("Common image files", "bmp", "gif", "jpg", "jpeg", "png", "tif", "tiff");
    
    /**
     * File filter for BitMap file extension.
     */
    public static final FileFilter BMP = new FileNameExtensionFilter("BMP - Bitmap files", "bmp");

    /**
     * File filter for GIF (Graphics Interchange Format) file extension.
     */
    public static final FileFilter GIF = new FileNameExtensionFilter("GIF - Graphics Interchange Format", "gif");
    
    /**
     * File filter for JPEG file extension.
     */
    public static final FileFilter JPEG = new FileNameExtensionFilter("JPG/JPEG - JPEG files", "jpg", "jpeg");

    /**
     * File filter for METAIMAGE file extension.
     */
    public static final FileFilter META_IMAGE = new FileNameExtensionFilter("MHD/MHA - MetaImage files", "mhd", "mha");
    
    /**
     * File filter for PNG file extension.
     */
    public static final FileFilter PNG = new FileNameExtensionFilter("PNG - Portable Network Graphics", "png");

    /**
     * File filter for TIFF file extension.
     */
    public static final FileFilter TIFF = new FileNameExtensionFilter("TIF/TIFF - TIFF files", "tif", "tiff");

    /**
     * Private constructor to prevent instantiation.
     */
    private ImageFileFilters()
    {
    }
}
