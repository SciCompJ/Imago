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
import net.sci.array.ArrayOperator;
import net.sci.image.Image;

/**
 * Encapsulates an operator on array.
 * 
 * @author David Legland
 *
 */
public class ArrayOperatorAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayOperator operator;

	public ArrayOperatorAction(ImagoFrame frame, String name, ArrayOperator op)
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

		// add the image document to GUI
		this.gui.addNewDocument(result, doc);
	}

}