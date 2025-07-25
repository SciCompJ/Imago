/**
 * 
 */
package imago.gui;

import java.util.Map;
import java.util.TreeMap;

/**
 * Plugin for adding functionalities to Imago.
 * 
 * @author dlegland
 *
 */
public interface FramePlugin
{
    /**
     * Utility method that convert an options string, where options are
     * delimited with comas, and each option is specified as a name-value pair,
     * into a map.
     * 
     * Note that the map is insensitive to case of keys.
     * 
     * @param optionsString
     *            the string to convert
     * @return a Map representation of the options within the String
     */
    public static Map<String,String> parseOptionsString(String optionsString)
    {
        String[] options = optionsString.split(",");
        TreeMap<String,String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String option : options)
        {
            String[] tokens = option.split("=", 2);
            map.put(tokens[0].trim(), tokens[1].trim());
        }
        return map;   
    }
    
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
