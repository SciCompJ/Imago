/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.ImageViewer;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt16;
import net.sci.array.scalar.UInt16Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class SetImageDisplayRangeToDataType implements Plugin
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
        System.out.println("set display range to data type");
		
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getDocument();
		Image metaImage = doc.getImage();

		Array<?> array = metaImage.getData();
		if (array instanceof VectorArray) 
		{
			array = VectorArray.norm((VectorArray<?>) array);
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
			extent = new double[]{0, UInt16.MAX_VALUE};
		}
		System.out.println("  New value range: [" + extent[0] + " ; " + extent[1] + "]");
		
		metaImage.getDisplaySettings().setDisplayRange(extent);
		
		ImageViewer viewer = ((ImageFrame) frame).getImageView();
		
		// update display
		viewer.refreshDisplay();
//		viewer.repaint();
	}
}

