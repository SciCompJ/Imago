/**
 * 
 */
package imago.gui;

/**
 * Encapsulates a plugin and the information necessary to integrate it into the GUI.
 * 
 * @see Plugin
 * 
 * @author dlegland
 *
 */
public class PluginHandler
{

    Plugin plugin;
    
    String name;
    
    /**
     * 
     */
    public PluginHandler(Plugin plugin, String name)
    {
        this.plugin = plugin;
        this.name = name;
    }
    
    public Plugin getPlugin()
    {
        return plugin;
    }

    public String getName()
    {
        return name;
    }
}
