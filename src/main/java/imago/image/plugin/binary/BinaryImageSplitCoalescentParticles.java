/**
 * 
 */
package imago.image.plugin.binary;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.binary.SplitCoalescentParticles;
import net.sci.image.connectivity.Connectivity2D;
import net.sci.image.connectivity.Connectivity3D;

/**
 * 
 */
public class BinaryImageSplitCoalescentParticles implements FramePlugin
{
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        Array<?> array = image.getData();

        int nd = array.dimensionality();
        if (nd != 2 && nd != 3)
        {
           frame.showErrorDialog("Implemented only for 2D and 3D images", "Dimensionality Error");
           return;
        }
        
        GenericDialog gd = new GenericDialog(frame, "Split Coalescent Particles");
        gd.addNumericField("Dynamic ", 1.0, 1);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        double dynamic = gd.getNextNumber();
        
        // create operator
        SplitCoalescentParticles op = new SplitCoalescentParticles();
        op.setDynamic(dynamic);
        op.setConnectivity(nd == 2 ? Connectivity2D.C8 : Connectivity3D.C26);
        
        // apply operator on current image
        Image result = ((ImageFrame) frame).runOperator(op, image);
        result.setName(image.getName() + "-WSsplit");
        
        // add the image document to GUI
        ImageFrame.create(result, frame);
    }


    /**
     * Returns true if the current frame contains a binary image.
     * 
     * @param frame
     *            the frame containing reference to this plugin
     * @return true if the frame contains a binary image.
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.isBinaryImage();
    }
}
