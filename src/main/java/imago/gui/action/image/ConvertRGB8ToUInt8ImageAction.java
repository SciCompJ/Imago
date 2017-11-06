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
import net.sci.array.data.UInt8Array;
import net.sci.array.data.color.RGB8Array;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertRGB8ToUInt8ImageAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConvertRGB8ToUInt8ImageAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("convert rgb8 image to uint8 image");
		
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
		if (!(array instanceof RGB8Array))
		{
		    ImagoGui.showErrorDialog(frame, "Requires a RGB8 color image", "Data Type Error");
			return;
		}

		UInt8Array result = ((RGB8Array) array).convertToUInt8();
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		this.gui.addNewDocument(resultImage); 
	}

}
