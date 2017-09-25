/**
 * 
 */
package imago.gui.action.image;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.image.Image;
import net.sci.image.process.shape.ImageSlicer;

/**
 * Extract a planar slice from a 3D image.
 * 
 * @author David Legland
 *
 */
public class Image3DGetSliceAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Image3DGetSliceAction(ImagoFrame frame, String name)
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
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("extract planar slice from 3D image");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();

		GenericDialog gd = new GenericDialog(this.frame, "Extract planar slice");
        gd.addNumericField("Dim. 1 ", 0, 0);
        gd.addNumericField("Dim. 2 ", 1, 0);
        gd.addNumericField("Slice index ", 0, 0);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        int dim1 = (int) gd.getNextNumber();
        int dim2 = (int) gd.getNextNumber();
        int sliceIndex = (int) gd.getNextNumber();

        int[] refPos = new int[]{sliceIndex, sliceIndex, sliceIndex};
        
        Image result = ImageSlicer.slice2d(image, dim1, dim2, refPos);
		result.setName(image.getName() + "-slices");
		
		// add the image document to GUI
		this.gui.addNewDocument(result);
	}
}
