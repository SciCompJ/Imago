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
import net.sci.array.data.ScalarArray;
import net.sci.array.data.VectorArray;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class StackToVectorImageAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StackToVectorImageAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("stack to vector image");
		
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
		if (array.dimensionality() != 3 || !(array instanceof ScalarArray))
		{
			// TODO : display error message
			System.err.println("Requires a 3D scalar stack");
			return;
		}

		VectorArray<?> vectArray = VectorArray.fromStack((ScalarArray<?>) array);
		Image result = new Image(vectArray, image);
				
		// add the image document to GUI
		this.gui.addNewDocument(result); 
	}

}
