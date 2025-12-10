/**
 * 
 */
package imago.gui.plugins.plugins.crop;

import net.sci.array.Array;
import net.sci.image.Image;

/**
 * Utility class used to store image information into a JSON file.
 */
public class ImageInfo
{
    /**
     * The name of the image.
     */
    public String name = "";
    
    /**
     * The name of the file storing image data.
     */
    public String filePath = "";
    
    /**
     * The dimensionality of the image.
     */
    public int nDims = 0;
    
    /**
     * The size of the image along each dimension.
     */
    public int[] size;
    
    /**
     * Empty constructor, for gathering information during import.
     */
    public ImageInfo()
    {
    }

    /**
     * Constructor from an image, for writing or initialization.
     */
    public ImageInfo(Image image)
    {
        this.name = image.getName();
        this.filePath = image.getFilePath();
        Array<?> array = image.getData();
        this.nDims = array.dimensionality();
        this.size = array.size();
    }
}
