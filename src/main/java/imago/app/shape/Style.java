/**
 * 
 */
package imago.app.shape;

import java.awt.Color;

/**
 * Encapsulates the information for drawing shapes.
 * 
 * @author dlegland
 */
public class Style
{
    // ===================================================================
    // Class variables
    
    /**
     * The color used to draw lines.
     */
    Color color = Color.BLUE;

    /**
     * The thickness used to draw lines.
     */
    double lineWidth = 1.0;


    // ===================================================================
    // Constructors
    
    /**
     * Empty constructor.
     */
    public Style()
    {
    	
    }
    
    /**
     * Copy constructor from another style.
     * 
     * @param refStyle
     *            the style to copy.
     */
    public Style(Style refStyle)
    {
    	this.color = refStyle.color;
    	this.lineWidth = refStyle.lineWidth;
    }
    
    
    // ===================================================================
    // Accessors and mutators
    
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
