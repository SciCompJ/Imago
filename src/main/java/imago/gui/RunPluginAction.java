/**
 * 
 */
package imago.gui;

import imago.image.ImageFrame;
import imago.image.plugin.ImageOperatorPlugin;
import net.sci.image.ImageOperator;

import java.awt.event.ActionEvent;

/**
 * Use to run a given plugin after selection of a menu entry.
 * 
 * @author dlegland
 *
 */
public class RunPluginAction extends ImagoAction
{
    /**
     * the serial version ID. 
     */
    private static final long serialVersionUID = 1L;
    
    // ===================================================================
    // Class variables

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
    public RunPluginAction(ImagoFrame frame, FramePlugin plugin)
    {
        super(frame, "");
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
    public RunPluginAction(ImagoFrame frame, FramePlugin plugin, String optionsString)
    {
        super(frame, "");
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
                    if (plugin instanceof ImageOperatorPlugin)
                    {
                        // specific processing for "ImageOperator" plugins
                        ImageOperator operator = ((ImageOperatorPlugin) plugin).operator();
                        String opName = operator.getClass().getCanonicalName();
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
