/**
 * 
 */
package imago.plugin.plugin.crop;

import java.io.File;
import java.io.IOException;

import imago.app.scene.ImageSerialSectionsNode;

/**
 * The data necessary for cropping a region within a 3D image.
 * 
 * @author dlegland
 *
 */
public class Crop3DRegion
{
    // ===================================================================
    // Class members

    /** 
     * The name of the region.
     */
    String name;
    
    /**
     * The manually selected polygons for a selection of slices.
     */
    // TODO: replace ImageSerialSectionsNode by Map<Int,LinearRing2D> 
    ImageSerialSectionsNode polygons;
    
    /**
     * The series of polygons obtained after interpolation of the "polygons"
     * node.
     */
    ImageSerialSectionsNode interpolatedPolygons = null;
    
    
    // ===================================================================
    // Constructors

    /**
     * Creates a new region with default settings.
     */
    public Crop3DRegion()
    {
        this("", new ImageSerialSectionsNode(""));
    }
    
    /**
     * Creates a new region, by specifying its name and the series of polygon.
     * 
     * @param name
     *            the name of the regions
     * @param polygons
     *            the reference polygons
     */
    public Crop3DRegion(String name, ImageSerialSectionsNode polygons)
    {
        this.name = name;
        this.polygons = polygons;
        
        this.interpolatedPolygons = new ImageSerialSectionsNode("");
    }
    
    
    // ===================================================================
    // Methods
    
    public void readPolygonsFromJson(File file) throws IOException
    {
        // read polygons of current region
        Crop3DDataReader reader = new Crop3DDataReader(file);
        this.polygons = reader.readPolygons();
        this.interpolatedPolygons.clear();
    }
}
