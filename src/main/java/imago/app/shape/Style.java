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
    // Enumerations
    
    // ===================================================================
    // Class variables
    
    /**
     * A boolean flag for visibility of marker elements.
     */
    boolean markerVisible = true;
    
    /**
     * The type of marker.
     */
    MarkerType markerType = MarkerType.CIRCLE;
    
    /**
     * The color used to draw markers.
     */
    Color markerColor = Color.BLUE;
    
    /**
     * The size of the marker elements.
     */
    int markerSize = 6;
        
    /**
     * A boolean flag for visibility of line elements.
     */
    boolean lineVisible = true;
    
    /**
     * The color used to draw lines.
     */
    Color lineColor = Color.BLUE;

    /**
     * The thickness used to draw lines.
     */
    double lineWidth = 1.0;

    /**
     * A boolean flag for visibility of fill elements.
     */
    boolean fillVisible = true;
    
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
        this.markerVisible = refStyle.markerVisible;
        this.markerType = refStyle.markerType;
        this.markerColor = refStyle.markerColor;
        this.markerSize = refStyle.markerSize;
        
        this.lineVisible = refStyle.lineVisible;
    	this.lineColor = refStyle.lineColor;
    	this.lineWidth = refStyle.lineWidth;
    	
        this.fillVisible = refStyle.fillVisible;
        this.fillColor = refStyle.fillColor;
        this.fillOpacity = refStyle.fillOpacity;
    }
    
    
    // ===================================================================
    // Accessors and mutators for markers
    
    /**
     * @return the markerVisible
     */
    public boolean isMarkerVisible()
    {
        return markerVisible;
    }

    /**
     * @param markerVisible the markerVisible to set
     */
    public Style setMarkerVisible(boolean markerVisible)
    {
        this.markerVisible = markerVisible;
        return this;
    }

    /**
     * @return the markerType
     */
    public MarkerType getMarkerType()
    {
        return markerType;
    }

    /**
     * @param markerType the markerType to set
     */
    public Style setMarkerType(MarkerType markerType)
    {
        this.markerType = markerType;
        return this;
    }

    /**
     * @return the markerColor
     */
    public Color getMarkerColor()
    {
        return markerColor;
    }

    /**
     * @param markerColor the markerColor to set
     */
    public Style setMarkerColor(Color markerColor)
    {
        this.markerColor = markerColor;
        return this;
    }

    /**
     * @return the markerSize
     */
    public int getMarkerSize()
    {
        return markerSize;
    }

    /**
     * @param markerSize the markerSize to set
     */
    public Style setMarkerSize(int markerSize)
    {
        this.markerSize = markerSize;
        return this;
    }
    

    // ===================================================================
    // Accessors and mutators for lines
    
    /**
     * @return the lineVisible
     */
    public boolean isLineVisible()
    {
        return lineVisible;
    }

    /**
     * @param lineVisible the lineVisible to set
     */
    public Style setLineVisible(boolean lineVisible)
    {
        this.lineVisible = lineVisible;
        return this;
    }

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

    
    // ===================================================================
    // Accessors and mutators for fill
    
    /**
     * @return the fillVisible
     */
    public boolean isFillVisible()
    {
        return fillVisible;
    }

    /**
     * @param fillVisible the fillVisible to set
     */
    public Style setFillVisible(boolean fillVisible)
    {
        this.fillVisible = fillVisible;
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
