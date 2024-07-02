/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
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
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
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
        if (!(array instanceof ScalarArray))
        {
            ImagoGui.showErrorDialog(frame, "Requires a scalar image", "Data Type Error");
            return;
        }

        // update type
        image.setType(ImageType.LABEL);

        // recompute display range to ensure labels are displayed with full colormap range
        double[] minMax = ((ScalarArray<?>) array).valueRange();
        minMax[0] = 0;
        image.getDisplaySettings().setDisplayRange(minMax);

        // notify changes
        handle.notifyImageHandleChange(ImageHandle.Event.IMAGE_MASK | ImageHandle.Event.CHANGE_MASK);
        viewer.updateTitle();
	}
}
