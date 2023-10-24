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
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;
import net.sci.image.ImageType;


/**
 * @author David Legland
 *
 */
public class SetImageTypeToLabel implements FramePlugin
{
	public SetImageTypeToLabel() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current frame
		ImageFrame viewer = (ImageFrame) frame;
        ImageHandle doc = viewer.getImageHandle();
		Image image = doc.getImage();
		
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
		
		// update widgets
		viewer.getImageView().refreshDisplay();
		viewer.repaint();
		viewer.updateTitle();
	}

}
