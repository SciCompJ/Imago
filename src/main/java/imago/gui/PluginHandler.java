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

    FramePlugin plugin;
    
    String name;

    String menuPath = "";
    
    /**
     * 
     */
    public PluginHandler(FramePlugin plugin, String name)
    {
        this.plugin = plugin;
        this.name = name;
    }
    
    public PluginHandler(FramePlugin plugin, String name, String menuPath)
    {
        this.plugin = plugin;
        this.name = name;
        this.menuPath = menuPath;
    }

    public FramePlugin getPlugin()
    {
        return plugin;
    }

    public String getName()
    {
        return name;
    }

    public String getMenuPath()
    {
        return menuPath;
    }
}
