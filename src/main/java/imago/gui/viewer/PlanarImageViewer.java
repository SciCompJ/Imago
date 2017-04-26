/**
 * 
 */
package imago.gui.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JScrollPane;

import imago.gui.ImageUtils;
import imago.gui.ImageViewer;
import imago.gui.ImagoTool;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.line.LineSegment2D;
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
	// static class variables

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// ===================================================================
	// Class variables
	
	JScrollPane scroll;
	ImageDisplay imageDisplay;
	
	BufferedImage awtImage;

	ZoomMode zoomMode = ZoomMode.FILL;
	
	protected ImagoTool currentTool = null;
	
	/**
	 * The shape of the current selection, usually a polyline or a rectangle
	 */
	protected Geometry2D selection = null;
	
	
	// ===================================================================
	// Constructor

	public PlanarImageViewer(Image image)
	{
		super(image);
		if (image.getDimension() != 2) 
		{
			throw new IllegalArgumentException("Requires a planar image as input");
		}
		
		this.awtImage = ImageUtils.createAwtImage(image, 0);
		
		setupLayout();
	}

	private void setupLayout()
	{
		// create the main display panel
		imageDisplay = new ImageDisplay(this.awtImage);
		
		// encapsulate into scroll panel
		scroll = new JScrollPane(this.imageDisplay);
		scroll.setBackground(Color.WHITE);
		
		this.setLayout(new BorderLayout());
		this.add(scroll, BorderLayout.CENTER);

		this.setBackground(Color.WHITE);
		
		// Add listeners
		// (mouse listeners are added from ImagoDocViewer, when component is build)
		this.addComponentListener(this);
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
		validate();
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
		imageDisplay.updateOffset();
		Image image = this.getImageToDisplay();
		
		this.awtImage = ImageUtils.createAwtImage(image, 0);
		this.imageDisplay.setBufferedImage(this.awtImage);
		this.imageDisplay.repaint();
	}
	
	public void repaint()
	{
		super.repaint();
		System.out.println("repaint");
		if (this.imageDisplay != null)
		{
		    paintSelection(this.imageDisplay.getGraphics());
		}
	}

    private void paintSelection(Graphics g)
    {
        // basic check to avoid errors
        if (this.selection == null)
        {
            return;            
        }
     
        System.out.println("paint selection");
        
        // convert to Graphics2D to have more drawing possibilities
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.YELLOW);
        
        if (this.selection instanceof Point2D)
        {
            Point2D point = (Point2D) this.selection;
            point = this.imageDisplay.imageToDisplay(point);
            int x = (int) point.getX();
            int y = (int) point.getY();
//                g2.fillOval(x-2, y-2, 5, 5);
            g2.drawLine(x-2, y, x+2, y);
            g2.drawLine(x, y-2, x, y+2);
        }
        else if (this.selection instanceof LineSegment2D)
        {
            LineSegment2D line = (LineSegment2D) this.selection;
            Point2D p1 = this.imageDisplay.imageToDisplay(line.getP1());
            int x1 = (int) p1.getX();
            int y1 = (int) p1.getY();
            Point2D p2 = this.imageDisplay.imageToDisplay(line.getP2());
            int x2 = (int) p2.getX();
            int y2 = (int) p2.getY();
            g2.drawLine(x1, y1, x2, y2);
        }
        else
        {
            System.out.println("can not handle geometry of class: " + selection.getClass());
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
}
