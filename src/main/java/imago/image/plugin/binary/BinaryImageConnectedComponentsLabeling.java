/**
 * 
 */
package imago.image.plugin.binary;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.numeric.Int32Array;
import net.sci.array.numeric.IntArray;
import net.sci.array.numeric.UInt16Array;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.impl.RunLengthInt32ArrayFactory;
import net.sci.image.Image;
import net.sci.image.binary.labeling.ComponentsLabeling;
import net.sci.image.binary.labeling.FloodFillComponentsLabeling1D;
import net.sci.image.binary.labeling.FloodFillComponentsLabeling2D;
import net.sci.image.binary.labeling.FloodFillComponentsLabeling3D;
import net.sci.image.connectivity.Connectivity2D;
import net.sci.image.connectivity.Connectivity3D;

/**
 * Connected component labeling of a binary image.
 * 
 * @author David Legland
 *
 */
public class BinaryImageConnectedComponentsLabeling implements FramePlugin
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
        ImageFrame imageFrame = (ImageFrame) frame;

        // retrieve image data
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();
        if (!(array instanceof BinaryArray))
        {
            frame.showErrorDialog("Requires a binary image input", "Data Type Error");
            return;
        }

        // check image dimensionality
        int nd = array.dimensionality();
        if (nd != 2 && nd != 3)
        {
            frame.showErrorDialog("Can process only 2D and 3D images", "Dimensionality Error");
            return;
        }

        GenericDialog gd = new GenericDialog(frame, "CC Labeling");
        if (nd == 2)
        {
            gd.addChoice("Connectivity: ", new String[] { "4", "8" }, "4");
        }
        else if (nd == 3)
        {
            gd.addChoice("Connectivity: ", new String[] { "6", "26" }, "6");
        }
        gd.addChoice("Output Type: ", new String[] { "8-bits", "16-bits", "32-bits", "32-bits (RLE)" }, "16-bits");
        gd.showDialog();

        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        int connIndex = gd.getNextChoiceIndex();
        int connValue = nd == 2 ? (connIndex == 0 ? 4 : 8) : (connIndex == 0 ? 6 : 26);
        int bitDepthIndex = gd.getNextChoiceIndex();
        IntArray.Factory<?> factory = switch (bitDepthIndex)
        {
            case 0 -> UInt8Array.defaultFactory;
            case 1 -> UInt16Array.defaultFactory;
            case 2 -> Int32Array.defaultFactory;
            case 3 -> new RunLengthInt32ArrayFactory();
            default -> throw new IllegalArgumentException("Bit depth index out of range");
        };

        // Create Components Labeling algorithm
        ComponentsLabeling algo = switch(nd)
        {
            case 1 -> new FloodFillComponentsLabeling1D(factory);
            case 2 -> new FloodFillComponentsLabeling2D(Connectivity2D.fromValue(connValue), factory);
            case 3 -> new FloodFillComponentsLabeling3D(Connectivity3D.fromValue(connValue), factory);
            default -> throw new RuntimeException("Can not manage images with dimensionality " + nd);
        };

        // apply connected components labeling
        Image result = imageFrame.runOperator(algo, image);
        result.setName(image.getName() + "-lbl");
        
        // add the image document to GUI
        ImageFrame.create(result, frame);
    }

    /**
     * Returns true if the current frame contains a binary image.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     * @return true if the frame contains a binary image
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame)) return false;

        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null) return false;

        return image.isBinaryImage();
    }
}
