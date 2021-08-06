/**
 * 
 */
package imago.plugin.plugin.crop;

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
}
