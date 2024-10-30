/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import imago.gui.GuiBuilder;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.numeric.IntArray;
import net.sci.image.Image;
import net.sci.image.ImageType;


/**
 * Changes type of current image to LABEL.
 * 
 * @author David Legland
 *
 */
public class SetImageTypeToLabel implements FramePlugin
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
        ImageFrame viewer = (ImageFrame) frame;
        ImageHandle handle = viewer.getImageHandle();
        Image image = handle.getImage();

        if (image == null)
        { 
            return; 
        }
        Array<?> array = image.getData();
        if (array == null)
        {
            return;
        }
        if (!(array instanceof IntArray))
        {
            ImagoGui.showErrorDialog(frame, "Requires a image containing int values", "Data Type Error");
            return;
        }

        // update type
        image.setType(ImageType.LABEL);

        // recompute display range to ensure labels are displayed with full colormap range
        int[] minMax = array instanceof BinaryArray ? new int[] {0, 1} : ((IntArray<?>) array).intRange();
        image.getDisplaySettings().setDisplayRange(new double[] {0, minMax[1]});

        // notify changes
        handle.notifyImageHandleChange(ImageHandle.Event.IMAGE_MASK | ImageHandle.Event.CHANGE_MASK);
        
        // need to refresh GUI menu
        new GuiBuilder(frame).createMenuBar();
        viewer.updateTitle();
        frame.getWidget().validate();
    }
}
