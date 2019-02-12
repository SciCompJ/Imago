/**
 * 
 */
package imago.plugin.developer;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;

/**
 * @author dlegland
 *
 */
public class DisplayExceptionDialog implements Plugin
{
    
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        ImagoGui.showExceptionDialog(frame, new RuntimeException("I'm a sample exception"), "Display Sample Exception");
    }
    
}
