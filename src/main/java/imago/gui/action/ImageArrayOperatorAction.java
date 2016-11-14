/**
 * 
 */
package imago.gui.action;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.ImageArrayOperator;

/**
 * Encapsulates an operator on array.
 * 
 * @author David Legland
 *
 */
public class ImageArrayOperatorAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ImageArrayOperator operator;

	public ImageArrayOperatorAction(ImagoFrame frame, String name, ImageArrayOperator op)
	{
		super(frame, name);
		this.operator = op;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		Array<?> data = image.getData();
		Array<?> data2 = operator.process(data);
		Image result = new Image(data2, image);

//		Image result = operator.createEmptyOutputImage(image);
//		operator.process(image, result);

		// add the image document to GUI
		this.gui.addNewDocument(result, doc);
	}

}
