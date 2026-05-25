/**
 * 
 */
package imago.image.plugins.binary;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.process.ConvertToBinary;
import net.sci.array.numeric.IntArray;
import net.sci.array.numeric.impl.RunLengthInt32ArrayFactory;
import net.sci.image.Image;
import net.sci.image.binary.labeling.ComponentsLabeling;
import net.sci.image.binary.labeling.FloodFillComponentsLabeling1D;
import net.sci.image.binary.labeling.FloodFillComponentsLabeling2D;
import net.sci.image.binary.labeling.FloodFillComponentsLabeling3D;
import net.sci.image.connectivity.Connectivity2D;
import net.sci.image.connectivity.Connectivity3D;
import net.sci.image.label.filters.LabelMapSizeOpening;

/**
 * 
 */
public class BinaryImageSizeOpening implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public BinaryImageSizeOpening()
    {
    }

    @Override
    public void run(ImagoFrame frame, String optionsString)
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

        GenericDialog gd = new GenericDialog(frame, "Size Opening");
        String label = nd == 2 ? "Min Pixel Count:" : "Min Voxel Count:";
        gd.addIntegerField(label, 100);
        if (nd == 2)
        {
            gd.addChoice("Connectivity: ", new String[] { "4", "8" }, "4");
        }
        else if (nd == 3)
        {
            gd.addChoice("Connectivity: ", new String[] { "6", "26" }, "6");
        }
        gd.showDialog();

        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        int minCount = gd.getNextInteger();
        int connIndex = gd.getNextChoiceIndex();
        int connValue = nd == 2 ? (connIndex == 0 ? 4 : 8) : (connIndex == 0 ? 6 : 26);
        IntArray.Factory<?> factory = new RunLengthInt32ArrayFactory();

        // Create Components Labeling algorithm
        ComponentsLabeling algo = switch(nd)
        {
            case 1 -> new FloodFillComponentsLabeling1D(factory);
            case 2 -> new FloodFillComponentsLabeling2D(Connectivity2D.fromValue(connValue), factory);
            case 3 -> new FloodFillComponentsLabeling3D(Connectivity3D.fromValue(connValue), factory);
            default -> throw new RuntimeException("Can not manage images with dimensionality " + nd);
        };
        
        Image labelMap = algo.process(image);
        Image filteredLabels = new LabelMapSizeOpening(minCount).process(labelMap);
        BinaryArray newBinary = new ConvertToBinary().process(filteredLabels.getData());

        // apply connected components labeling
        Image result = new Image(newBinary, image);
        result.setName(image.getName() + "-sizeFilt");
        
        // add the image document to GUI
        ImageFrame.create(result, frame);
    }

}
