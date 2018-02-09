/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.viewer.StackSliceViewer;

import java.awt.event.ActionEvent;

import net.sci.image.Image;
import net.sci.image.process.shape.ImageSlicer;

/**
 * Extract a planar slice from a 3D image.
 * 
 * @author David Legland
 *
 */
public class Image3DGetCurrentSliceAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Image3DGetCurrentSliceAction(ImagoFrame frame, String name)
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
		System.out.println("extract current slice from 3D image");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		ImageViewer viewer = ((ImagoDocViewer) this.frame).getImageView();

		Image image	= doc.getImage();

		if (!(viewer instanceof StackSliceViewer))
		{
		    System.err.println("Requires 3D image viewer");
		    return;
		}
		
        // get slice index
		// TODO: have some "Image3DViewer" interface
		StackSliceViewer viewer3d = (StackSliceViewer) viewer;
		int sliceIndex = viewer3d.getSliceIndex();
		
		Image result = ImageSlicer.slice2d(image, sliceIndex);
		
		// new name contains slice index, with number of digits depending on slice number
		int sizeZ = image.getSize(2);
		int nDigits = (int) Math.floor(Math.log10(sizeZ-1)) + 1;
		result.setName(image.getName() + "-z" + String.format("%0" + nDigits + "d", sliceIndex));
		
		// add the image document to GUI
		this.gui.addNewDocument(result);
	}
}
