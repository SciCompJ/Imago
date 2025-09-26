/**
 * 
 */
package imago.image.plugin.process;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.image.Image;
import net.sci.image.segmentation.OtsuThreshold;

/**
 * Applies threshold to an image by computing threshold value with Otsu
 * algorithm.
 * 
 * Principle of Otsu method is to identify threshold value that maximizes the
 * variance between the classes, or equivalently to minimize the sum of
 * variances within each class.
 */
public class ImageOtsuThreshold implements FramePlugin
{
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

        OtsuThreshold op = new OtsuThreshold();
        Image resultImage = ((ImageFrame) frame).runOperator("Otsu Threshold", op, image);
        resultImage.setName(image.getName() + "-segOtsu");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

}
