/**
 * 
 */
package imago.plugin;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.data.Float32Array;
import net.sci.array.data.ScalarArray;
import net.sci.image.Image;
import net.sci.image.process.filter.BoxFilter3x3;

/**
 * @author David Legland
 *
 */
public class BoxFilter3x3FloatPlugin implements Plugin
{
	public BoxFilter3x3FloatPlugin()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("box filter 3x3 float");
		
		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
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
		if (frame instanceof ImagoDocViewer)
		{
		    filter.addAlgoListener((ImagoDocViewer) frame);
		}
		filter.processScalar((ScalarArray<?>) array, output);
		Image result = new Image(output, metaImage);

		// add the image document to GUI
		frame.getGui().addNewDocument(result);
	}

}
