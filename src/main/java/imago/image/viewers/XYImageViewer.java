/**
 * 
 */
package imago.image.viewers;

import imago.image.ImageHandle;
import imago.image.ImageViewer;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.Array3D;
import net.sci.array.shape.Slice;

/**
 * A specialization of ImageViewer that displays an image as a 2D planar view
 * along the X and Y axes. This is the parent class of 2D image viewers, but
 * also of XY slice viewer of 3D or multidimensional images.
 * 
 * @see PlanarImageViewer
 * @see StackSliceViewer
 * @see Image5DXYSliceViewer
 * 
 */
public abstract class XYImageViewer extends ImageViewer
{
    public XYImageViewer(ImageHandle handle)
    {
        super(handle);
    }

    /**
     * Returns the display used for drawing the image (or the image slice in the
     * case of a multidimensional image).
     * 
     * @return the display used for drawing the image
     */
    public abstract ImageDisplay getImageDisplay();
    
    /**
     * Returns the current slice of the reference image as a 2D Array. Tries to
     * return a view when possible.
     * 
     * @return the 2D array corresponding to the current slice.
     */
    public Array2D<?> getCurrentSlice()
    {
        return getXYSlice(image.getData());
    }
    
    /**
     * Returns a view on the current slice of the current display image as a 2D Array.
     * 
     * @return the 2D array corresponding to the current slice of the display
     *         image.
     */
    public Array2D<?> getCurrentDisplaySlice()
    {
        return getXYSlice(getImageToDisplay().getData());
    }
    
    private Array2D<?> getXYSlice(Array<?> array)
    {
        return switch (array.dimensionality())
        {
            case 2 -> Array2D.wrap(array);
            case 3 -> Array3D.wrap(array).slice(this.slicingPosition[2]);
            default -> Array2D
                    .wrap(new Slice(new int[] { 0, 1 }, this.slicingPosition).createView(array));
        };
    }
}
