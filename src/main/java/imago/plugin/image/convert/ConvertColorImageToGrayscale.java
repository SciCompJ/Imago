/**
 * 
 */
package imago.plugin.image.convert;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.color.RGB16;
import net.sci.array.color.RGB16Array;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.UInt16;
import net.sci.array.numeric.UInt16Array;
import net.sci.array.numeric.UInt8Array;
import net.sci.image.Image;
import net.sci.image.ImageType;


/**
 * Converts the current color image into a grayscale image, using a data type
 * according to the color data type.
 * 
 * @author David Legland
 */
public class ConvertColorImageToGrayscale implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ConvertColorImageToGrayscale()
    {
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        if (image == null)
        {
            return;
        }
        
        // retrieve data
        Array<?> array = image.getData();
        if (array == null)
        {
            return;
        }
        
        // dispatch processing depending on input image data type
        Image resultImage;
        if (array.elementClass() == RGB8.class)
        {
            // Default behavior for binary: create a view
            UInt8Array result = RGB8Array.wrap(array).createUInt8View();
            resultImage = new Image(result, image);
            resultImage.getDisplaySettings().setDisplayRange(new double[] { 0, 255 });
        }
        else if (array.elementClass() == RGB16.class)
        {
            UInt16Array result = RGB16Array.wrap(array).createUInt16View();
            resultImage = new Image(result, image);
            resultImage.getDisplaySettings().setDisplayRange(new double[] { 0, UInt16.MAX_INT });
        }
        else
        {
            ImagoGui.showErrorDialog(frame, "Requires a color image", "Data Type Error");
            return;
        }
        
        // force type and display range of result image
        resultImage.setType(ImageType.GRAYSCALE);
        
        // add the image document to GUI
        resultImage.setName(image.getName() + "-gray");
        ImageFrame.create(resultImage, frame);
    }
    
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame type
        if (!(frame instanceof ImageFrame)) return false;
        
        // retrieve image
        Image image = ((ImageFrame) frame).getImageHandle().getImage();
        if (image == null) return false;
        
        // retrieve data
        Array<?> array = image.getData();
        return array.elementClass() == RGB8.class || array.elementClass() == RGB16.class;
    }
}
