/**
 * 
 */
package imago.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sci.geom.Geometry;
import net.sci.image.Image;
import net.sci.image.shape.ImageSlicer;


/**
 * A Panel that displays a single (XY)-slice of a multi-dimensional image.
 * 
 * @author David Legland
 *
 */
public class Image5DXYSliceViewer extends ImageViewer implements ChangeListener, ActionListener, ComponentListener 
{	
	// ===================================================================
	// Class variables

	JPanel panel;
	
	BufferedImage awtImage;

	ZoomMode zoomMode = ZoomMode.FILL;
	
	// GUI handles
	JScrollPane scroll;
	
	/**
	 * The panel used to display the image.
	 */
	ImageDisplay imageDisplay;
	
	/**
     * The shape of the current selection, usually a polyline or a rectangle, in pixels coordinates.
     */
    protected Geometry selection = null;

	protected ImageTool currentTool = null;

	
	// ===================================================================
	// Constructors

	public Image5DXYSliceViewer(ImageHandle handle) 
	{
		super(handle);
		
		recomputeAwtImage();
        setupLayout();
	}
	
	private void setupLayout() 
	{
	    this.panel = new JPanel();
	    this.panel.setBackground(Color.WHITE);
	    
		// create the main display panel
		this.imageDisplay = new ImageDisplay(awtImage);
		
		// encapsulate the display into a scroll panel
		scroll = new JScrollPane(this.imageDisplay);
		scroll.setBackground(Color.WHITE);

		// Setup the general layout
		this.panel.setLayout(new BorderLayout());
		this.panel.add(scroll, BorderLayout.CENTER);
		
		// Add listeners
		this.panel.addComponentListener(this);
	}
	
	// ===================================================================
	// General methods

	public ImageDisplay getImageDisplay() 
	{
		return imageDisplay;
	}
	
	
    // ===================================================================
    // Selection management

	public Geometry getSelection()
    {
        return this.selection;
    }

    public void setSelection(Geometry selection)
    {
        this.selection = selection;
    }
	    
	// ===================================================================
	// Display methods

	public double getZoom() 
	{
		return imageDisplay.getZoom();
	}
	
	public void setZoom(double zoom) 
	{
		imageDisplay.setZoom(zoom);		
		imageDisplay.invalidate();
		this.panel.validate();
		imageDisplay.updateOffset();		
	}

	/**
	 * Computes the zoom factor that best fits the image within the limits of
	 * the panel.
	 */
	public void setBestZoom() 
	{
		Dimension dim0 = scroll.getSize();
		double ratioX = ((double) dim0.width - 5) / ((double) (image.getSize(0)));
		double ratioY = ((double) dim0.height - 5) / ((double) (image.getSize(1)));
		double zoom = Math.min(ratioX, ratioY);
		setZoom(zoom);
	}

	/**
	 * Recompute the AWT slice image from current index.
	 */
	public void updateSliceImage() 
	{
//	    System.out.println("update slice image");
	    recomputeAwtImage();
        imageDisplay.setBufferedImage(awtImage);
	}

	// ===================================================================
	// tool management

	@Override
	public ImageTool getCurrentTool() 
	{
		return currentTool;
	}

	@Override
	public void setCurrentTool(ImageTool tool) 
	{
		if (currentTool != null)
		{
			imageDisplay.removeMouseListener(currentTool);
			imageDisplay.removeMouseMotionListener(currentTool);
		}
		currentTool = tool;
		
		if (currentTool != null)
		{
			imageDisplay.addMouseListener(currentTool);
			imageDisplay.addMouseMotionListener(currentTool);
		}
	}

    public void refreshDisplay()
    {
        imageDisplay.updateOffset();
//        Image image = this.getImageToDisplay();
////        System.out.println("refresh StackSLiceViewer display");
//        
//        this.awtImage = BufferedImageUtils.createAwtImage(image, this.zSliceIndex);
        recomputeAwtImage();
        this.imageDisplay.setBufferedImage(this.awtImage);
        this.imageDisplay.repaint();
    }

    private void recomputeAwtImage()
    {
        Image image = this.getImageToDisplay();
        Image slice = ImageSlicer.slice2d(image, 0, 1, this.slicingPosition);
        this.awtImage = image.getType().createAwtImage(slice);
    }
    
    
    // ===================================================================
	// Implementation of StateListener interface

	@Override
	public void stateChanged(ChangeEvent evt)
	{
		updateSliceImage();
		this.panel.repaint();
	}

	// ===================================================================
	// Implementation of ActionListener interface

	public void actionPerformed(ActionEvent evt) 
	{
		updateSliceImage();
		this.panel.repaint();
	}
	
	// ===================================================================
	// Implementation of Component Listener

	@Override
	public void componentHidden(ComponentEvent evt)	{}

	@Override
	public void componentMoved(ComponentEvent evt) {}

	@Override
	public void componentResized(ComponentEvent evt) 
	{
		if (zoomMode == ZoomMode.FILL) 
		{
			setBestZoom();
			refreshDisplay();
		}
	}

	@Override
	public void componentShown(ComponentEvent evt) {}

    @Override
    public Object getWidget()
    {
        return this.panel;
    }
}
