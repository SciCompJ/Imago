/**
 * 
 */
package imago.util.imagej;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.jupiter.api.Test;

/**
 * 
 */
class ImagejRoiDecoderTest
{
    
    /**
     * Test method for {@link imago.util.imagej.ImagejRoiDecoder#getRoi()}.
     * @throws IOException 
     */
    @Test
    final void testGetRoi_rect() throws IOException
    {
        String fileName = getClass().getResource("/imagej/roi/ijroi_rect.roi").getFile();
        byte[] array = readByteArray(fileName);
        
        ImagejRoi roi = ImagejRoiDecoder.decode(array);
        
        assertNotNull(roi);
    }
    
    /**
     * Test method for {@link imago.util.imagej.ImagejRoiDecoder#getRoi()}.
     * @throws IOException 
     */
    @Test
    final void testGetRoi_poly() throws IOException
    {
        String fileName = getClass().getResource("/imagej/roi/ijroi_poly.roi").getFile();
        byte[] array = readByteArray(fileName);
        
        ImagejRoi roi = ImagejRoiDecoder.decode(array);
        
        assertNotNull(roi);
    }
    
    /**
     * Test method for {@link imago.util.imagej.ImagejRoiDecoder#getRoi()}.
     * @throws IOException 
     */
    @Test
    final void testGetRoi_elli() throws IOException
    {
        String fileName = getClass().getResource("/imagej/roi/ijroi_elli.roi").getFile();
        byte[] array = readByteArray(fileName);
        
        ImagejRoi roi = ImagejRoiDecoder.decode(array);
        
        assertNotNull(roi);
    }
    
    private byte[] readByteArray(String fileName) throws IOException
    {
        // read all bytes from the file
        File file = new File(fileName);
        int n = (int) file.length();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        byte[] array = new byte[n];
        raf.read(array);
        raf.close();
        return array;
    }
}
