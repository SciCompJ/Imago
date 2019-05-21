/**
 * 
 */
package imago.scene;

import java.awt.Graphics2D;

import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.geom.geom2d.polygon.Polyline2D;

/**
 * A scene item that refers to a 2D geometry.
 * 
 * @author dlegland
 *
 */
public class Geometry2DItem extends SceneItem
{
    Geometry2D geometry;
    
    // TODO: add style management...
    
    public Geometry2DItem(String name, Geometry2D geom)
    {
        super(name);
        this.geometry = geom;
    }
    
    public Geometry2D getGeometry()
    {
        return this.geometry;
    }
    
    public void draw(Graphics2D g2)
    {
        if (this.geometry instanceof Polygon2D)
        {
            drawPolygon(g2, (Polygon2D) this.geometry);
        }
        else if (this.geometry instanceof Polyline2D)
        {
            drawPolyline(g2, (Polyline2D) this.geometry);
        }
        else
        {
            System.err.println("Unable to draw geometry with class: " + this.geometry.getClass().getName());
        }
    }
    
    private void drawPolygon(Graphics2D g2, Polygon2D poly)
    {
        drawPolyline(g2, poly.boundary());
    }

    private void drawPolyline(Graphics2D g2, Polyline2D poly)
    {
        if (poly.vertexNumber() < 2)
            return;
        
        java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();

        int nv = poly.vertexNumber();
        net.sci.geom.geom2d.Point2D p0 = poly.vertexPosition(nv - 1);
        path.moveTo((float) p0.getX(), (float) p0.getY());
        
        // process each point
        for(Point2D point : poly.vertexPositions())
        {
            path.lineTo((float) point.getX(), (float) point.getY());
        }
        
        // close the path, even if the path is already at the right position
        path.closePath();

        g2.draw(path);
    }
}
