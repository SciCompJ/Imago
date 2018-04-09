/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.process.shape.Flip;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ImageFlip implements Plugin
{
	int dim;
	
    public ImageFlip() 
    {
        this.dim = 0;
    }
    
    public ImageFlip(int dim) 
    {
        this.dim = dim;
    }
    
	@Override
	public void run(ImagoFrame frame) 
	{
		System.out.println("flip image");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image = doc.getImage();

		if (this.dim > image.getDimension())
		{
            throw new IllegalArgumentException(String.format(
                    "Can now flip image of dim. %d along dim. %d", image.getDimension(), this.dim));
		}
		
		Flip filter = new Flip(this.dim);
		Image result = image.apply(filter);
				
		// add the image document to GUI
		frame.getGui().addNewDocument(result); 
	}

	@Override
	public boolean isEnabled(ImagoFrame frame)
	{
	    // check frame class
	    if (!(frame instanceof ImagoDocViewer))
	        return false;
	    
	    // check image
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return true;
	}
}
