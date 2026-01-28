/**
 * 
 */
package imago.developer.plugins;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;

/**
 * Utility class used to check that Imago application does not crash even if it
 * tries to load a plugin that can not be instantiated.
 */
public class FailingConstructorPlugin implements FramePlugin
{
    /**
     * Default empty constructor, that throws an exception, making the plugin
     * potentially problematic when creating a new frame.
     */
    public FailingConstructorPlugin()
    {
        throw new RuntimeException("Custom Constructor error");
    }
    
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // empty body
    }
}
