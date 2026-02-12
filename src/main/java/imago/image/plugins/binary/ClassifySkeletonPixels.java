/**
 * 
 */
package imago.image.plugins.binary;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.numeric.IntArray2D;
import net.sci.array.numeric.Int32Array2D;
import net.sci.array.numeric.impl.RunLengthInt32Array2D;
import net.sci.image.Image;
import net.sci.image.ImageType;

/**
 * Associates a label to each pixel of a binary image, depending on the local
 * topology. The different classes are chosen according to the number of
 * neighbors of the current pixel:
 * <ul>
 * <li>0 -> background pixel</li>
 * <li>1 -> extremity pixel, with one neighbor</li>
 * <li>2 -> edge pixel, with two neighbors</li>
 * <li>3 -> intersection pixel, with at least 3 neighbors.</li>
 * </ul>
 */
public class ClassifySkeletonPixels implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ImageFrame imageFrame = (ImageFrame) frame;

        // retrieve image data
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();
        if (!(array instanceof BinaryArray))
        {
            frame.showErrorDialog("Requires a binary image input", "Data Type Error");
            return;
        }

        // check image dimensionality
        int nd = array.dimensionality();
        if (nd != 2)
        {
            frame.showErrorDialog("Can only process 2D images", "Dimensionality Error");
            return;
        }
        
        IntArray2D<?> res = processBinary2d(BinaryArray2D.wrap(BinaryArray.wrap(array)));
        
        Image resImage = new Image(res, ImageType.LABEL, image);
        resImage.setName(image.getName() + "-type");
        
        // add the image document to GUI
        ImageFrame.create(resImage, frame);
    }
    
    public IntArray2D<?> processBinary2d(BinaryArray2D array)
    {
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        Int32Array2D res =  new RunLengthInt32Array2D(sizeX, sizeY);
        
        // iterate over pixels
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                if (array.getBoolean(x, y))
                {
                    int count = 0;
                    for (int y2 = Math.max(y-1, 0); y2 <= Math.min(y + 1, sizeY-1); y2++)
                    {
                        for (int x2 = Math.max(x-1, 0); x2 <= Math.min(x + 1, sizeX-1); x2++)
                        {
                            if (array.getBoolean(x2, y2))
                            {
                                count++;
                            }
                        }
                    }
                    
                    // remove one to get the number of neighbors
                    res.setInt(x, y, Math.min(count - 1, 3));
                }
            }
        }
        
        return res;
    }
}
