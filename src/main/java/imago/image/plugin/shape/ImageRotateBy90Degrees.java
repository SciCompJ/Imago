/**
 * 
 */
package imago.image.plugin.shape;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.plugin.ImageFramePlugin;
import net.sci.array.shape.Rotate3D90;
import net.sci.array.shape.Rotate90;
import net.sci.image.Image;

/**
 * Applies a 90-degrees rotation around one of the main axes to a given image.
 * 
 * @author dlegland
 *
 */
public class ImageRotateBy90Degrees implements ImageFramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageRotateBy90Degrees()
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
        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();

        boolean is3D = image.getDimension() == 3;
        
        GenericDialog gd = new GenericDialog(frame, "Rotate by 90 degrees");
        if (is3D) gd.addNumericField("Rotation Axis:", 0, 0);
        gd.addNumericField("Rotation count:", 1, 0);
        if (!is3D) gd.addCheckBox("Create View", true);
        gd.showDialog();

        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        int axisIndex = is3D ? (int) gd.getNextNumber() : 2;
        int rotationCount = (int) gd.getNextNumber();
        boolean createView = is3D ? false : gd.getNextBoolean();

        Image result;
        if (is3D)
        {
            Rotate3D90 algo = new Rotate3D90(axisIndex, rotationCount);
            result = imageFrame.runOperator(algo, image);
        }
        else
        {
            Rotate90 algo = new Rotate90(rotationCount);
            if (createView) 
            {
                result = new Image(algo.createView(image.getData()), image);
            }
            else
            {
                result = imageFrame.runOperator(algo, image);
            }
        }
        result.setName(image.getName() + "-rot90");

        // add the image document to GUI
        ImageFrame.create(result, frame);
    }
}
