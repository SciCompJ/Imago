/**
 * 
 */
package imago.gui.plugins.plugins.crop;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import net.sci.geom.polygon2d.LinearRing2D;

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
    Map<Integer, LinearRing2D> polygons;
    
    /**
     * The series of polygons obtained after interpolation of the "polygons"
     * node.
     */
    Map<Integer, LinearRing2D> interpolatedPolygons = null;
    
    
    // ===================================================================
    // Constructors

    /**
     * Creates a new region with default settings.
     */
    public Crop3DRegion()
    {
        this("", new TreeMap<Integer, LinearRing2D>());
    }
    
    /**
     * Creates a new empty region identified by its name.
     * 
     * @param name
     *            the name of the region
     */
    public Crop3DRegion(String name)
    {
        this(name, new TreeMap<Integer, LinearRing2D>());
    }
    
    /**
     * Creates a new region, by specifying its name and the series of polygon.
     * 
     * @param name
     *            the name of the region
     * @param polygons
     *            the reference polygons
     */
    public Crop3DRegion(String name, Map<Integer, LinearRing2D> polygons)
    {
        this.name = name;
        this.polygons = polygons;
        
        this.interpolatedPolygons = new TreeMap<Integer, LinearRing2D>();
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
