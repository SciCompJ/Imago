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
public class CommonImageFileFilters
{
    /**
     * File filter for TIFF file extension.
     */
    public static final FileFilter TIFF = new FileNameExtensionFilter("TIFF files (*.tif, *.tiff)", "tif", "tiff");

    /**
     * File filter for METAIMAGE file extension.
     */
    public static final FileFilter META_IMAGE = new FileNameExtensionFilter("MetaImage files (*.mhd, *.mha)", "mhd", "mha");
    
    /**
     * Private constructor to prevent instantiation.
     */
    private CommonImageFileFilters()
    {
    }
}
