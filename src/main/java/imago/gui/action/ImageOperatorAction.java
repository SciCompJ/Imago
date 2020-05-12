/**
 * 
 */
package imago.gui.action;

import imago.app.ImageHandle;
import imago.gui.ImagoAction;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.image.Image;
import net.sci.image.ImageOperator;

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
	public void actionPerformed(ActionEvent evt)
	{
		// get current frame
		ImageHandle doc = ((ImageFrame) this.frame).getImageHandle();
		Image image = doc.getImage();

		Image result = operator.process(image);

		// add the image document to GUI
		this.gui.createImageFrame(result, doc);
	}

}
