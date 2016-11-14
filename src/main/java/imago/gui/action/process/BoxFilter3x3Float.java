/**
 * 
 */
package imago.gui.action.process;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.ArrayToArrayOperator;
import net.sci.array.data.FloatArray;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class BoxFilter3x3Float extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BoxFilter3x3Float(ImagoFrame frame, String name)
	{
		super(frame, name);
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
		System.out.println("box filter 3x3 float");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image metaImage = doc.getImage();
		Array<?> array = metaImage.getData();

		// create result image with specified type
		FloatArray output = FloatArray.create(array.getSize());

		// create operator and apply
		ArrayToArrayOperator filter = new net.sci.image.process.BoxFilter3x3();
		filter.process(array, output);
		Image result = new Image(output, metaImage);

		// add the image document to GUI
		this.gui.addNewDocument(result);
	}

}
