/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.data.ScalarArray;
import net.sci.array.data.VectorArray;
import net.sci.array.data.scalar3d.ScalarArray3D;
import net.sci.array.data.vector.VectorArray2D;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertImage3DToVectorImage implements Plugin
{
	public ConvertImage3DToVectorImage() 
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("stack to vector image");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
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
		if (array.dimensionality() != 3 || !(array instanceof ScalarArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires a 3D scalar stack", "Data Type Error");
			return;
		}

		VectorArray<?> vectArray = VectorArray2D.fromStack((ScalarArray3D<?>) array);
		Image result = new Image(vectArray, image);
				
		// add the image document to GUI
		frame.getGui().addNewDocument(result); 
	}

}
