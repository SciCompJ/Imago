/**
 * 
 */
package imago.image;

import org.junit.Test;

import imago.image.ImageFrame;
import net.sci.array.numeric.UInt8Array2D;
import net.sci.image.Image;
import net.sci.image.ImageType;

/**
 * @author dlegland
 *
 */
public class ImageFrameTest
{

    /**
     * Test method for {@link imago.gui.ImagoFrame#setVisible(boolean)}.
     */
    @Test
    public void testDisplayLabelImage()
    {
        // create an image with 16 labels
        UInt8Array2D array = UInt8Array2D.create(200, 200);
        for (int y = 0; y < 50; y++)
        {
            for (int x = 0; x < 50; x++)
            {
                array.setInt(    x,     y,  0);
                array.setInt( x+50,     y,  1);
                array.setInt(x+100,     y,  2);
                array.setInt(x+150,     y,  3);
                array.setInt(    x,  y+50,  4);
                array.setInt( x+50,  y+50,  5);
                array.setInt(x+100,  y+50,  6);
                array.setInt(x+150,  y+50,  7);
                array.setInt(    x, y+100,  8);
                array.setInt( x+50, y+100,  9);
                array.setInt(x+100, y+100, 10);
                array.setInt(x+150, y+100, 11);
                array.setInt(    x, y+150, 12);
                array.setInt( x+50, y+150, 13);
                array.setInt(x+100, y+150, 14);
                array.setInt(x+150, y+150, 15);
            }
        }
        Image image = new Image(array, ImageType.LABEL);
        image.setName("Label Image");
        
        ImageFrame frame = ImageFrame.create(image, null);
        frame.close();
    }

}
