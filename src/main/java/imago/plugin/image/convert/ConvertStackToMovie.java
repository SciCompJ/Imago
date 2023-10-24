/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.ImageAxis;

/**
 * @author dlegland
 *
 */
public class ConvertStackToMovie implements FramePlugin
{
    
    /**
     * 
     */
    public ConvertStackToMovie()
    {
    }
    
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame viewer = (ImageFrame) frame;
        ImageHandle doc = viewer.getImageHandle();
        Image image = doc.getImage();

        int nd = image.getDimension();
        if (nd < 3)
        {
            return;
        }
        
        Image resultImage = image.duplicate();
        
        // convert last axis to time axis
        Calibration calib = resultImage.getCalibration();
        calib.setAxis(nd-1, new ImageAxis.T());
        
        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }
    
}
