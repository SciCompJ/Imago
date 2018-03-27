/**
 * 
 */
package imago.gui.action.edit;

import imago.app.ImagoDoc;
import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.viewer.PlanarImageViewer;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.data.scalar2d.Float32Array2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;

/**
 * Convert the selection of the current image into a new scalar image containing
 * the distance to current selection.
 * 
 * @author David Legland
 *
 */
public class ImageSelectionToDistanceMapAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageSelectionToDistanceMapAction(ImagoFrame frame, String name)
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
		System.out.println("selection to distance map");

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

        // create array for result
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        Float32Array2D distMap = Float32Array2D.create(sizeX, sizeY);
        
        // iterate over output pixels 
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                distMap.setValue(x, y, selection.distance(new Point2D(x, y)));
            }
        }
        
        // create result image
        Image resultImage = new Image(distMap, image);
        
        // add the image document to GUI
        this.gui.addNewDocument(resultImage); 

	}
}
