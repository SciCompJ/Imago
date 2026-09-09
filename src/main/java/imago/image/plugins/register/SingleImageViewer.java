/**
 * 
 */
package imago.image.plugins.register;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import imago.image.ImageDataRenderer;
import imago.image.ImageTool;
import imago.image.viewers.ImageDisplay;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;

/**
 * Displays a pair of 2D or 3D images together with some widgets for modifying
 * the view.
 * 
 * @see imago.image.viewers.PlanarImageViewer
 * @see imago.image.viewers.StackSliceViewer
 * 
 * @author David Legland
 *
 */
public class SingleImageViewer 
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
	 * The image to display.
	 */
    protected Image image;
    
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
	
    JPanel mainPanel;
    
    JScrollPane scroll;
    ImageDisplay imageDisplay;
    
    /**
     * The strategy for displaying a image.
     */
    protected ImageDataRenderer renderer;
    
    BufferedImage awtImage;

    ZoomMode zoomMode = ZoomMode.FILL;
    
    protected ImageTool currentTool = null;
    
    /**
     * The shape of the current selection, usually a polyline or a rectangle, in pixel coordinates.
     */
    protected Geometry2D selection = null;
    
    
	// ===================================================================
	// Constructor
	
    public SingleImageViewer(Image image)
	{
        this.image = image;
        if (image.getDimension() != 2) 
        {
            throw new IllegalArgumentException("Requires a planar image as input");
        }

		// initialize slicing position
        int nd = image.getDimension();
        this.slicingPosition = new int[nd];
        for (int d = 0; d < nd; d++)
        {
            this.slicingPosition[d] = (int) Math.floor(image.getSize(d) / 2);
        }
        
        // update the image to display
        this.renderer = ImageDataRenderer.createRenderer(image);
        this.awtImage = this.renderer.render(image.getData());

        setupLayout();
	}
    
    private void setupLayout()
    {
        // create the main display panel
        imageDisplay = new ImageDisplay(this.awtImage);
        
        // encapsulate into scroll panel
        scroll = new JScrollPane(this.imageDisplay);
        scroll.setBackground(Color.WHITE);
        
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.add(scroll, BorderLayout.CENTER);
    }

    public void setImage(Image image)
    {
        this.image = image;
        
        // update the image to display
        this.renderer = ImageDataRenderer.createRenderer(image);
        this.awtImage = this.renderer.render(image.getData());
    }
    
    
    public Image getImage()
    {
        return this.image;
    }
    
    
	// ===================================================================
    // General methods
	
    public ImageDisplay getImageDisplay() 
    {
        return imageDisplay;
    }
    
	/**
	 * @return the current selection, as a Geometry instance, or null if no selection exists.
	 */
	public Geometry getSelection()
	{
	    return this.selection;
	}

	/**
     * @param selection
     *            the selection of the current viewer, as an instance of
     *            Geometry2D.
     */
    public void setSelection(Geometry selection)
    {
        if (!(selection instanceof Geometry2D))
        {
            throw new RuntimeException("Selection must be an instance of Geometry2D");
        }
        
        this.selection = (Geometry2D) selection;
        this.imageDisplay.setSelection((Geometry2D) selection);
    }
    
	/**
	 * Clears the selection of the current viewer.
	 */
    public void clearSelection()
    {
        setSelection(null);
    }

	// ===================================================================
	// Setters and getters for display options
	
    public double getZoom() 
    {
        return imageDisplay.getZoom();
    }
    
    public void setZoom(double zoom) 
    {
        imageDisplay.setZoom(zoom);     
        imageDisplay.invalidate();
        this.mainPanel.validate();
        imageDisplay.updateOffset();        
    }

//    /**
//     * Computes the zoom factor that best fits the image within the limits of
//     * the panel.
//     */
//    public void setBestZoom() 
//    {
//        Dimension dim0 = scroll.getSize();
//        double ratioX = ((double) dim0.width - 5) / ((double) (refImage.getSize(0)));
//        double ratioY = ((double) dim0.height - 5) / ((double) (refImage.getSize(1)));
//        double zoom = Math.min(ratioX, ratioY);
//        setZoom(zoom);
//    }


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
    // Display methods

    public void refreshDisplay()
    {
        updateAwtImage();
        
        this.imageDisplay.setBufferedImage(this.awtImage);
        imageDisplay.updateOffset();
        this.imageDisplay.repaint();
    }

    /**
     * Updates the AWT Image displayed in the center of the ImageDisplay.
     */
    public void updateAwtImage()
    {
        this.awtImage = this.renderer.render(image.getData());
    }
    
    public void repaint()
    {
        this.mainPanel.repaint();
    }

	/**
     * @return the GUI widget associated with this viewer, usually as an
     *         instance of JPanel.
     */
	public Object getWidget()
	{
	    return this.mainPanel;
	}
}
