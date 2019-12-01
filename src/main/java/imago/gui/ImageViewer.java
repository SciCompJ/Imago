/**
 * 
 */
package imago.gui;


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
	
	
	// ===================================================================
	// Constructor
	
	public ImageViewer(Image image) 
	{
		this.image = image;
	}
	
	
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
