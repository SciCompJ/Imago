/**
 * 
 */
package imago.image.plugin.process;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.numeric.Float32Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;
import net.sci.image.ImageType;

/**
 * Computes an angle from the first two components of a vector image (with at
 * least two components).
 * 
 * @author David Legland
 *
 */
public class VectorImageAngle implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public VectorImageAngle()
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
        {
            return;
        }
        
        // retrieve data array
        Array<?> array = image.getData();
        if (array == null)
        {
            return;
        }
        if (!(array instanceof VectorArray))
        {
            ImagoGui.showErrorDialog(frame, "Requires a Vector image", "Data Type Error");
            return;
        }
        
        VectorArray<?, ?> vectorArray = (VectorArray<?, ?>) array;
        int nChannels = vectorArray.channelCount();
        if (nChannels < 2)
        {
            ImagoGui.showErrorDialog(frame, "Requires at least two channels", "Data Type Error");
            return;
        }
        
        // allocate memory for result array
        ScalarArray<?> angleArray = Float32Array.create(vectorArray.size());
        
        // iterate over elements
        for (int[] pos : angleArray.positions())
        {
            double vx = vectorArray.getValue(pos, 0);
            double vy = vectorArray.getValue(pos, 1);
            double angle = Math.atan2(vy, vx);
            angleArray.setValue(pos, angle);
        }
        
        // create resulting image
        Image resultImage = new Image(angleArray, ImageType.ANGLE, image);
        resultImage.getDisplaySettings().setDisplayRange(new double[] {-Math.PI, Math.PI});
        String name = String.format("%s-angle", image.getName());
        resultImage.setName(name);
        
        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }
}
