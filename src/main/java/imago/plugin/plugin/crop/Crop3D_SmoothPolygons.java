/**
 * 
 */
package imago.plugin.plugin.crop;

import java.awt.Color;
import java.util.Locale;

import imago.app.ImageHandle;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.shape.Shape;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.StackSliceViewer;
import net.sci.array.Array;
import net.sci.geom.geom2d.polygon.LinearRing2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.image.Image;

/**
 * Creates a new shape from the current selection and add it to the current
 * image handle.
 * 
 * @author David Legland
 *
 */
public class Crop3D_SmoothPolygons implements Plugin
{
	public Crop3D_SmoothPolygons()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
		System.out.println("crop3d - smoth polygons");

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
		
		// number of digits for creating slice names
		int nDigits = (int) Math.ceil(Math.log10(array.size(2)));
        
		if (array.dimensionality() != 3)
		{
		    throw new RuntimeException("Requires an image containing 3D Array");
		}

		// get input and output node references
        ImageHandle handle = iframe.getImageHandle();
        ImageSerialSectionsNode polyNode = Crop3D.getPolygonsNode(handle); 
        ImageSerialSectionsNode smoothNode = Crop3D.getSmoothPolygonsNode(handle);

        // clear output nodes
        smoothNode.clear();
        
        // iterate over polygons to create a smoothed version
        for (ImageSliceNode sliceNode : polyNode.children())
        {
            int sliceIndex = sliceNode.getSliceIndex(); 

            ShapeNode shapeNode = (ShapeNode) sliceNode.children().iterator().next();
            Polygon2D poly = (Polygon2D) shapeNode.getShape().getGeometry();

            LinearRing2D ring = poly.rings().iterator().next();
            LinearRing2D ring2 = ring.resampleBySpacing(2.0); // every 2 pixels

            Shape shape = new Shape(ring2);
            shape.setColor(Color.GREEN);
            shape.setLineWidth(0.5);
            
            Node shapeNode2 = new ShapeNode(shape);
            String sliceName = String.format(Locale.US, "smooth%0" + nDigits + "d", sliceIndex);
            shapeNode2.setName(sliceName);

            // create the slice for smooth version
            ImageSliceNode sliceNode2 = new ImageSliceNode(sliceName, sliceIndex);
            smoothNode.addSliceNode(sliceNode2);
            
            sliceNode2.addNode(shapeNode2);
        }

        // need to call this to update items to display 
        viewer.refreshDisplay(); 
        
        viewer.repaint();
	}
}
