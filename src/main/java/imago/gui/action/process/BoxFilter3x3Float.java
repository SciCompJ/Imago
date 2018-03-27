/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.data.Float32Array;
import net.sci.array.data.ScalarArray;
import net.sci.image.Image;
import net.sci.image.process.filter.BoxFilter3x3;

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

		// Check array is scalar
		if (!(array instanceof ScalarArray))
		{
		    return;
		}

		// create result image with specified type
		Float32Array output = Float32Array.create(array.getSize());

		// create operator and apply
		BoxFilter3x3 filter = new BoxFilter3x3();
		filter.processScalar((ScalarArray<?>) array, output);
		Image result = new Image(output, metaImage);

		// add the image document to GUI
		this.gui.addNewDocument(result);
	}

}
