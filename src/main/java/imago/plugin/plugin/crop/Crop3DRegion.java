/**
 * 
 */
package imago.plugin.plugin.crop;

import imago.app.scene.ImageSerialSectionsNode;

/**
 * The data necessary for cropping a region within a 3D image.
 * 
 * @author dlegland
 *
 */
public class Crop3DRegion
{
    /** 
     * The name of the region.
     */
    String name;
    
    /**
     * The manually selected polygons for a selection of slices.
     */
    ImageSerialSectionsNode polygons;
    
    public Crop3DRegion()
    {
        this.polygons = new ImageSerialSectionsNode("");
    }
    
    public Crop3DRegion(String name, ImageSerialSectionsNode polygons)
    {
        this.name = name;
        this.polygons = polygons;
    }
}
