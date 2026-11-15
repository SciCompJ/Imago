/**
 * 
 */
package imago.app;

import net.sci.array.color.ColorMap;

/**
 * A handle to colormap.
 */
public class ColorMapHandle extends ObjectHandle
{
    // =============================================================
    // class member
    
    ColorMap colorMap;
    
    
    // =============================================================
    // constructor
    
    public ColorMapHandle(String tag, ColorMap colorMap, String name)
    {
        super(tag);
        this.colorMap = colorMap;
        this.name = name;
    }
    

    // =============================================================
    // Implementation of Handle methods
    
    @Override
    public ColorMap getObject()
    {
        return colorMap;
    }

}
