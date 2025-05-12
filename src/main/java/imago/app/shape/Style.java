/**
 * 
 */
package imago.app.shape;

import java.awt.Color;

/**
 * Encapsulates the information for drawing shapes.
 * 
 * {@snippet lang="java" :
 * Style style = new Style()
 *      .setLineColor(Color.RED)
 *      .setLineWidth(2.5)
 *      .setFillColor(Color.YELOW),
 *      .setFillOpacity(0.3);
 * }
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

    /**
     * The opacity of the fill, between 0 and 1. Initialized at 0.5.
     */
    double fillOpacity = 0.5;


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
        this.fillColor = refStyle.fillColor;
        this.fillOpacity = refStyle.fillOpacity;
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
    public Style setLineColor(Color color)
    {
        this.lineColor = color;
        return this;
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
    public Style setLineWidth(double lineWidth)
    {
        this.lineWidth = lineWidth;
        return this;
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
    public Style setFillColor(Color color)
    {
        this.fillColor = color;
        return this;
    }
    
    /**
     * @return the fill opacity
     */
    public double getFillOpacity()
    {
        return fillOpacity;
    }

    /**
     * @param opacity the opacity of the fill, between 0 and 1.
     */
    public Style setFillOpacity(double opacity)
    {
        this.fillOpacity = lineWidth;
        return this;
    }
}
