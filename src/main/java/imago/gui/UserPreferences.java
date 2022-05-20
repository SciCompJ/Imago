/**
 * 
 */
package imago.gui;

/**
 * Global settings for GUI interactions, such as brush size or color.
 * 
 * @author dlegland
 *
 */
public class UserPreferences
{
    /**
     * The value of the brush for intensity images.
     * Default is 255, corresponding to white for UInt8 images.
     */
    public double brushValue = 255;
    
    /**
     * The radius of the brush.
     */
    public double brushRadius = 2.0;
    
}
