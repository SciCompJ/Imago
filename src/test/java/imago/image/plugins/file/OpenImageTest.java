/**
 * 
 */
package imago.image.plugins.file;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;

/**
 * 
 */
class OpenImageTest
{

    @Test
    void test_run()
    {
        ImagoApp app = new ImagoApp();
        ImagoGui gui = new ImagoGui(app);
        
        FramePlugin plugin = gui.getPluginManager().retrievePlugin(OpenImage.class);
        ImagoFrame baseFrame = gui.getEmptyFrame();
        String optionsString = "fileName=images/grains.png";
        
        assertNotNull(plugin);
        plugin.run(baseFrame, optionsString);
        
        assertEquals(1, gui.getFrames().size());
    }
}
