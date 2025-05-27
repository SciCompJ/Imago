/**
 * 
 */
package imago.app;

import java.util.ArrayList;
import java.util.Collection;

import net.sci.geom.Geometry;
import net.sci.geom.MultiPoint;
import net.sci.geom.Point;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.StraightLine2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.geom.geom3d.LineSegment3D;
import net.sci.geom.geom3d.Polygon3D;
import net.sci.geom.geom3d.StraightLine3D;
import net.sci.geom.geom3d.polyline.Polyline3D;
import net.sci.geom.mesh3d.Mesh3D;


/**
 * A handle to a geometry.
 * 
 * @author dlegland
 *
 */
public class GeometryHandle extends ObjectHandle
{
    // =============================================================
    // Static utility methods
    
    /**
     * Creates a new handle for a geometry, adds it to the workspace, and
     * returns the created handle.
     * 
     * @param geom
     *            the geometry.
     * @return the handle to manage the geometry.
     */
    public static final GeometryHandle create(ImagoApp app, Geometry geom)
    {
        return create(app, geom, null);
    }

    /**
     * Creates a new handle for a geometry, adds it to the workspace, and
     * returns the created handle.
     * 
     * @param geom
     *            the geometry.
     * @param parent
     *            a parent handle, used to initialize handles fields.
     * @return the handle to manage the geometry.
     */
    public static final GeometryHandle create(ImagoApp app, Geometry geom, GeometryHandle parent)
    {
        String baseTag = createTag(geom);
        String name = baseTag;
        
        Workspace workspace = app.getWorkspace();
        String tag = workspace.findNextFreeTag(baseTag);

        GeometryHandle handle = new GeometryHandle(geom, name, tag);
        workspace.addHandle(handle);
        return handle;
    }

    /**
     * Returns all the geometry handles contained in the application.
     * 
     * @param app
     *            the application to explore
     * @return the list of all table handles within the application workspace
     */
    public static final Collection<GeometryHandle> getAll(ImagoApp app)
    {
        ArrayList<GeometryHandle> res = new ArrayList<GeometryHandle>();
        for (ObjectHandle handle : app.getWorkspace().getHandles())
        {
            if (handle instanceof GeometryHandle)
            {
                res.add((GeometryHandle) handle);
            }
        }
        return res;
    }
    
    /**
     * Get the name of all geometry handles.
     * 
     * @return the list of names of handles containing geometries.
     */
    public static final Collection<String> getAllNames(ImagoApp app)
    {
        ArrayList<String> res = new ArrayList<String>();
        for (ObjectHandle handle : app.getWorkspace().getHandles())
        {
            if (handle instanceof GeometryHandle)
            {
                res.add(handle.getName());
            }
        }
        return res;
    }
    
    public static final GeometryHandle findFromName(ImagoApp app, String handleName)
    {
        for (ObjectHandle handle : app.getWorkspace().getHandles())
        {
            if (handle instanceof GeometryHandle)
            {
                if (handle.getName().equals(handleName))
                    return (GeometryHandle) handle;
            }
        }
        
        throw new IllegalArgumentException("App does not contain any geometry handle with name: " + handleName);
    }

    /**
     * Generates a default tag for a geometry based on geometry class. For
     * example, Point geometries will generate tag "pnt", polygon or polyline
     * geometries will generate tag "poly", and so on. Default tag is d"geom".
     * 
     * @param geom
     *            the geometry
     * @return a string that can be used as tag base for the geometry handle
     */
    public static final String createTag(Geometry geom)
    {
        if (geom instanceof Polygon2D || geom instanceof Polyline2D) return "poly";
        if (geom instanceof Polygon3D || geom instanceof Polyline3D) return "poly";
        if (geom instanceof Mesh3D) return "mesh";
        if (geom instanceof Point || geom instanceof MultiPoint) return "pnt";
        if (geom instanceof LineSegment2D || geom instanceof StraightLine2D) return "line";
        if (geom instanceof LineSegment3D || geom instanceof StraightLine3D) return "line";
        return "geom";    
    }
    
    
    // =============================================================
    // Class members
    
    Geometry geometry;
    
    
    // =============================================================
    // Constructor
    
    public GeometryHandle(Geometry geometry, String name, String tag)
    {
        super(tag);
        this.geometry = geometry;
        this.name = name;
    }
    
    
    // =============================================================
    // Data access methods
    
    public Geometry getGeometry()
    {
        return geometry;
    }

    public Geometry getObject()
    {
        return this.geometry;
    }

}
