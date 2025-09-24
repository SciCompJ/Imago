/**
 * 
 */
package imago.imagepair;

import javax.swing.JPanel;

import imago.image.ImageTool;
import net.sci.geom.Geometry;
import net.sci.image.Image;

/**
 * Displays a pair of 2D or 3D images together with some widgets for modifying
 * the view. The type of image representation is left to the sub-classes.
 * 
 * Assumes the two images have the same dimensions.
 * 
 * Limited to scalar images.
 * 
 * @see imago.imagepair.CompositeImagePairViewer
 * 
 * @author David Legland
 *
 */
public abstract class ImagePairViewer 
{
	// ===================================================================
	// Public constants

	/**
	 * The behavior of the zoom when the component is resized.
	 */
	public static enum ZoomMode 
	{
		FIXED,
		FILL
	}
	

	// ===================================================================
	// Class variables

//	protected ImageHandle imageHandle;
	
	/**
	 * The reference image used for computing display view port.
	 */
    protected Image refImage;
    
    /**
     * Another image, usually a moving image in a registration framework.
     */
    protected Image otherImage;
    
    protected Image compositeImage;
	
	protected double zoom = 1;

	
    /**
     * For 3D+ images, the position of a point visible in the image, used to
     * compute slice images.
     * 
     * The first two indices correspond to the X and Y indices. The other ones
     * correspond to indices of the slice in the other dimensions (usually Z,
     * channel, and frame).
     */
    protected int[] slicingPosition;
	
	
	// ===================================================================
	// Constructor
	
	public ImagePairViewer(Image refImage, Image otherImage) 
	{
        this.refImage = refImage;
        this.otherImage = otherImage;
		
		// initialize slicing position
        int nd = refImage.getDimension();
        this.slicingPosition = new int[nd];
        for (int d = 0; d < nd; d++)
        {
            this.slicingPosition[d] = (int) Math.floor(refImage.getSize(d) / 2);
        }
	}
	
	public abstract JPanel createOptionsPanel();
	

	// ===================================================================
    // General methods
	
	/**
	 * @return the current selection, as a Geometry instance, or null if no selection exists.
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
     * <pre>{@code int index = viewer.getSlicingPosition(2);} </pre>
     * 
     * @param dim
     *            the dimension to consider
     * @return the reference position along the given dimension.
     */
	public int getSlicingPosition(int dim)
	{
	    return this.slicingPosition[dim];
	}

    
    // ===================================================================
    // Display management methods

    /**
     * Updates the display of the image stored within this viewer. 
     * 
     * @see repaint()
     */
    public abstract void refreshDisplay();

	/**
     * @return the GUI widget associated with this viewer, usually as an
     *         instance of JPanel.
     */
	public abstract Object getWidget();

	
    public void setReferenceImage(Image image)
    {
        this.refImage = image;
    }

    public void setMovingImage(Image image)
    {
        this.otherImage = image;
    }
}
