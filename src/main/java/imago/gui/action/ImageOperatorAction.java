/**
 * 
 */
package imago.gui.action;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.image.ImageOperator;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class ImageOperatorAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ImageOperator operator;

	public ImageOperatorAction(ImagoFrame frame, String name, ImageOperator op)
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

		Image result = operator.process(image);

		// add the image document to GUI
		this.gui.addNewDocument(result, doc);
	}

}
