/**
 * 
 */
package imago.image;

import imago.image.ImageHandle.Event;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.VectorArray;
import net.sci.geom.Geometry;
import net.sci.image.Image;

/**
 * Displays a 2D or 3D image together with some widgets for modifying the view.
 * The type of image representation is left to the sub-classes.
 * 
 * @see imago.image.viewers.PlanarImageViewer
 * @see imago.image.viewers.StackSliceViewer
 * 
 * @author David Legland
 *
 */
public abstract class ImageViewer implements ImageHandle.Listener
{
    // ===================================================================
    // Public constants

    /**
     * Determines the strategy for displaying a vector image.
     */
    public static enum VectorImageDisplayMode
    {
        /** Display a single channel/component of the vector image. */
        CHANNEL,
        /** Display the norm of each pixel of the vector image. */
        NORM,
        /**
         * Display the maximal channel value of each pixel of the vector image.
         */
        MAX,
    }

    /**
     * The behavior of the zoom when the component is resized.
     */
    public static enum ZoomMode
    {
        FIXED, FILL
    }
    

    // ===================================================================
    // Class variables

    protected ImageHandle imageHandle;

    protected Image image;

    /**
     * An instance of Image that can be used for preview, or null of no preview
     * is made for this viewer.
     */
    protected Image previewImage = null;

    protected double zoom = 1;

    protected boolean displayImage = true;

    protected boolean displaySceneGraph = true;

    /**
     * For 3D+ images, the position of a point visible in the image, used to
     * compute slice images.
     * 
     * The first two indices correspond to the X and Y indices. The other ones
     * correspond to indices of the slice in the other dimensions (usually Z,
     * channel, and frame).
     */
    protected int[] slicingPosition;

    /**
     * The strategy for displaying a vector image.
     */
    protected VectorImageDisplayMode vectorImageDisplayMode = VectorImageDisplayMode.CHANNEL;

    /**
     * The index of the current channel, when the image data is a vector array,
     * and when the viewer displays a single channel.
     */
    protected int currentChannelIndex = 0;

    
    // ===================================================================
    // Constructor

    public ImageViewer(ImageHandle handle)
    {
        this.imageHandle = handle;
        this.image = imageHandle.getImage();

        // initialize slicing position
        int nd = image.getDimension();
        this.slicingPosition = new int[nd];
        for (int d = 0; d < nd; d++)
        {
            this.slicingPosition[d] = (int) Math.floor(image.getSize(d) / 2);
        }
        
//        this.imageHandle.addImageHandleListener(this);
    }

    
    // ===================================================================
    // General methods

    /**
     * @return the current selection, as a Geometry instance, or null if no
     *         selection exists.
     */
    public abstract Geometry getSelection();

    /**
     * Changes the current selection of the viewer.
     * 
     * @param selection
     *            an instance of Geometry representing the selection region
     *            within the viewer.
     */
    public abstract void setSelection(Geometry selection);

    /**
     * Clears the selection of the current viewer.
     */
    public void clearSelection()
    {
        setSelection(null);
    }

    
    // ===================================================================
    // General methods

    /**
     * Returns the image to display, which can be a preview image, or the
     * reference image.
     * 
     * @return the preview image if is it not null, or the image otherwise.
     */
    public Image getImageToDisplay()
    {
        if (this.previewImage != null)
        {
            return this.previewImage;
        }
        return this.image;
    }

    /**
     * @return the base image stored in this view
     */
    public Image getImage()
    {
        return this.image;
    }

    /**
     * @return the ImageHandle of the image stored within this view.
     */
    public ImageHandle getImageHandle()
    {
        return this.imageHandle;
    }

    /**
     * @return the previewImage
     */
    public Image getPreviewImage()
    {
        return previewImage;
    }

    /**
     * @param previewImage
     *            the previewImage to set
     */
    public void setPreviewImage(Image previewImage)
    {
        this.previewImage = previewImage;
    }

    public void updateSliceImage()
    {

    }

    
    // ===================================================================
    // Tool management methods

    public abstract void setCurrentTool(ImageTool tool);

    public abstract ImageTool getCurrentTool();

    
    // ===================================================================
    // Setters and getters for display options

    public double getZoom()
    {
        return this.zoom;
    }

