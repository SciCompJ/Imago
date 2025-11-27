/**
 * 
 */
package imago.transform.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import net.sci.geom.Transform;
import net.sci.geom.geom2d.AffineTransform2D;

/**
 * 
 */
class JsonTransformReaderTest
{
    
    /**
     * Test method for {@link imago.transform.io.JsonTransformReader#readTransform()}.
     * @throws IOException 
     */
    @Test
    final void testReadTransform() throws IOException
    {
        String fileName = getClass().getResource("/transforms/translation2d.json").getFile();
        File file = new File(fileName);
        
        JsonTransformReader transformReader = new JsonTransformReader(new FileReader(file));

        Transform transfo = transformReader.readTransform();
        
        transformReader.close();

        assertNotNull(transfo);
        assertInstanceOf(AffineTransform2D.class, transfo);
        double[][] mat = ((AffineTransform2D) transfo).affineMatrix();
        assertEquals( 1.0, mat[0][0], 0.01);
        assertEquals( 0.0, mat[0][1], 0.01);
        assertEquals(-5.0, mat[0][2], 0.01);
        assertEquals( 0.0, mat[1][0], 0.01);
        assertEquals( 1.0, mat[1][1], 0.01);
        assertEquals(10.0, mat[1][2], 0.01);
    }
    
}
