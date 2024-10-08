/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class SetImageDisplayRangeToData implements FramePlugin
{
	public SetImageDisplayRangeToData() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image = doc.getImage();

		Array<?> array = image.getData();
		if (array instanceof VectorArray) 
		{
			array = VectorArray.norm((VectorArray<?,?>) array);
		}
		
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar Array");
		}
		ScalarArray<?> scalarArray = (ScalarArray<?>) array;
		
		// Compute min and max values within the array 
		double[] extent = scalarArray.valueRange();
		System.out.println("Array value range: [" + extent[0] + " ; " + extent[1] + "]");
		
		image.getDisplaySettings().setDisplayRange(extent);

		// refresh display
		ImageViewer viewer = ((ImageFrame) frame).getImageViewer();
		viewer.refreshDisplay();
	}
}

