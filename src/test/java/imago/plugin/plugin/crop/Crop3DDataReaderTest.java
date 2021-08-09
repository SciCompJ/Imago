/**
 * 
 */
package imago.plugin.plugin.crop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class Crop3DDataReaderTest
{

    /**
     * Test method for {@link imago.plugin.plugin.crop.Crop3DDataReader#readCrop3DData()}.
     * @throws IOException 
     */
    @Test
    public final void testReadCrop3DData() throws IOException
    {
        String fileName = getClass().getResource("/json/H_250_2_MD_crop3.crop3d").getFile();
        File file = new File(fileName);
        assertTrue(file.exists());
        assertTrue(file.canRead());
        
        Crop3DDataReader reader = new Crop3DDataReader(file);
        Crop3DData data = reader.readCrop3DData();
        
        assertNotNull(data);
        assertNotNull(data.imageInfo);
        assertNotNull(data.regions);
        
        assertEquals(data.regions.size(), 1);
    }

}
