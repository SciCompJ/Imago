/**
 * 
 */
package imago.gui.plugins.file;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.plugins.file.OpenImage;

/**
 * 
 */
class CloseAllFramesTest
{
    @Test
    void test_run_singleFrame()
    {
        ImagoApp app = new ImagoApp();
        ImagoGui gui = new ImagoGui(app);
        
        FramePlugin openImagePlugin = gui.getPluginManager().retrievePlugin(OpenImage.class);
        ImagoFrame baseFrame = gui.getEmptyFrame();
        String optionsString = "fileName=images/grains.png";
        
        assertNotNull(openImagePlugin);
        openImagePlugin.run(baseFrame, optionsString);
        
        assertTrue(gui.getFrames().size() > 0);
        ImagoFrame frame = gui.getFrames().iterator().next();
        
        FramePlugin closeFramePlugin = gui.getPluginManager().retrievePlugin(CloseAllFrames.class);
        assertNotNull(closeFramePlugin);
        
        closeFramePlugin.run(frame, "");
        assertEquals(0, gui.getFrames().size());
    }

    @Test
    void test_run_twoFrames()
    {
        ImagoApp app = new ImagoApp();
        ImagoGui gui = new ImagoGui(app);
        
        FramePlugin openImagePlugin = gui.getPluginManager().retrievePlugin(OpenImage.class);
        assertNotNull(openImagePlugin);
        
        ImagoFrame baseFrame = gui.getEmptyFrame();
        openImagePlugin.run(baseFrame, "fileName=images/grains.png");
        openImagePlugin.run(baseFrame, "fileName=images/peppers.png");
        
        assertTrue(gui.getFrames().size() > 0);
        ImagoFrame frame = gui.getFrames().iterator().next();
        
        FramePlugin closeFramePlugin = gui.getPluginManager().retrievePlugin(CloseAllFrames.class);
        assertNotNull(closeFramePlugin);
        
        closeFramePlugin.run(frame, "");
        assertEquals(0, gui.getFrames().size());
    }

}
