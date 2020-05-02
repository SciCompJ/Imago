/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.ImageAxis;

/**
 * @author dlegland
 *
 */
public class ConvertStackToMovie implements Plugin
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
        System.out.println("convert 3D image to movie");

        // get current image data
        ImageFrame viewer = (ImageFrame) frame;
        ImageHandle doc = viewer.getDocument();
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
        frame.getGui().addNewDocument(resultImage); 
    }
    
}
