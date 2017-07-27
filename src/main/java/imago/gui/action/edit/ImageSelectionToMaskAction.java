/**
 * 
 */
package imago.gui.action.edit;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.viewer.PlanarImageViewer;
import net.sci.array.Array;
import net.sci.array.data.scalar2d.BinaryArray2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.SimplePolygon2D;
import net.sci.image.Image;

/**
 * Convert the selection of the current image into a new binary image.
 * 
 * @author David Legland
 *
 */
public class ImageSelectionToMaskAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageSelectionToMaskAction(ImagoFrame frame, String name)
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
		System.out.println("selection to mask");

		// Check type is image frame
        if (!(frame instanceof ImagoDocViewer))
            return;
        ImagoDocViewer iframe = (ImagoDocViewer) frame;
        
        
        ImageViewer viewer = iframe.getImageView();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		if (array.dimensionality() != 2)
		{
		    throw new RuntimeException("Requires an image containing 2D Array");
		}

		
        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry2D selection = piv.getSelection();
        if (!(selection instanceof SimplePolygon2D))
        {
            System.out.println("requires selection to be a simple polygon");
            return;
        }
        
        SimplePolygon2D poly = (SimplePolygon2D) selection;
		
        // manage clockwise and counter-clockwise polygons
        boolean clockWise = poly.signedArea() < 0;

        // create array for result
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        BinaryArray2D mask = BinaryArray2D.create(sizeX, sizeY);
        
        // iterate over output pixels 
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                if (poly.contains(new Point2D(x, y)) ^ clockWise)
                {
                    mask.setState(x, y, true);
                }
            }
        }
        
        // create result image
        Image resultImage = new Image(mask, image);
        
        // add the image document to GUI
        this.gui.addNewDocument(resultImage); 

	}
}
