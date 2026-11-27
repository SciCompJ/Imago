/**
 * 
 */
package imago.image.plugins.labelmap;

import java.util.HashSet;
import java.util.stream.Stream;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.numeric.Int;
import net.sci.array.numeric.IntArray;
import net.sci.image.Image;

/**
 * Creates a new label map by retaining only a selection of labels within the
 * original label map.
 */
public class LabelMapSelectLabels implements FramePlugin
{
    private static final IntArray<?> keepLabels(IntArray<?> array, int[] labels)
    {
        HashSet<Integer> labelSet = new HashSet<Integer>();
        for (int i = 0; i < labels.length; i++)
        {
            labelSet.add(labels[i]);
        }
        
        IntArray<?> res = array.newInstance(array.size());
        for (int[] pos : array.positions())
        {
            int label = array.getInt(pos);
            if (label != 0 && labelSet.contains(label))
            {
                res.setInt(pos, label);
            }
        }
        
        return res;
    }
    
    /**
     * Default empty constructor.
     */
    public LabelMapSelectLabels()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
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
        
        // cast to int array
        @SuppressWarnings({ "unchecked", "rawtypes" })
        IntArray<?> array = IntArray.wrap((Array<Int>) image.getData());
        
        // Create dialog for entering parameters
        GenericDialog gd = new GenericDialog(frame, "Choose Label(s)");
        gd.addTextField("Label(s)", "1", 12);
        gd.addMessage("Separate label values by \",\"");
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        String labelListString = gd.getNextString();
        int[] labels = parseLabels(labelListString);
        
        IntArray<?> newLabels = keepLabels(array, labels);
        Image resImage = new Image(newLabels, image);
        
        resImage.setName(image.getName() + "-sel");

        // add the image document to GUI
        ImageFrame.create(resImage, frame);
    }
    
    private static final int[] parseLabels(String string) 
    {
        String[] tokens = string.split("[, ]+");
        return Stream.of(tokens)
                .mapToInt(Integer::parseInt)
                .toArray();
    }
    
    /**
     * Defines whether this plugin should be enabled for the given frame.
     * 
     * Returns true if the calling frame is an instance of ImageFrame and it
     * contains a valid label map image.
     * 
     * @param frame
     *            the calling frame
     * @return true if the calling frame contains an image.
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        if (frame instanceof ImageFrame imageFrame)
        {
            Image image = imageFrame.getImageHandle().getImage();
            return image != null && image.isLabelImage();
        }

        return false;
    }
}
