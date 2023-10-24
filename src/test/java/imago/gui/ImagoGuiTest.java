/**
 * 
 */
package imago.gui;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import imago.app.ImagoApp;

/**
 * @author dlegland
 *
 */
public class ImagoGuiTest
{
    /**
     * Test method for {@link imago.gui.ImagoGui#createImageFrame(net.sci.image.Image)}.
     */
    @Test
    public void test_create()
    {
        ImagoApp app = new ImagoApp();
        ImagoGui gui = new ImagoGui(app);
        assertNotNull(gui);
    }
    
}
