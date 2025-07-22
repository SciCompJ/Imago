/**
 * 
 */
package imago.gui.util;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;

/**
 * Utility class used to check that Imago application does not crash even if it
 * tries to load a plugin that can not be instantiated.
 */
public class FailingConstructorPlugin implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String args)
    {
        throw new RuntimeException("Custom error...");
    }
}
