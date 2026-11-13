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
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.numeric.Int;
import net.sci.array.numeric.IntArray;
import net.sci.image.Image;

/**
 * Replacing label values within a label map stored as an array of int.
 */
public class ReplaceLabelValues implements FramePlugin
{
    public static final void replaceLabels(IntArray<?> array, int[] labels, int newLabel)
    {
        HashSet<Integer> labelSet = new HashSet<Integer>();
        for (int i = 0; i < labels.length; i++)
        {
            labelSet.add(labels[i]);
        }
        
        for (int[] pos : array.positions())
        {
            if (labelSet.contains(array.getInt(pos)))
            {
                array.setInt(pos, newLabel);
            }
        }
    }
    
    /**
     * Default empty constructor.
     */
    public ReplaceLabelValues()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame)) return;
        ImageFrame iframe = (ImageFrame) frame;
        ImageHandle handle = iframe.getImageHandle();
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
        GenericDialog gd = new GenericDialog(frame, "Remove/Replace Label(s)");
        gd.addTextField("Label(s)", "1", 12);
        gd.addMessage("Separate label values by \",\"");
        gd.addIntegerField("Final Value", 0);
        gd.addMessage("Replacing by value 0\n will remove label(s)");
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        String labelListString = gd.getNextString();
        int[] labels = parseLabels(labelListString);
        int newLabel = gd.getNextInteger();
        
        replaceLabels(array, labels, newLabel);
        
        
        // notify associated viewers
        handle.notifyImageHandleChange(ImageHandle.Event.IMAGE_MASK | ImageHandle.Event.CHANGE_MASK);
    }
    
    private static final int[] parseLabels(String string) 
    {
        String[] tokens = string.split("[, ]+");
        return Stream.of(tokens)
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
