/**
 * 
 */
package imago.image.plugins.convert;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.Float32Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.image.Image;

/**
 * Converts a scalar image to a new intensity image containing an array of
 * {@code Float32}.
 * 
 * @author David Legland
 *
 */
public class ConvertImageToFloat32 implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ConvertImageToFloat32()
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
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();

        if (image == null)
        { return; }
        Array<?> array = image.getData();
        if (array == null)
        { return; }
        if (!(array instanceof ScalarArray))
        { return; }

        Float32Array result = Float32Array.convert((ScalarArray<?>) array);
        Image resultImage = new Image(result, image);
        resultImage.setDisplaySettings(image.getDisplaySettings().duplicate());

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }
}