    public void setZoom(double zoom)
    {
        this.zoom = zoom;
    }

    /**
     * @return the displayImage
     */
    public boolean isDisplayImage()
    {
        return displayImage;
    }

    /**
     * @param displayImage
     *            the displayImage to set
     */
    public void setDisplayImage(boolean displayImage)
    {
        this.displayImage = displayImage;
    }

    /**
     * @return the displaySceneGraph
     */
    public boolean isDisplaySceneGraph()
    {
        return displaySceneGraph;
    }

    /**
     * @param displaySceneGraph
     *            the displaySceneGraph to set
     */
    public void setDisplaySceneGraph(boolean displaySceneGraph)
    {
        this.displaySceneGraph = displaySceneGraph;
    }

    /**
     * Changes the current reference point for displaying a multi-dimensional
     * image.
     * 
     * @param pos
     *            the new reference position for the viewer.
     */
    public void setSlicingPosition(int[] pos)
    {
        this.slicingPosition = pos;
    }

    public void setSlicingPosition(int dim, int pos)
    {
        this.slicingPosition[dim] = pos;
    }

    /**
     * Returns the reference position for the given dimension. For example, to
     * retrieve the z-slice index:
     * 
     * <pre>{@code
     * int index = viewer.getSlicingPosition(2);
     * } </pre>
     * 
     * @param dim
     *            the dimension to consider
     * @return the reference position along the given dimension.
     */
    public int getSlicingPosition(int dim)
    {
        return this.slicingPosition[dim];
    }

    /**
     * @return the vectorImageDisplayMode
     */
    public VectorImageDisplayMode getVectorImageDisplayMode()
    {
        return vectorImageDisplayMode;
    }

    /**
     * @param vectorImageDisplayMode
     *            the vectorImageDisplayMode to set
     */
    public void setVectorImageDisplayMode(VectorImageDisplayMode vectorImageDisplayMode)
    {
        this.vectorImageDisplayMode = vectorImageDisplayMode;
    }

    /**
     * @return the currentChannelIndex
     */
    public int getCurrentChannelIndex()
    {
        return currentChannelIndex;
    }

    /**
     * @param currentChannelIndex
     *            the currentChannelIndex to set
     */
    public void setCurrentChannelIndex(int currentChannelIndex)
    {
        this.currentChannelIndex = currentChannelIndex;
    }

    /**
     * Computes the scalar array that will be displayed, based on the current
     * settings of the viewer.
     * 
     * @param array
     *            the vector array to convert
     * @return an instance of ScalarArray representing the input array.
     */
    protected ScalarArray<?> computeVectorArrayDisplay(VectorArray<?, ?> array)
    {
        switch (this.vectorImageDisplayMode)
        {
            case CHANNEL:
                return array.channel(this.currentChannelIndex);
            case NORM:
                return VectorArray.norm(array);
            case MAX:
                return VectorArray.maxNorm(array);
            default:
                throw new RuntimeException("Unknown mode for converting vector image...");
        }
    }

    // ===================================================================
    // Display management methods

    /**
     * Updates the display of the image stored within this viewer.
     * 
     * This method should be called when image values, image display range, or
     * Look-Up Table have been updated.
     * 
     * @see repaint()
     */
    public abstract void refreshDisplay();

    /**
     * @return the GUI widget associated with this viewer, usually as an
     *         instance of JPanel.
     */
    public abstract Object getWidget();

    public void repaint()
    {
        System.out.println("ImageViewer.repaint()");
    }

    public void invalidate()
    {
        System.out.println("ImageViewer.repaint()");
    }

    public void validate()
    {
        System.out.println("ImageViewer.repaint()");
    }

    // ===================================================================
    // Implementation of the ImageHandle.Listener interface

    @Override
    public void imageHandleModified(Event evt)
    {
        int code = evt.getCode();
        if ((code & ImageHandle.Event.LUT_MASK) != 0
                || (code & ImageHandle.Event.DISPLAY_RANGE_MASK) != 0
                || (code & ImageHandle.Event.IMAGE_MASK) != 0
                || (code & ImageHandle.Event.SHAPES_MASK) !=0)
        {
            refreshDisplay();
        }

        repaint();
    }
}
