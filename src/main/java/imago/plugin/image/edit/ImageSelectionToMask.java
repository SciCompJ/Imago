/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.frames.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.viewer.PlanarImageViewer;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.image.Image;

/**
 * Convert the selection of the current image into a new binary image.
 * 
 * @author David Legland
 *
 */
public class ImageSelectionToMask implements FramePlugin
{
	public ImageSelectionToMask()
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
		System.out.println("selection to mask");

		// Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        
        
        ImageViewer viewer = iframe.getImageView();
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
        Geometry2D selection = piv.getSelection();
        if (!(selection instanceof Polygon2D))
        {
            System.out.println("requires selection to be a simple polygon");
            return;
        }
        
        Polygon2D poly = (Polygon2D) selection;
		
        // manage clockwise and counter-clockwise polygons
        boolean clockWise = poly.signedArea() < 0;

        // create array for result
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        BinaryArray2D mask = BinaryArray2D.create(sizeX, sizeY);
        mask.fillBooleans(pos -> poly.contains(pos[0], pos[1]) ^ clockWise);
                
        // create result image
        Image resultImage = new Image(mask, image);
        
        // add the image document to GUI
        frame.getGui().createImageFrame(resultImage); 

	}
}
