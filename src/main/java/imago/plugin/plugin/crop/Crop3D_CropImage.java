/**
 * 
 */
package imago.plugin.plugin.crop;

import imago.app.ImageHandle;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ShapeNode;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.StackSliceViewer;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.Array3D;
import net.sci.geom.geom2d.polygon.LinearRing2D;
import net.sci.image.Image;

/**
 * Crop image from the set of crop polygons.
 * 
 * @author David Legland
 *
 */
public class Crop3D_CropImage implements Plugin
{
	public Crop3D_CropImage()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
		System.out.println("crop3d - crop image");

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
		
		// get input and output node references
        ImageHandle handle = iframe.getImageHandle();
        ImageSerialSectionsNode interpNode = Crop3D.getInterpolatedPolygonsNode(handle);

        if (interpNode.getSliceIndices().isEmpty())
        {
            return;
        }

        Array<?> result = process(array, interpNode);
        
        Image resImage = new Image(result, image);
        resImage.setName(image.getName() + "-crop");
		
		// add the image document to GUI
		frame.createImageFrame(resImage);
	}
	
	public <T> Array<T> process(Array<T> array, ImageSerialSectionsNode interpNode)
	{
		if (array.dimensionality() != 3)
		{
		    throw new RuntimeException("Requires an image containing 3D Array");
		}
		Array3D<T> array3d = Array3D.wrap(array);

        
        // Create empty result array
        Array3D<T> resArray = Array3D.wrap(array3d.newInstance(array.size()));
        
        // size of array
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        for (int sliceIndex : interpNode.getSliceIndices())
        {
        	System.out.println("crop slice " + sliceIndex);
        	// get 2D view on array slices
        	Array2D<T> slice = array3d.slice(sliceIndex);
            Array2D<T> resSlice = resArray.slice(sliceIndex);
        	
            // get crop polygon
            ShapeNode shapeNode = (ShapeNode) interpNode.getSliceNode(sliceIndex).children().iterator().next();
            LinearRing2D ring = (LinearRing2D) shapeNode.getGeometry();
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                	if (ring.isInside(x,  y))
                	{
                		resSlice.set(slice.get(x, y), x, y);
                	}
                }
            }
        }

        return resArray;
	}
}
