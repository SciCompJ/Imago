/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.app.scene.GroupNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.image.PlanarImageViewer;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.image.Image;

/**
 * Creates a new shape from the current selection and add it to the current
 * image handle.
 * 
 * @author David Legland
 *
 */
public class ImageSelectionToSceneGraph implements FramePlugin
{
	public ImageSelectionToSceneGraph()
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
        if (!(selection instanceof Polygon2D))
        {
            System.out.println("requires selection to be a simple polygon");
            return;
        }
        
		
        // enforce counter-clockwise polygon
        Polygon2D poly = (Polygon2D) selection;
        if (poly.signedArea() < 0)
        {
        	poly = poly.complement();
        }
        
        ImageHandle handle = iframe.getImageHandle();
        Node node = new ShapeNode(poly);
        node.setName("Selection");
        ((GroupNode) handle.getRootNode()).addNode(node);
        
        piv.setSelection(null);
        
        // need to call this to update items to display 
        viewer.refreshDisplay(); 
        
        viewer.repaint();
	}
}
