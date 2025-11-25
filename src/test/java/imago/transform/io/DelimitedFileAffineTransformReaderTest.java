/**
 * 
 */
package imago.transform.io;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import net.sci.geom.Transform;
import net.sci.geom.geom3d.AffineTransform3D;

/**
 * 
 */
class DelimitedFileAffineTransformReaderTest
{
    
    /**
     * Test method for {@link imago.transform.io.DelimitedFileAffineTransformReader#readTransform(java.io.Reader)}.
     * @throws IOException 
     */
    @Test
    final void testReadTransform() throws IOException
    {
        String fileName = getClass().getResource("/transforms/L_100_1_align_transfo.txt").getFile();
        
        File file = new File(fileName);
        DelimitedFileAffineTransformReader reader = new DelimitedFileAffineTransformReader(file);

        Transform transfo = reader.readTransform();

        assertNotNull(transfo);
        assertInstanceOf(AffineTransform3D.class, transfo);
    }
    
}
