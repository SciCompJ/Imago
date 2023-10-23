/**
 * 
 */
package imago.app;

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
import net.sci.geom.mesh.Mesh3D;


/**
 * A handle to a geometry.
 * 
 * @author dlegland
 *
 */
public class GeometryHandle extends ObjectHandle
{
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
    
    Geometry geometry;
    
    public GeometryHandle(Geometry geometry, String name, String tag)
    {
        super(tag);
        this.geometry = geometry;
        this.name = name;
    }
    
    public Geometry getGeometry()
    {
        return geometry;
    }

    public Geometry getObject()
    {
        return this.geometry;
    }

}
