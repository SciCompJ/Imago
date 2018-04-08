/**
 * 
 */
package imago.gui.action;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;

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
    
    Plugin plugin;
    
    /**
     * @param frame
     *            the frame from which the plugin will be called
     * @param plugin
     *            the plugin to run
     */
    public RunPluginAction(ImagoFrame frame, Plugin plugin)
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
                plugin.run(frame);
            }
        };
        t.start();
    }

}
