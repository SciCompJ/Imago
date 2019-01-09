/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.process.shape.Crop;
import net.sci.image.Image;

/**
 * Crop an image by specifying the bounds in a dialog. 
 * 
 * @author David Legland
 *
 */
public class ImageCropDialog implements Plugin
{
	public ImageCropDialog()
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
		System.out.println("crop");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(frame, "Crop Image");
		for (int d = 0; d < nd; d++)
		{
            gd.addNumericField("Min index. " + (d+1) + " (incl.)", 0, 0);
            gd.addNumericField("Max index. " + (d+1) + " (excl.)", array.getSize(d), 0);
		}
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        int[] minDims = new int[nd];
        int[] maxDims = new int[nd];
		for (int d = 0; d < nd; d++)
		{
            minDims[d] = (int) gd.getNextNumber();
            maxDims[d] = (int) gd.getNextNumber();
		}

		// create operator box filtering operator
		Crop op= new Crop(minDims, maxDims);
		op.addAlgoListener((ImagoDocViewer) frame);
		
		// apply operator on current image array
		Array<?> result = op.process(array);
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-crop");
		
		// add the image document to GUI
		frame.addChild(frame.getGui().addNewDocument(resultImage));
	}
	
    /**
     * Returns true if the current frame contains an image.
     * 
     * @param frame
     *            the frame containing reference to this plugin
     * @return true if the frame contains an image.
     */
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
