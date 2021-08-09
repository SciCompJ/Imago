/**
 * 
 */
package imago.plugin.plugin.crop;

import java.io.File;
import java.io.IOException;

import imago.app.scene.ImageSerialSectionsNode;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;

/**
 * Container for the data of the Crop3D plugin, mainly used to retrieve data
 * from files.
 * 
 * @author dlegland
 *
 */
public class Crop3DData
{
    /**
     * Description of the image (filename, size...).
     */
    public ImageInfo imageInfo;
    
    /**
     * The reference image, may be null.
     */
    public Image image = null;
    
    /**
     * The list of manually edited polygons, that will be used to compute the
     * interpolated ones.
     */
    public ImageSerialSectionsNode polygons;
    
    /**
     * Empty constructor.
     */
    public Crop3DData()
    {
    }
    
    /**
     * Opens a (virtual) image from the information stored in ImageInfo.
     * @throws IOException 
     */
    public void openImage() throws IOException
    {
        // Check necessary information have been loaded
        if (imageInfo.filePath == null)
        {
            throw new RuntimeException("Could not load image file information.");
        }
        
        // create the file
        File file = new File(imageInfo.filePath);
        
        // open a virtual image from the file
        TiffImageReader reader = new TiffImageReader(file);
        this.image = reader.readVirtualImage3D();
    }
}
