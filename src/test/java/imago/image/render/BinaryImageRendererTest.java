/**
 * 
 */
package imago.image.render;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import net.sci.array.binary.BinaryArray2D;
import net.sci.array.color.ColorMaps;

/**
 * 
 */
class BinaryImageRendererTest
{
    /**
     * Test method for {@link imago.image.render.BinaryImageRenderer#render(net.sci.array.Array)}.
     */
    @Test
    void test_render_simple5x4()
    {
        BinaryArray2D array = BinaryArray2D.of(new boolean[][] { 
                    { false, false, true, true, true}, 
                    { false, false, true, true, true },
                    { true, true, false, false, false }, 
                    { true, true, false, false, false } });
        BinaryImageRenderer renderer = new BinaryImageRenderer();
        
        BufferedImage res = renderer.render(array);
        
        assertEquals(5, res.getWidth());
        assertEquals(4, res.getHeight());
        // white for background, red for foreground
        assertEquals(0xFFFFFFFF, res.getRGB(0, 0));
        assertEquals(0xFFFF0000, res.getRGB(4, 0));
        assertEquals(0xFFFF0000, res.getRGB(0, 3));
        assertEquals(0xFFFFFFFF, res.getRGB(4, 3));
    }

    /**
     * Test method for {@link imago.image.render.BinaryImageRenderer#render(net.sci.array.Array)}.
     */
    @Test
    void test_render_simple5x4_colormapGray()
    {
        BinaryArray2D array = BinaryArray2D.of(new boolean[][] { 
                    { false, false, true, true, true}, 
                    { false, false, true, true, true },
                    { true, true, false, false, false }, 
                    { true, true, false, false, false } });
        BinaryImageRenderer renderer = new BinaryImageRenderer()
                .setColorMap(ColorMaps.GRAY.createColorMap(256));
        
        BufferedImage res = renderer.render(array);
        
        assertEquals(5, res.getWidth());
        assertEquals(4, res.getHeight());
        // black for background, white for foreground
        assertEquals(0xFF000000, res.getRGB(0, 0));
        assertEquals(0xFFFFFFFF, res.getRGB(4, 0));
        assertEquals(0xFFFFFFFF, res.getRGB(0, 3));
        assertEquals(0xFF000000, res.getRGB(4, 3));
    }

}
