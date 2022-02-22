/**
 * 
 */
package imago.plugin.plugin.crop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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
    // ===================================================================
    // Fields
    
    /**
     * Description of the image (filename, size...).
     */
    public ImageInfo imageInfo;
    
    /**
     * The reference image, may be null.
     */
    public Image image = null;
    
    /**
     * A list of crop regions, each of them defined by a name and a series of
     * polygons on a selection of slices.
     */
    public ArrayList<Crop3DRegion> regions;
    
    
    // ===================================================================
    // Constructor
    
    /**
     * Empty constructor.
     */
    public Crop3DData()
    {
        this.regions = new ArrayList<Crop3DRegion>();
    }

    /**
     * Initialize from an existing image.
     */
    public Crop3DData(Image image)
    {
        this.image = image;
        this.imageInfo = new ImageInfo(image);
        this.regions = new ArrayList<Crop3DRegion>();
    }

    
    // ===================================================================
    // Image Management
    
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

    
    // ===================================================================
    // Management of regions

    public Collection<Crop3DRegion> regions()
    {
        return this.regions;
    }
    
    public void addRegion(Crop3DRegion region)
    {
        this.regions.add(region);
    }

    public void removeRegion(String regionName)
    {
        Crop3DRegion region = getRegion(regionName);
        this.regions.remove(region);
    }

    public Crop3DRegion getRegion(String regionName)
    {
        for (Crop3DRegion region : regions)
        {
            if (regionName.equals(region.name))
            {
                return region;
            }
        }
        throw new RuntimeException("Crop3DData does not contain any region with name: " + regionName);
    }

}
