/**
 * 
 */
package imago.image.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import net.sci.array.color.ColorMaps;
import net.sci.array.numeric.Float32Array2D;

/**
 * 
 */
class DefaultIntensityImageRendererTest
{
    /**
     * Test method for {@link imago.image.render.IndexedColorMapImageRenderer#render(net.sci.array.Array)}.
     */
    @Test
    final void test_render()
    {
        Float32Array2D array = Float32Array2D.fromFloatArray(new float[][] { 
            { 0.0f, 0.0f, 263f, 263f, 263f }, 
            { 0.0f, 0.0f, 263f, 263f, 263f },
            { 263f, 263f, 0.0f, 0.0f, 0.0f }, 
            { 263f, 263f, 0.0f, 0.0f, 0.0f } });
        IndexedColorMapImageRenderer renderer = new DefaultIntensityImageRenderer();

        BufferedImage res = renderer.render(array);

        assertEquals(5, res.getWidth());
        assertEquals(4, res.getHeight());
        // black for background, white for foreground
        int BG = Color.BLACK.getRGB();
        int FG = Color.WHITE.getRGB();
        assertEquals(BG, res.getRGB(0, 0));
        assertEquals(FG, res.getRGB(4, 0));
        assertEquals(FG, res.getRGB(0, 3));
        assertEquals(BG, res.getRGB(4, 3));
    }

    /**
     * Test method for {@link imago.image.render.IndexedColorMapImageRenderer#render(net.sci.array.Array)}.
     */
    @Test
    final void test_render_setColorMap()
    {
        Float32Array2D array = Float32Array2D.fromFloatArray(new float[][] { 
            { 0.0f, 0.0f, 263f, 263f, 263f }, 
            { 0.0f, 0.0f, 263f, 263f, 263f },
            { 263f, 263f, 0.0f, 0.0f, 0.0f }, 
            { 263f, 263f, 0.0f, 0.0f, 0.0f } });
        IndexedColorMapImageRenderer renderer = new DefaultIntensityImageRenderer()
                .setColorMap(ColorMaps.JET.createColorMap(256));

        BufferedImage res = renderer.render(array);

        assertEquals(5, res.getWidth());
        assertEquals(4, res.getHeight());
        // dark blue for background, dark red for foreground
        int BG = new Color(0, 0, 127).getRGB();
        int FG = new Color(131, 0, 0).getRGB();
        assertEquals(BG, res.getRGB(0, 0));
        assertEquals(FG, res.getRGB(4, 0));
        assertEquals(FG, res.getRGB(0, 3));
        assertEquals(BG, res.getRGB(4, 3));
    }

    /**
     * Test method for {@link imago.image.render.IndexedColorMapImageRenderer#render(net.sci.array.Array)}.
     */
    @Test
    final void test_render_setDisplayRange_setColorMap()
    {
        Float32Array2D array = Float32Array2D.create(100, 100);
        array.fillValues((x,y) -> (double) x + y);
        IndexedColorMapImageRenderer renderer = new DefaultIntensityImageRenderer()
                .setDisplayRange(new double[] {50, 150})
                .setColorMap(ColorMaps.JET.createColorMap(256));

        BufferedImage res = renderer.render(array);

        assertEquals(100, res.getWidth());
        assertEquals(100, res.getHeight());
        
        // dark blue for background, dark red for foreground
        int BG = new Color(0, 0, 127).getRGB();
        int FG = new Color(131, 0, 0).getRGB();
        
        assertEquals(BG, res.getRGB( 0,  0));
        assertEquals(FG, res.getRGB(99, 99));
    }
}
