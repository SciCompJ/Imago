/**
 * 
 */
package imago.gui.viewer;

import imago.gui.ImageUtils;
import imago.gui.ImageViewer;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoTool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JScrollPane;

import net.sci.image.Image;
import math.jg.geom2d.Shape2D;
import math.jg.geom2d.line.LineSegment2D;


/**
 * A Panel that displays the current image in the upper left corner.
 * 
 * @author David Legland
 *
 */
public class PlanarImageViewer extends ImageViewer implements ComponentListener {

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
	protected Shape2D selection = null;
	
	
	
	// ===================================================================
	// Constructor

	public PlanarImageViewer(Image image) {
		super(image);
		if (image.getDimension() != 2) 
		{
			throw new IllegalArgumentException("Requires a planar image as input");
		}
		
		this.awtImage = ImageUtils.createAwtImage(image, 0);
		this.selection = new LineSegment2D(10, 10, 210, 180);
		
		setupLayout();
	}

	private void setupLayout() {
		// create the main display panel
		imageDisplay = new ImageDisplay(this.awtImage);
		
		// encapsulate into scroll panel
		scroll = new JScrollPane(this.imageDisplay);
		scroll.setBackground(Color.WHITE);
		
		this.setLayout(new BorderLayout());
		this.add(scroll, BorderLayout.CENTER);

		this.setBackground(Color.WHITE);
		
//		// Add listeners
//		ImagoTool tool = new DisplayCurrentValueTool(this.gui, "showValue");
//		this.display.addMouseMotionListener(tool);
		this.addComponentListener(this);
	}
	
	// ===================================================================
	// General methods

	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	public Shape2D getSelection() {
		return this.selection;
	}
	
	public void setSelection(Shape2D shape) {
		this.selection = shape;
	}
	
	// ===================================================================
	// Zoom management

	public double getZoom() {
		return imageDisplay.getZoom();
	}
	
	public void setZoom(double zoom) {
		imageDisplay.setZoom(zoom);		
		imageDisplay.invalidate();
		validate();
		imageDisplay.updateOffset();		
	}

	/**
	 * Computes the zoom factor that best fits the image within the limits of
	 * the panel.
	 */
	public void setBestZoom() {
		Dimension dim0 = scroll.getSize();
		double ratioX = ((double) dim0.width - 5) / ((double) (image.getSize(0)));
		double ratioY = ((double) dim0.height - 5) / ((double) (image.getSize(1)));
		double zoom = Math.min(ratioX, ratioY);
		setZoom(zoom);
	}

	// ===================================================================
	// tool management

	@Override
	public ImagoTool getCurrentTool() {
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

	public void refreshDisplay() {
		imageDisplay.updateOffset();
		this.awtImage = ImageUtils.createAwtImage(image, 0);
		this.imageDisplay.setBufferedImage(this.awtImage);
		this.imageDisplay.repaint();
	}
	
	public void repaint() {
//		System.out.println("PlanarImageViewer.repaint");
		super.repaint();
	}
	
//	public void paintComponent(Graphics g) {
//		System.out.println("PlanarImageViewer.paintComponent");
//		super.paintComponent(g);
//		if (this.selection != null && imageDisplay != null) {
//			System.out.println("draw selection");
//			Graphics2D g2 = (Graphics2D) imageDisplay.getGraphics();
//			if (g2 != null) {
//				g2.setColor(Color.BLUE);
//				this.selection.draw(g2);
//			}
//		}
//	}

	public ImagoDocViewer getViewer() {
		Container container = this.getParent();
		while (!(container instanceof ImagoDocViewer)) {
			container = container.getParent();
		}
		return (ImagoDocViewer) container;
	}

//	public Dimension getPreferredSize() {
//		return imageDisplay.getPreferredSize();
//	}

	// ===================================================================
	// Implementation of Component Listener

	@Override
	public void componentHidden(ComponentEvent evt) {
	}

	@Override
	public void componentMoved(ComponentEvent evt) {
	}

	@Override
	public void componentResized(ComponentEvent evt) {
//		System.out.println("Planar Image View resized");
		if (zoomMode == ZoomMode.FILL) {
			setBestZoom();
			refreshDisplay();
		}
//		this.scroll.setSize(this.getSize());
	}

	@Override
	public void componentShown(ComponentEvent evt) {
	}

}
