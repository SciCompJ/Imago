/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.process.shape.Slicer;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class Image3DMiddleSlice implements Plugin
{
	public Image3DMiddleSlice() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("middle Slice");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
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
		frame.getGui().addNewDocument(result); 
	}

}