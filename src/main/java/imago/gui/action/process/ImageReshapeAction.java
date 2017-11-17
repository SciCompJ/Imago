/**
 * 
 */
package imago.gui.action.process;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.array.Array;
import net.sci.array.process.shape.Reshape;
import net.sci.image.Image;

/**
 * Reshape an image
 * 
 * @author David Legland
 *
 */
public class ImageReshapeAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageReshapeAction(ImagoFrame frame, String name)
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
		System.out.println("reshape image");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		int[] newDims = array.getSize();
		while(true)
		{
		    GenericDialog gd = new GenericDialog(this.frame, "Reshape");
		    for (int d = 0; d < nd; d++)
		    {
		        gd.addNumericField("Size dim. " + (d+1), newDims[d], 0);
		    }
		    gd.showDialog();
		    
		    if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		    {
		        return;
		    }
		    
		    // parse dialog results
		    for (int d = 0; d < nd; d++)
		    {
		        newDims[d] = (int) gd.getNextNumber();
		    }
		    
		    // If compatibility of dimensions is met, break loop 
		    int numel = cumProd(array.getSize());
		    if (cumProd(newDims) == numel)
		    {
		        break;
		    }
		    
            ImagoGui.showErrorDialog(frame, 
                    "Output element number should match input element number: " + numel);
		};
		
		// create reshape operator
		Reshape filter = new Reshape(newDims);
		
		// apply operator on current image
        Array<?> result = filter.process(array);
        Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-reshape");
		
		// add the image document to GUI
		this.gui.addNewDocument(resultImage);
	}

    private static final int cumProd(int[] dims)
    {
        int prod = 1;
        for (int d : dims)
        {
            prod *= d;
        }
        return prod;
    }
}
