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
     * Runs this plugin from the specified Imago frame.
     * 
     * Can be called with options:
     * {@snippet lang="java":
     * run(frame, null);
     * }
     * or with options:
     * {@snippet lang="java":
     * run(frame, "name=myName,value=3");
     * }
     * 
     * @param frame
     *            the current frame.
     * @param optionsString
     *            an optional String containing the list of options given to the
     *            plugin. Options are provided as name-value pairs, and are
     *            separated with comas.
     */
    public void run(ImagoFrame frame, String optionsString);
    
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
