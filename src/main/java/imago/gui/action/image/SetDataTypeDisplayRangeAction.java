/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.data.ScalarArray;
import net.sci.array.data.UInt16Array;
import net.sci.array.data.UInt8Array;
import net.sci.array.type.UInt16;
import net.sci.image.Image;

import java.awt.event.ActionEvent;

/**
 * @author David Legland
 *
 */
public class SetDataTypeDisplayRangeAction extends ImagoAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SetDataTypeDisplayRangeAction(ImagoFrame frame, String name) {
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.out.println("set display range to data type");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image metaImage = doc.getImage();

		Array<?> image = metaImage.getData();
//		if (image instanceof VectorImage) {
//			image = ((VectorImage) image).getNorm();
//		}
		
		if (!(image instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar Image");
		}
		ScalarArray<?> scalarImage = (ScalarArray<?>) image;
		
		double[] extent = new double[]{0, 1};
		if (scalarImage instanceof UInt8Array)
		{
			extent = new double[]{0, 255};
		}
		else if (scalarImage instanceof UInt16Array)
		{
			extent = new double[]{0, UInt16.MAX_VALUE};
		}
		System.out.println("  New value range: [" + extent[0] + " ; " + extent[1] + "]");
		
		metaImage.setDisplayRange(extent);
		
		ImageViewer viewer = ((ImagoDocViewer) this.frame).getImageView();
		
		// update display
		viewer.refreshDisplay();
//		viewer.repaint();
	}
}

