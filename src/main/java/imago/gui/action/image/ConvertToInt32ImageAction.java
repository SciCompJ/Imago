/**
 * 
 */
package imago.gui.action.image;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.data.Int32Array;
import net.sci.array.data.ScalarArray;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertToInt32ImageAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConvertToInt32ImageAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("convert to int32 image");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
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
			// TODO : display error message
			System.err.println("Requires a scalar image");
			return;
		}

		
		Int32Array result = Int32Array.convert((ScalarArray<?>) array);
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		this.gui.addNewDocument(resultImage); 
	}

}
