/**
 * 
 */
package imago.plugin.developer;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;

/**
 * Utility class used to check that Imago application does not crash even if it
 * tries to run a plugin that throws an exception.
 */
public class RunThrowExceptionPlugin implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String args)
    {
        throw new RuntimeException("Custom run() error");
    }
}
