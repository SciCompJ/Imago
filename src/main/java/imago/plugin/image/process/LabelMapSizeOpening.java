/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.image.Image;

/**
 * Applies size opening on a label map: creates a new label map that contains
 * only regions with at least the specified number of elements.
 */
public class LabelMapSizeOpening implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame)) return;
        ImageFrame iframe = (ImageFrame) frame;
        Image image = iframe.getImageHandle().getImage();

        // requires a label map image
        if (!image.isLabelImage())
        {
            return;
        }
        
        // Create dialog for entering parameters
        GenericDialog gd = new GenericDialog(frame, "Size Opening");
        gd.addNumericField("Min Pixel Count:", 100, 0);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // retrieve option values
        int minCount = (int) gd.getNextNumber();
        
        Image resImage = new net.sci.image.label.filters.LabelMapSizeOpening(minCount).process(image);
        resImage.setName(image.getName() + "-sizeOp" + minCount);

        // add the image document to GUI
        ImageFrame.create(resImage, frame);
    }
}
