/**
 * 
 */
package imago.image.plugin.convert;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.color.ColorMap;
import net.sci.array.color.ColorMaps;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.IntArray;
import net.sci.image.Image;

/**
 * Convert a label image to color image based on RGB8 using current LUT and
 * background color.
 * 
 * @author dlegland
 *
 */
public class ConvertLabelMapToRGB8 implements FramePlugin
{
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        
        if (image == null)
        {
            return;
        }
        if (!image.isLabelImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires a label image as input", "Data Type Error");
            return;
        }
        
        Array<?> array = image.getData();
        if (array == null)
        {
            return;
        }
        if (!(array instanceof IntArray))
        {
            throw new RuntimeException("Label images assume inner array implements IntArray");
        }
        IntArray<?> intArray = (IntArray<?>) array;

        // extract LUT from image, or create one otherwise
        ColorMap lut = image.getDisplaySettings().getColorMap();
        if (lut == null)
        {
            lut = ColorMaps.GLASBEY.createColorMap(255);
        }
        int nLabels = lut.size();
        
        // retrieve also color for background
        RGB8 bgColor = RGB8.fromColor(image.getDisplaySettings().getBackgroundColor());
        
        // Create the AWT image
        RGB8Array rgb = RGB8Array.create(array.size());
        
        for (int[] pos : rgb.positions())
        {
            int index = intArray.getInt(pos);
            if (index == 0)
            {
                rgb.set(pos, bgColor);
            }
            else
            {
                index = ((index - 1) % nLabels);
                rgb.set(pos, RGB8.fromColor(lut.getColor(index)));
            }
        }

        // create new image, propagating spatial calibration
        Image rgbImage = new Image(rgb, image);
                
        // add the image document to GUI
        ImageFrame.create(rgbImage, frame);
    }
}
