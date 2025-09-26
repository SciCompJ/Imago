/**
 * 
 */
package imago.image.plugin.edit;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.ImageViewer;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt16;
import net.sci.array.numeric.UInt16Array;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class SetImageDisplayRangeToDataType implements FramePlugin
{
    public SetImageDisplayRangeToDataType()
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
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image metaImage = doc.getImage();

		Array<?> array = metaImage.getData();
		if (array instanceof VectorArray) 
		{
			array = VectorArray.norm((VectorArray<?,?>) array);
		}
		
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar Array");
		}
		ScalarArray<?> scalarArray = (ScalarArray<?>) array;
		
		double[] extent = new double[]{0, 1};
		if (scalarArray instanceof UInt8Array)
		{
			extent = new double[]{0, 255};
		}
		else if (scalarArray instanceof UInt16Array)
		{
			extent = new double[]{0, UInt16.MAX_INT};
		}
		System.out.println("  New value range: [" + extent[0] + " ; " + extent[1] + "]");
		
		metaImage.getDisplaySettings().setDisplayRange(extent);
		
		ImageViewer viewer = ((ImageFrame) frame).getImageViewer();
		
		// update display
		viewer.refreshDisplay();
//		viewer.repaint();
	}
}

