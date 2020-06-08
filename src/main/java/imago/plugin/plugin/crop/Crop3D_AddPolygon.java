/**
 * 
 */
package imago.plugin.plugin.crop;

import java.util.Locale;

import imago.app.ImageHandle;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.StackSliceViewer;
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
public class Crop3D_AddPolygon implements Plugin
{
	public Crop3D_AddPolygon()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
		System.out.println("add polygon to crop3d");

		// Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
                
        ImageViewer viewer = iframe.getImageView();
        if (!(viewer instanceof StackSliceViewer))
        {
            System.out.println("requires an instance of stack slice viewer");
            return;
        }

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		if (array.dimensionality() != 3)
		{
		    throw new RuntimeException("Requires an image containing 3D Array");
		}

		
		StackSliceViewer piv = (StackSliceViewer) viewer;
		int sliceIndex = piv.getSliceIndex();
		
        Geometry2D selection = piv.getSelection();
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

        // Create a new LinearRing shape from the boundary of the polygon 
        Node shapeNode = new ShapeNode(poly.rings().iterator().next());
        shapeNode.setName("Polygon");
        

        ImageHandle handle = iframe.getImageHandle();
        ImageSerialSectionsNode polyNode = Crop3D.getPolygonsNode(handle);
        
        int nDigits = (int) Math.ceil(Math.log10(array.size(2)));
        String sliceName = String.format(Locale.US, "slice%0" + nDigits + "d", sliceIndex);
        
        ImageSliceNode sliceNode;
        if (polyNode.hasSliceNode(sliceIndex))
        {
        	 sliceNode = (ImageSliceNode) polyNode.getSliceNode(sliceIndex);
        }
        else
        {
        	sliceNode = new ImageSliceNode(sliceName, sliceIndex);
        	polyNode.addSliceNode(sliceNode);
        }
        
        sliceNode.clear();
        sliceNode.addNode(shapeNode);
        
        // clear selection of current viewer
        piv.setSelection(null);
        
        // need to call this to update items to display 
        viewer.refreshDisplay(); 
        
        viewer.repaint();
	}
}
