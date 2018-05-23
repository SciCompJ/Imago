/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImagoDoc;
import imago.gui.ImageViewer;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class SetImageDisplayRangeToData implements Plugin
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
		System.out.println("choose image display extent");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image = doc.getImage();

		Array<?> array = image.getData();
		if (array instanceof VectorArray) 
		{
			array = VectorArray.norm((VectorArray<?>) array);
		}
		
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar Array");
		}
		ScalarArray<?> scalarArray = (ScalarArray<?>) array;
		
		// Compute min and max values within the array 
		double[] extent = scalarArray.valueRange();
		System.out.println("Array value range: [" + extent[0] + " ; " + extent[1] + "]");
		
		image.setDisplayRange(extent);

		// refresh display
		ImageViewer viewer = ((ImagoDocViewer) frame).getImageView();
		viewer.refreshDisplay();
	}
}

