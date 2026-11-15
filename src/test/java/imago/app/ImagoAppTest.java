package imago.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import imago.image.ImageHandle;
import net.sci.array.Array2D;
import net.sci.array.color.ColorMap;
import net.sci.array.color.ColorMaps;
import net.sci.array.numeric.UInt8Array2D;
import net.sci.image.Image;

public class ImagoAppTest
{
    @Test
    public void test_addHandle()
    {
        ImagoApp app = new ImagoApp();

        Array2D<?> array = UInt8Array2D.create(320, 200);
        Image image = new Image(array);

        ImageHandle.create(app, image);

        assertEquals(1, ImageHandle.getAll(app).size());
    }

    /**
     * Test method for {@link imago.app.ImagoApp#getColorMap(java.lang.String)}.
     */
    @Test
    public final void test_getColorMap()
    {
        ImagoApp app = new ImagoApp();
        
        app.addColorMap("Red", ColorMaps.RED.createColorMap(256));
        app.addColorMap("Jet", ColorMaps.JET.createColorMap(256));
        app.addColorMap("Fire", ColorMaps.FIRE.createColorMap(256));
        
        ColorMap jet256 = ColorMaps.JET.createColorMap(256);
        
        ColorMap cmap = app.getColorMap("Jet");
        assertNotNull(cmap);
        assertEquals(jet256.getColor(0), cmap.getColor(0));
        assertEquals(jet256.getColor(50), cmap.getColor(50));
        assertEquals(jet256.getColor(255), cmap.getColor(255));
    }
}
