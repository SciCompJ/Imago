/**
 * 
 */
package imago.gui.action;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import imago.plugin.image.ImageOperatorPlugin;
import net.sci.image.ImageOperator;
import imago.gui.FramePlugin;

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
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The plugin to run when an action is performed.
     */
    FramePlugin plugin;
    
    /**
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
                    plugin.run(frame, null);
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
