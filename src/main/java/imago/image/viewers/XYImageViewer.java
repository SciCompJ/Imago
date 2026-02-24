/**
 * 
 */
package imago.image.viewers;

import imago.image.ImageHandle;
import imago.image.ImageViewer;

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
}
