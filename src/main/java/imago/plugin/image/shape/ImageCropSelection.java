/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.ImageViewer;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.PlanarImageViewer;
import net.sci.array.Array;
import net.sci.array.process.shape.Crop;
import net.sci.geom.geom2d.Box2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;

/**
 * Crop an image by using the bounding box of current selection. 
 * 
 * @author David Legland
 *
 */
public class ImageCropSelection implements Plugin
{
	public ImageCropSelection()
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

        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        Image image = iframe.getImageHandle().getImage();
        Array<?> array = image.getData();

        int nd = array.dimensionality();
        if (nd != 2)
        {
           throw new RuntimeException("Requires 2D array");
        }

        ImageViewer viewer = iframe.getImageView();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }
        
        // get bounding box of current selection
        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry2D selection = piv.getSelection();
        Box2D box = selection.boundingBox();
        
        // determine crop size
        int[] minInds = new int[2];
        int[] maxInds = new int[2];
        for (int d = 0;d < 2; d++)
        {
            minInds[d] = Math.max((int) box.getMin(0), 0); 
            maxInds[d] = Math.min((int) box.getMax(0) + 1, array.size(d)); 
        }
        
		// create operator box filtering operator
		Crop op = Crop.fromMinMax(minInds, maxInds);
		op.addAlgoListener((ImageFrame) frame);
		
		// apply operator on current image array
		Array<?> result = op.process(array);
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-crop");
		
		// add the image document to GUI
		frame.addChild(frame.getGui().createImageFrame(resultImage));
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
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return true;
    }
}