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
import net.sci.array.data.scalar2d.ScalarArray2D;
import net.sci.array.data.scalar3d.ScalarArray3D;
import net.sci.image.Image;
import net.sci.image.morphology.MorphologicalReconstruction;


/**
 * @author David Legland
 *
 */
public class ImageKillBordersAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ImageKillBordersAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("kill borders");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray2D) && !(array instanceof ScalarArray3D))
		{
			return;
		}
		Image resultImage = MorphologicalReconstruction.killBorders(image);
		
		// add the image document to GUI
		this.gui.addNewDocument(resultImage); 
	}

}
