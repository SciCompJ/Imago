/**
 * 
 */
package imago.scene;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class SceneTest
{

    /**
     * Test method for {@link imago.scene.Scene#Scene()}.
     */
    @Test
    public final void testScene()
    {
        // Create a 2D scene
        Scene scene = new Scene(2);
        
        // extent in each dimension should equals 1
        assertEquals(1.0, scene.getExtent(0), .01);
        assertEquals(1.0, scene.getExtent(1), .01);
    }

}
