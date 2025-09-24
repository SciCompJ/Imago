/**
 * 
 */
package imago.image.plugin.process;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.image.Image;
import net.sci.image.segmentation.IsodataThreshold;

/**
 * Applies threshold to an image by computing threshold value with Isodata
 * algorithm.
 */
public class ImageIsodataThreshold implements FramePlugin
{
    public ImageIsodataThreshold()
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

        IsodataThreshold op = new IsodataThreshold();
        Image resultImage = ((ImageFrame) frame).runOperator("Isodata Threshold", op, image);
        resultImage.setName(image.getName() + "-segIsodata");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

}
