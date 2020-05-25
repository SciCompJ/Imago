/**
 * 
 */
package imago.plugin.plugin.crop;

import java.util.Collection;
import java.util.Iterator;

import imago.app.ImageHandle;
import imago.app.scene.ImageSerialSectionsNode;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.StackSliceViewer;
import net.sci.array.Array;
import net.sci.image.Image;

/**
 * Creates a new shape from the current selection and add it to the current
 * image handle.
 * 
 * @author David Legland
 *
 */
public class Crop3D_InterpolatePolygons implements Plugin
{
	public Crop3D_InterpolatePolygons()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
		System.out.println("crop3d - interpolate polygons");

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
        ImageSerialSectionsNode smoothNode = Crop3D.getSmoothPolygonsNode(handle);
        ImageSerialSectionsNode interpNode = Crop3D.getInterpolatedPolygonsNode(handle);

        if (smoothNode.isLeaf())
        {
            System.out.println("smooth node is empty");
            return;
        }
        
        Collection<Integer> indices = smoothNode.getSliceIndices();
        // TODO:assume indices are ordererd
        Iterator<Integer> sliceIndexIter = indices.iterator();
        if (!sliceIndexIter.hasNext())
        {
            return;
        }
        
        
        int currentSliceIndex = sliceIndexIter.next();
        while (sliceIndexIter.hasNext())
        {
            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex  <nextSliceIndex; sliceIndex++)
            {
                System.out.println("process slice " + sliceIndex);
            }
            
            
            currentSliceIndex = nextSliceIndex;
        }

        // need to call this to update items to display 
        viewer.refreshDisplay(); 
        
        viewer.repaint();
	}
}
