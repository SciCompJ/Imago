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
import net.sci.array.data.Float64Array;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertToFloat64ImageAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConvertToFloat64ImageAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("convert to float64 image");
		
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
		
		Float64Array result = Float64Array.convert(array);
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		this.gui.addNewDocument(resultImage); 
	}

}
