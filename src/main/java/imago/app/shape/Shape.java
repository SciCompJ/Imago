/**
 * 
 */
package imago.app.shape;

import java.awt.Color;

import imago.app.scene.Style;
import net.sci.geom.Geometry;

/**
 * A shape that can be used for drawing annotations, segmentation results, bounding boxes...
 * 
 * @author dlegland
 *
 */
public class Shape
{
    // ===================================================================
    // Class variables
    
    Geometry geometry;
    
    Style style;

    
    // ===================================================================
    // Constructors
    
    public Shape(Geometry geometry)
    {
        this.geometry = geometry;
        this.style = new Style();
    }

    public Shape(Geometry geometry, Style style)
    {
        this.geometry = geometry;
        this.style = style;
    }

    // ===================================================================
    // Accessors and mutators
    
    /**
     * @return the geometry
     */
    public Geometry getGeometry()
    {
        return geometry;
    }


    /**
     * @param geometry the geometry to set
     */
    public void setGeometry(Geometry geometry)
    {
        this.geometry = geometry;
    }


    /**
     * @return the color
     */
    public Color getColor()
    {
        return style.getColor();
    }


    /**
     * @param color the color to set
     */
    public void setColor(Color color)
    {
        this.style.setColor(color);
    }


    /**
     * @return the lineWidth
     */
    public double getLineWidth()
    {
        return style.getLineWidth();
    }


    /**
     * @param lineWidth the lineWidth to set
     */
    public void setLineWidth(double lineWidth)
    {
        this.style.setLineWidth(lineWidth);
    }
    
}