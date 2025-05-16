/**
 * 
 */
package imago.util.imagej;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import imago.app.shape.Shape;
import imago.app.shape.Style;
import net.sci.geom.geom2d.Geometry2D;

/**
 * Encapsulates information from decoding ImageJ Roi. Used as intermediary for
 * creating Imago Shape instances.
 */
public class ImagejRoi
{
    // ===================================================================
    // Constants

    public static final String[] POINT_SIZES = {"Tiny", "Small", "Medium", "Large", "Extra Large", "XXL", "XXXL"};
    public static final String[] POINT_TYPES = {"Hybrid", "Cross", "Dot", "Circle"};

    /** List of sizes for point ROI */
    private static final int TINY=1, SMALL=3, MEDIUM=5, LARGE=7, EXTRA_LARGE=11, XXL=17, XXXL=25;
    
    public enum SubType
    {
        UNKNOWN(0),
        TEXT(1),
        ARROW(2),
        ELLIPSE(3),
        IMAGE(4),
        ROTATED_RECT(5);
        
        int value;
        
        private SubType(int value)
        {
            this.value = value;
        }
        
        public int value()
        {
            return this.value;
        }
        
        public static final SubType fromValue(int value)
        {
            return switch(value)
            {
                case 0 -> UNKNOWN;
                case 1 -> TEXT;
                case 2 -> ARROW;
                case 3 -> ELLIPSE;
                case 4 -> IMAGE;
                case 5 -> ROTATED_RECT;
                default -> throw new RuntimeException("Could not identify subtype from value: " + value);
            };
        }
    }
    
    // ===================================================================
    // Class variables

    Geometry2D geometry;
    
    String name;
    
    
    Color strokeColor = Color.BLUE;
    
    double strokeWidth = 1.0;
    
    boolean scaleStrokeWidth = true;
    
    Color fillColor = Color.YELLOW;
    
    /** the point type (0=hybrid, 1=cross, 2=dot, 3=circle)*/
    int pointType = 0;
    
    /** The size of point ROI, in pixels */
    int pointSize = SMALL;
    
    
    Properties props;
    
    
    // ===================================================================
    // Constructors

    public ImagejRoi(Geometry2D geometry)
    {
        this.geometry = geometry;
    }
    
    
    // ===================================================================
    // General methods

    /**
     * Converts this data structure into a Shape instance that can be used
     * within Imago.
     * 
     * @return the result of conversion into a shape.
     */
    public Shape asShape()
    {
        Style style = new Style();
        style.setLineColor(strokeColor);
        style.setLineWidth(strokeWidth);
        style.setFillColor(fillColor);
        
        return new Shape(this.geometry, style);
    }
    
    
    // ===================================================================
    // getters and setters

    public void setName(String name)
    {
        this.name = name;
    }
        
    /** Sets the point type (0=hybrid, 1=cross, 2=dot, 3=circle). */
    public void setPointType(int type)
    {
        this.pointType = type;
    }
    
    /** Sets the point size, where 'size' is 0-6 (Tiny-XXXL). */
    public void setPointSizeIndex(int size) 
    {
        if (size >= 0 && size < POINT_SIZES.length) 
        {
            this.pointSize = convertIndexToSize(size);
        }
    }
    
    private static int convertIndexToSize(int index)
    {
        return switch (index)
        {
            case 0 -> TINY;
            case 1 -> SMALL;
            case 2 -> MEDIUM;
            case 3 -> LARGE;
            case 4 -> EXTRA_LARGE;
            case 5 -> XXL;
            case 6 -> XXXL;
            default -> SMALL; 
        };
    }
    
    public void setStrokeColor(Color c)
    {
        this.strokeColor = c;
    }
    
    public void setStrokeWidth(double width)
    {
        this.strokeWidth = width;
    }
    
    public void setFillColor(Color c)
    {
        this.fillColor = c;
    }
    
    public void setProperties(String properties) {
        if (props == null)
            props = new Properties();
        else
            props.clear();
        
        try (InputStream is = new ByteArrayInputStream(properties.getBytes("utf-8")))
        {
            props.load(is);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

}
