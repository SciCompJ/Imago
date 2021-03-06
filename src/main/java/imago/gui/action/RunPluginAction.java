/**
 * 
 */
package imago.gui.action;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
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
