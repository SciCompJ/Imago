/**
 * 
 */
package imago.gui.action;

import java.awt.event.ActionEvent;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
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
		Image image  = ((ImageFrame) this.frame).getImage();

		Image result = operator.process(image);

		// add the image document to GUI
		this.gui.createImageFrame(result, this.frame);
	}

}
