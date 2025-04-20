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
    Color lineColor = Color.BLUE;

    /**
     * The thickness used to draw lines.
     */
    double lineWidth = 1.0;

    /**
     * The color used to fill shapes.
     */
    Color fillColor = Color.CYAN;


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
    	this.lineColor = refStyle.lineColor;
    	this.lineWidth = refStyle.lineWidth;
    }
    
    
    // ===================================================================
    // Accessors and mutators
    
    /**
     * @return the color to draw lines
     */
    public Color getLineColor()
    {
        return lineColor;
    }

    /**
     * @param color the color to draw lines
     */
    public void setLineColor(Color color)
    {
        this.lineColor = color;
    }

    /**
     * @return the width of lines
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
    
    /**
     * @return the color used to fill shapes
     */
    public Color getFillColor()
    {
        return fillColor;
    }

    /**
     * @param color the new color used to fill shapes
     */
    public void setFillColor(Color color)
    {
        this.fillColor = color;
    }
}
