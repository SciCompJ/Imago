/**
 * 
 */
package imago.gui;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Utility class for creating menu items of the various frames of the Imago
 * application. Provides also some methods for generating menus shared by
 * several frames.
 */
public class FrameMenuBuilder
{
    // ===================================================================
    // class members
    
    /** 
     * The frame to setup.
     */
    protected ImagoFrame frame;
    
    static Icon emptyIcon;
    static 
    {
        int width = 16;
        int height = 16;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                image.setRGB(x, y, 0x00FFFFFF);
            }
        }
        emptyIcon = new ImageIcon(image);
    }
    

    // ===================================================================
    // Constructor
    
    /**
     * Creates a builder for the specified frame.
     * 
     * @param frame
     *            the frame to build.
     */
    public FrameMenuBuilder(ImagoFrame frame)
    {
        this.frame = frame;
    }
    
    
    // ===================================================================
    // menu creation methods
    
    public void addSharedMenus(JMenuBar menuBar)
    {
        menuBar.add(createPluginsMenu());
        menuBar.add(createHelpMenu());
    }
    
    /**
     * Creates the sub-menu for the "Plugins" item in the main menu bar, shared
     * by several frame types.
     */
    public JMenu createPluginsMenu()
    {
        JMenu menu = new JMenu("Plugins");
        
        JMenu devMenu = new JMenu("Developer");
        addPlugin(devMenu, imago.developer.plugins.DisplayExceptionDialog.class, "Show Demo Exception");
        // The two following plugins are used for debugging
//        addPlugin(devMenu, imago.plugin.developer.FailingConstructorPlugin.class, "(X) Can not Instantiate");
//        addPlugin(devMenu, imago.plugin.developer.RunThrowExceptionPlugin.class, "(X) Can not Run");
        devMenu.addSeparator();
        addPlugin(devMenu, imago.developer.plugins.PrintFrameList.class, "Print Frame List");
        addPlugin(devMenu, imago.developer.plugins.PrintDocumentList.class, "Print Document List");
        addPlugin(devMenu, imago.developer.plugins.PrintWorkspaceContent.class, "Print Workspace Content");
        menu.add(devMenu);
        menu.addSeparator();

        // Add some domain-specific plugins, to be transformed into user plugins in the future
        JMenu perigrainMenu = new JMenu("Perigrain");
        addPlugin(perigrainMenu, imago.gui.plugins.plugins.crop.Crop3DPlugin.class, "Crop 3D");
        addPlugin(perigrainMenu, imago.gui.plugins.plugins.crop.CreateSurface3DPlugin.class, "Surface 3D");
        addPlugin(perigrainMenu, imago.gui.plugins.plugins.ImportImage3DPolylineSeries.class, "Import Polyline Series");
        menu.add(perigrainMenu);
        
        // Add the user plugins
        if (!frame.gui.getPluginManager().pluginHandlers.isEmpty())
        {
            menu.addSeparator();
            for (PluginHandler handler : frame.gui.getPluginManager().pluginHandlers)
            {
                addPlugin(menu, handler);
            }
        }
        
        return menu;
    }
    
    public JMenu createHelpMenu()
    {
        JMenu menu = new JMenu("Help");
        addMenuItem(menu, "About...", null, true);
        return menu;
    }
    
    
    // ===================================================================
    // User Plugins management
    
    public void addPlugin(JMenu menu, PluginHandler handler)
    {
        // If menu path is specified, retrieve or create the hierarchy of menus
        String menuPath = handler.getMenuPath();
        if (!menuPath.isEmpty())
        {
            // determine menu text hierarchy
            String[] tokens = menuPath.split(">");
            
            // remove the first item ("Plugins")
            int ntokens = tokens.length;
            String[] tokens2 = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, tokens2, 0, ntokens - 1);
            
            // retrieve correct menu
            for (String name : tokens2)
            {
                menu = getSubMenu(menu, name);
            }
        }
        
        FramePlugin plugin = handler.getPlugin();
        addPlugin(menu, handler.getPlugin(), handler.getName(), plugin.isEnabled(frame));
    }
    
    private JMenu getSubMenu(JMenu baseMenu, String subMenuName)
    {
        for (Component sub : baseMenu.getMenuComponents())
        {
            if (sub instanceof JMenu menu)
            {
                if (menu.getText().equals(subMenuName))
                {
                    return menu;
                }
            }
        }
        
        // create a new sub-menu
        JMenu subMenu = new JMenu(subMenuName);
        baseMenu.add(subMenu);
        return subMenu;
    }


    // ===================================================================
    // Plugin menu entry methods
    
    public JMenuItem addPlugin(JMenu menu, Class<? extends FramePlugin> itemClass, String optionsString, String label)
    {
        // retrieve plugin
        FramePlugin plugin = frame.gui.getPluginManager().retrievePlugin(itemClass);
        if (plugin == null) return null;
        
        // create action that will catch action events
        PluginRunner action = new PluginRunner(frame, plugin, optionsString);
        
        // setup menu item
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(action);
        item.setIcon(emptyIcon);
        item.setMargin(new Insets(0, 0, 0, 0));
        item.setEnabled(plugin.isEnabled(frame));
        menu.add(item);
        return item;
    }
    
    public JMenuItem addPlugin(JMenu menu, Class<? extends FramePlugin> pluginClass, String label)
    {
        FramePlugin plugin = frame.gui.getPluginManager().retrievePlugin(pluginClass);
        return addPlugin(menu, plugin, label, plugin.isEnabled(frame));
    }
    
    public JMenuItem addPlugin(JMenu menu, Class<? extends FramePlugin> pluginClass, String label, boolean isEnabled)
    {
        FramePlugin plugin = frame.gui.getPluginManager().retrievePlugin(pluginClass);
        return addPlugin(menu, plugin, label, isEnabled);
    }

    public JMenuItem addPlugin(JMenu menu, FramePlugin plugin, String label)
    {
        return addPlugin(menu, plugin, label, plugin.isEnabled(frame));
    }
    
    public JMenuItem addPlugin(JMenu menu, FramePlugin plugin, String label, boolean enabled)
    {
        if (plugin == null) return null;
        JMenuItem item = createPluginMenuItem(plugin, label);
        item.setEnabled(enabled);
        menu.add(item);
        return item;
    }
    
    private JMenuItem createPluginMenuItem(FramePlugin plugin, String label)
    {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(new PluginRunner(frame, plugin));
        item.setIcon(emptyIcon);
        item.setMargin(new Insets(0, 0, 0, 0));
        return item;
    }
    
    
    // ===================================================================
    // menu item creation methods
    
    private JMenuItem addMenuItem(JMenu menu, String label, ActionListener listener, boolean enabled)
    {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(listener);
        item.setIcon(emptyIcon);
        item.setEnabled(enabled);
        menu.add(item);
        return item;
    }
}
