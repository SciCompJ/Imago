/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.process.shape.Slicer;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class MiddleSliceImageAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MiddleSliceImageAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		System.out.println("middle Slice");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		// check image dimensionality
		if (image.getDimension() < 2)
		{
			System.err.println("Requires 3-dimensional image");
			return;
		}
		
		// compute slice index
		int index = Math.max((image.getSize(2)) / 2 - 1, 0);
		
		// compute resulting slice
		Slicer filter = new Slicer(2, index);
		Image result = image.apply(filter);
				
		// add the image document to GUI
		this.gui.addNewDocument(result); 
	}

}
