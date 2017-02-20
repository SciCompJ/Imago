/**
 * 
 */
package imago.app.shape;

import java.awt.Color;

import net.sci.geom.Geometry;

/**
 * A shape that can be used for drawing annotations, segmentation results, bounding boxes...
 * 
 * @author dlegland
 *
 */
public class ImagoShape
{
    // ===================================================================
    // Class variables
    
    Geometry geometry;
    
    Color color = Color.BLUE;

    double lineWidth = 1.0;

    
    // ===================================================================
    // Constructors
    
    public ImagoShape(Geometry geometry)
    {
        this.geometry = geometry;
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
        return color;
    }


    /**
     * @param color the color to set
     */
    public void setColor(Color color)
    {
        this.color = color;
    }


    /**
     * @return the lineWidth
     */
    public double getLineWidth()
    {
        return lineWidth;
    }


    /**
     * @param lineWidth the lineWidth to set
     */
    public void setLineWidth(double lineWidth)
    {
        this.lineWidth = lineWidth;
    }
    
}
