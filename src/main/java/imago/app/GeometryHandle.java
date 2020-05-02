/**
 * 
 */
package imago.app;

import net.sci.geom.Geometry;


/**
 * An handle to a geometry.
 * 
 * @author dlegland
 *
 */
public class GeometryHandle extends ObjectHandle
{
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

    public Object getObject()
    {
        return this.geometry;
    }

}
