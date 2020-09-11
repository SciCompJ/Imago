/**
 * 
 */
package imago.gui.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import imago.app.ImageHandle;
import imago.app.scene.GroupNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.shape.Shape;
import imago.gui.ImageViewer;
import imago.gui.ImagoTool;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.vector.VectorArray;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.BufferedImageUtils;
import net.sci.image.ColorMap;
import net.sci.image.ColorMaps;
import net.sci.image.Image;


/**
 * A Panel that displays the current image in the upper left corner.
 * 
 * @author David Legland
 *
 */
public class PlanarImageViewer extends ImageViewer implements ComponentListener 
{
	// ===================================================================
	// Class variables
	
    JPanel panel;
    
	JScrollPane scroll;
	ImageDisplay imageDisplay;
	
	BufferedImage awtImage;

	ZoomMode zoomMode = ZoomMode.FILL;
	
	protected ImagoTool currentTool = null;
	
	/**
	 * The shape of the current selection, usually a polyline or a rectangle, in pixels coordinates.
	 */
	protected Geometry2D selection = null;
	
	
	// ===================================================================
	// Constructor

	public PlanarImageViewer(ImageHandle handle)
	{
		super(handle);
		if (image.getDimension() != 2) 
		{
			throw new IllegalArgumentException("Requires a planar image as input");
		}
		
		this.awtImage = BufferedImageUtils.createAwtImage(image);
		
		setupLayout();
	}

	private void setupLayout()
	{
		// create the main display panel
		imageDisplay = new ImageDisplay(this.awtImage);
		
		// encapsulate into scroll panel
		scroll = new JScrollPane(this.imageDisplay);
		scroll.setBackground(Color.WHITE);
		
		this.panel = new JPanel();
		this.panel.setLayout(new BorderLayout());
		this.panel.add(scroll, BorderLayout.CENTER);

		this.panel.setBackground(Color.WHITE);
		
		// Add listeners
		// (mouse listeners are added from ImageFrame, when component is build)
		this.panel.addComponentListener(this);
	}
	
	// ===================================================================
	// General methods

	public ImageDisplay getImageDisplay() 
	{
		return imageDisplay;
	}

	public Geometry2D getSelection()
	{
		return this.selection;
	}
	
	public void setSelection(Geometry2D shape)
	{
		this.selection = shape;
		this.imageDisplay.setSelection(shape);
	}
	
	// ===================================================================
	// Zoom management

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

	// ===================================================================
	// tool management

	@Override
	public ImagoTool getCurrentTool() 
	{
		return currentTool;
	}

	@Override
	public void setCurrentTool(ImagoTool tool) 
	{
		// remove previous tool
		if (currentTool != null) 
		{
			imageDisplay.removeMouseListener(currentTool);
			imageDisplay.removeMouseMotionListener(currentTool);
		}
		
		// setup current tool
		currentTool = tool;
		if (currentTool != null) 
		{
			imageDisplay.addMouseListener(currentTool);
			imageDisplay.addMouseMotionListener(currentTool);
		}
	}


	// ===================================================================
	// Display methods

	public void refreshDisplay()
	{
//		imageDisplay.updateOffset();
//		Image image = this.getImageToDisplay();
//		this.awtImage = BufferedImageUtils.createAwtImage(image);
	    updateAwtImage();
	    
		this.imageDisplay.setBufferedImage(this.awtImage);
		imageDisplay.updateOffset();
		this.imageDisplay.repaint();
		
		refreshSceneGraph();
	}

	private void refreshSceneGraph()
	{
		if (this.imageHandle == null)
		{
			return;
		}
		
		this.imageDisplay.clearSceneGraphItems();
		if (!this.displaySceneGraph)
		{
			return;
		}
		
		Node rootNode = this.imageHandle.getRootNode();
		if (rootNode == null)
		{
			return;
		}
		
		displaySceneGraphNode(rootNode);
	}
	
	private void displaySceneGraphNode(Node node)
	{
		if (!node.isVisible())
		{
			return;
		}
		
		if (node instanceof GroupNode)
		{
			for (Node child : node.children())
			{
				displaySceneGraphNode(child);
			}
		}
		else if (node instanceof ShapeNode)
		{
			Geometry geom = ((ShapeNode) node).getGeometry();
			if (geom instanceof Geometry2D)
			{
				Shape shape = new Shape(geom, ((ShapeNode) node).getStyle());
				this.imageDisplay.addSceneGraphItem(shape);
			}
		}
		else
		{
			return;
		}
	}
	

	/**
	 * Updates the AWT Image displayed in the center of the ImageDisplay.
	 */
	public void updateAwtImage()
	{
        Image image = this.getImageToDisplay();
        if (image.isScalarImage() || image.isColorImage())
        {
            this.awtImage = BufferedImageUtils.createAwtImage(image);
        }
        else if (image.isColorImage())
        {
            this.awtImage = BufferedImageUtils.createAwtImage(image);
        }
        else
        {
            // For vector images, create a new view depending on current settings.
            ScalarArray2D<?> scalarDisplay = ScalarArray2D.wrap(computeVectorArrayDisplay((VectorArray<?>) image.getData()));
            // extract LUT from image, or create one otherwise
            ColorMap lut = image.getDisplaySettings().getColorMap();
            if (lut == null)
            {
                lut = ColorMaps.GRAY.createColorMap(256); 
            }
            double[] valueRange = scalarDisplay.finiteValueRange();
            double[] displayRange = new double[]{0.0, valueRange[1]};
            this.awtImage = BufferedImageUtils.createAwtImage(scalarDisplay, displayRange, lut);
        }
	}
	
	public void repaint()
	{
	    this.panel.repaint();
	    if (this.selection != null)
	    {
	        
	    }
	}


    // ===================================================================
	// Implementation of Component Listener

	@Override
	public void componentHidden(ComponentEvent evt)
	{
	}

	@Override
	public void componentMoved(ComponentEvent evt)
	{
	}

	@Override
	public void componentResized(ComponentEvent evt) 
	{
//		System.out.println("Planar Image View resized");
		if (zoomMode == ZoomMode.FILL) {
			setBestZoom();
			refreshDisplay();
		}
//		this.scroll.setSize(this.getSize());
	}

	@Override
	public void componentShown(ComponentEvent evt)
	{
	}

    @Override
    public Object getWidget()
    {
        return this.panel;
    }
}
