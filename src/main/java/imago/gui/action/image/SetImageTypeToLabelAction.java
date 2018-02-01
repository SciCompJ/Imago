/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.data.ScalarArray;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class SetImageTypeToLabelAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SetImageTypeToLabelAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("convert image type to label");
		
		// get current frame
		ImagoDocViewer viewer = (ImagoDocViewer) this.frame;
        ImagoDoc doc = viewer.getDocument();
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
		image.setType(Image.Type.LABEL);
		
		// recompute display range to ensure labels are displayed with full colormap range
		double[] minMax = ((ScalarArray<?>) array).valueRange();
		minMax[0] = 0;
		image.setDisplayRange(minMax);
		
		// update widgets
		viewer.getImageView().refreshDisplay();
		viewer.repaint();
		viewer.updateTitle();
	}

}
