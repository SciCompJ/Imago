/**
 * 
 */
package imago.gui;

/**
 * Plugin for adding functionalities to Imago.
 * 
 * @author dlegland
 *
 */
public interface FramePlugin
{
    /**
     * Run the plugin from the specified frame.
     * 
     * @param frame the current frame.
     * @param args an optional string containing options for the plugin  
     */
    public void run(ImagoFrame frame, String args);
    
    /**
     * Defines whether this plugin should be enabled for the given frame.
     * Default is true.
     * 
     * @param frame the parent frame 
     * @return the default enable state
     */
    public default boolean isEnabled(ImagoFrame frame)
    {
        return true;
    }
}
