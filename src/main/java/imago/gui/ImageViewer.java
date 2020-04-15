/**
 * 
 */
package imago.gui;


import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;


/**
 * Displays a 2D or 3D image together with some widgets for modifying the view.
 * The type of image representation is left to the sub-classes.
 * 
 * @see imago.gui.viewer.PlanarImageViewer
 * @see imago.gui.viewer.StackSliceViewer
 * 
 * @author David Legland
 *
 */
public abstract class ImageViewer 
{
	// ===================================================================
	// Class variables

	/**
	 * The behavior of the zoom when the component is resized.
	 */
	public static enum ZoomMode 
	{
		FIXED,
		FILL
	}
	
	protected Image image;
	
	/**
	 * An instance of Image that can be used for preview, or null of no preview
	 * is made for this viewer.
	 */
	protected Image previewImage = null;
	

	protected double zoom = 1;
	
    /**
     * For 3D+ images, the position of a point visible in the image, used to compute slice images.
     * 
     * The first two indices correspond to the X and Y indices. The other ones
     * correspond to indices of the slice in the other dimensions.
     */
	protected int[] slicingPosition;
	
	
	// ===================================================================
	// Constructor
	
	public ImageViewer(Image image) 
	{
		this.image = image;
		
		// initialize slicing position
        int nd = image.getDimension();
        this.slicingPosition = new int[nd];
        for (int d = 0; d < nd; d++)
        {
            this.slicingPosition[d] = (int) Math.floor(image.getSize(d) / 2);
        }
	}
	
    // ===================================================================
    // General methods

	public abstract Geometry2D getSelection();

	public abstract void setSelection(Geometry2D shape);

	
	// ===================================================================
	// General methods

	/**
	 * Returns the preview image if is it not null, or the image otherwise.
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
	 * @return the previewImage
	 */
	public Image getPreviewImage()
	{
		return previewImage;
	}


	/**
	 * @param previewImage the previewImage to set
	 */
	public void setPreviewImage(Image previewImage)
	{
		this.previewImage = previewImage;
	}

	public void updateSliceImage()
	{
	    
	}
	
//	/**
//	 * Returns the instance of ImagoDocViewer that contains this Image view,
//	 * or null if no one is found.
//	 */
//	public ImagoDocViewer getViewer() 
//	{
//		Container container = this.getParent();
//		while (!(container instanceof ImagoDocViewer) && container != null) 
//		{
//			container = container.getParent();
//		}
//		
//		return (ImagoDocViewer) container;
//	}	

	
	// ===================================================================
	// Tool management methods
	
	public abstract void setCurrentTool(ImagoTool tool);

	public abstract ImagoTool getCurrentTool();
	
	
	// ===================================================================
	// Display management methods
	
	public double getZoom()
	{
		return this.zoom;
	}
	
	public void setZoom(double zoom) 
	{
		this.zoom = zoom;
	}
	
	public void setSlicingPosition(int[] pos)
	{
	    this.slicingPosition = pos;
	}

    public void setSlicingPosition(int dim, int pos)
    {
        this.slicingPosition[dim] = pos;
    }

	public int getSlicingPosition(int dim)
	{
	    return this.slicingPosition[dim];
	}

	
	public abstract void refreshDisplay();
//	{
////		System.out.println("refresh display");
//	}

	/**
	 * @return the GUI widget associated with this viewer, usually as an instance of JPanel.
	 */
	public abstract Object getWidget();
	
	public void repaint() 
	{
		System.out.println("repaint Image Viewer");
	}

	public void invalidate() 
    {
        System.out.println("invalidate Image Viewer");
    }

	public void validate() 
    {
        System.out.println("validate Image Viewer");
    }
}
