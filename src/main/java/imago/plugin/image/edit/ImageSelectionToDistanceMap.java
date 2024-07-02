/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.image.PlanarImageViewer;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.Float32Array2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;

/**
 * Convert the selection of the current image into a new scalar image containing
 * the distance to current selection.
 * 
 * @author David Legland
 *
 */
public class ImageSelectionToDistanceMap implements FramePlugin
{
	public ImageSelectionToDistanceMap()
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
		// Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        
        
        ImageViewer viewer = iframe.getImageViewer();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		if (array.dimensionality() != 2)
		{
		    throw new RuntimeException("Requires an image containing 2D Array");
		}
		
        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry2D selection = (Geometry2D) piv.getSelection();

        // create array for result
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        Float32Array2D distMap = Float32Array2D.create(sizeX, sizeY);
        
        // iterate over output pixels
        distMap.fillValues((x, y) -> selection.distance(x, y));
        
        // create result image
        Image resultImage = new Image(distMap, image);

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }
}
