/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.numeric.IntArray;
import net.sci.image.Image;
import net.sci.image.label.LabelImages;

/**
 * 
 */
public class LabelMapCropLabel implements FramePlugin
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
        IntArray<?> array = (IntArray<?>) image.getData();
        
        // Create dialog for entering parameters
        GenericDialog gd = new GenericDialog(frame, "Crop Label");
        gd.addNumericField("Region Label", 1, 0);
        gd.addNumericField("Add Border (pixels)", 0, 0);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // retrieve option values
        int label = (int) gd.getNextNumber();
        int border = (int) gd.getNextNumber();
        
        IntArray<?> res = LabelImages.cropLabel(array, label, border);
        Image resImage = new Image(res, image);
        resImage.setName(image.getName() + "-crop");

        // add the image document to GUI
        ImageFrame.create(resImage, frame);
    }
}
