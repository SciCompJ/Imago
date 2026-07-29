/**
 * 
 */
package imago.developer.plugins;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.PluginHandler;
import imago.gui.PluginManager;

/**
 * 
 */
public class PrintUserPluginList implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public PrintUserPluginList()
    {
    }

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        PluginManager mgr = frame.getGui().getPluginManager();
        String pattern = "  %-35s %-30s %s\n";
        
        System.out.println("List of user plugins:");
        System.out.printf(pattern, "Name", "Path", "Class");
       
        for (PluginHandler handle : mgr.pluginHandlers())
        {
            FramePlugin plugin = handle.getPlugin();
            if (plugin != null)
            {
                String name = handle.getName();
                String path = handle.getMenuPath();
                String className = plugin.getClass().getName();
                
                System.out.printf(pattern, name, path, className);
                
            }
        }
    }

}
