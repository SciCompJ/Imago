/**
 * 
 */
package imago.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import imago.image.ImageFrame;
import imago.image.plugins.ImageOperatorPlugin;

/**
 * Used to run a given plugin after selection of a menu entry within a frame.
 * 
 * @author dlegland
 *
 */
public class PluginRunner implements ActionListener
{
    // ===================================================================
    // Class variables

    ImagoFrame frame;

    /**
     * The plugin to run when an action is performed.
     */
    FramePlugin plugin;
    
    String optionsString = null;
    
    
    // ===================================================================
    // Constructors

    /**
     * Simple constructor.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     * @param plugin
     *            the plugin to run
     */
    public PluginRunner(ImagoFrame frame, FramePlugin plugin)
    {
        this.frame = frame;
        this.plugin = plugin;
    }

    /**
     * Constructor that also specifies options string.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     * @param plugin
     *            the plugin to run
     * @param optionsString
     *            a string containing the list of options given to the plugin.
     *            Options are provided as name-value pairs, and are separated
     *            with comas.
     */
    public PluginRunner(ImagoFrame frame, FramePlugin plugin, String optionsString)
    {
        this.frame = frame;
        this.plugin = plugin;
        this.optionsString = optionsString;
    }

    
    // ===================================================================
    // Implementation of the ActionListener interface

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        Thread t = new Thread()
        {
            public void run()
            {
                // log plugin start
                String pluginName = plugin.getClass().getCanonicalName();
                
                // add some plugin-class-specific processing to track process
                if (frame instanceof ImageFrame)
                {
                    String imageName = ((ImageFrame) frame).getImageHandle().getName();
                    if (plugin instanceof ImageOperatorPlugin plugin2)
                    {
                        // specific processing for "ImageOperator" plugins
                        String opName = plugin2.operator().getClass().getCanonicalName();
                        String pattern = "run Image Operator Plugin \"%s\" on image \"%s\"";
                        System.out.println(String.format(pattern, opName, imageName));
                    }
                    else
                    {
                        // process generic frame plugin
                        String pattern = "run FramePlugin \"%s\" on image \"%s\"";
                        System.out.println(String.format(pattern, pluginName, imageName));
                    }
                }
                else
                {
                    // process generic frame plugin
                    String pattern = "run FramePlugin \"%s\"";
                    System.out.println(String.format(pattern, pluginName));
                }
                
                // run the plugin
                try 
                {
                    plugin.run(frame, optionsString);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace(System.err);
                    ImagoGui.showExceptionDialog(frame, ex, plugin.getClass().getSimpleName() + " Plugin Error");
                }
            }
        };
        
        t.start();
    }
}
