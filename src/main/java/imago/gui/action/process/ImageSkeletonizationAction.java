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
import net.sci.array.data.scalar2d.BinaryArray2D;
import net.sci.image.Image;
import net.sci.image.binary.skeleton.ImageJSkeleton;


/**
 * @author David Legland
 *
 */
public class ImageSkeletonizationAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ImageSkeletonizationAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("skeletonization");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		Array<?> array = image.getData();
		if (!(array instanceof BinaryArray2D))
		{
			return;
		}
		
		ImageJSkeleton skel = new ImageJSkeleton();
        BinaryArray2D res = skel.process2d((BinaryArray2D) array);

        Image resultImage = new Image(res, image);
        
		// add the image document to GUI
		this.gui.addNewDocument(resultImage); 
	}

}
