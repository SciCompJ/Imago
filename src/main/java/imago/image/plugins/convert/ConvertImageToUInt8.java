/**
 * 
 */
package imago.image.plugins.convert;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.process.BinaryToUInt8;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.impl.ScalarArrayUInt8View;
import net.sci.image.Image;
import net.sci.image.ImageType;


/**
 * Converts an image (scalar or RGB) to an UInt8 encoded image. When possible,
 * the result image is a view on the original image data.
 * 
 * @author David Legland
 *
 */
public class ConvertImageToUInt8 implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ConvertImageToUInt8()
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
        UInt8Array res;
        try
        {
            res = convertArray(array, image);
        }
        catch (Exception ex)
        {
            ImagoGui.showErrorDialog(frame, "Requires a scalar or color image", "Data Type Error");
            return;
        }
        Image resultImage = new Image(res, image);
        
        // force type and display range of result image
        resultImage.setType(ImageType.GRAYSCALE);
        resultImage.getDisplaySettings().setDisplayRange(new double[] { 0, 255 });
        
        // add the image document to GUI
        resultImage.setName(image.getName() + "-uint8");
        ImageFrame.create(resultImage, frame);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private UInt8Array convertArray(Array<?> array, Image image)
    {
        // dispatch processing depending on input image data type
        return switch (array.sampleElement())
        {
            case Binary b -> new BinaryToUInt8.View(BinaryArray.wrap(array));
            case Scalar<?> s -> 
            {
                double[] range = image.getDisplaySettings().getDisplayRange();
                yield new ScalarArrayUInt8View(ScalarArray.wrap((Array<Scalar>) array), range[0], range[1]);
            }
            case RGB8 rgb -> (RGB8Array.wrap(array).createUInt8View());
            default -> throw new RuntimeException("Requires a scalar or color image");
        };
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
        return array instanceof ScalarArray || array instanceof RGB8Array;
    }
}
