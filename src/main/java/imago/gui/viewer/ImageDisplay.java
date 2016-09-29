/**
 * 
 */
package imago.gui.viewer;

import imago.gui.ImagoDocViewer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import math.jg.geom2d.Point2D;

/**
 * A specialization of JPanel that displays a buffered AWT image.
 *  
 * Responsibilities for this class:
 * <ul>
 * <li>Containing an instance of BufferedImage</li>
 * <li>Managing the zoom</li>
 * <li>Computing the optimal display size and the component size</li>
 * <li>Computing the offset for image display (when zoom is small)</li>
 * <li>Managing coordinate changes between panel coords and image coords</li>
 * </ul>
 * 
 * @author David Legland
 *
 */
public class ImageDisplay extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// ===================================================================
	// Class variables

	BufferedImage image;
	
	double zoom = 1;
	
	int offsetX;
	int offsetY;

	
	// ===================================================================
	// Constructor
	
	public ImageDisplay(BufferedImage image) {
		this.image = image;
		this.setBackground(Color.WHITE);
	}

	// ===================================================================
	// General methods

	public BufferedImage getImage() {
		return this.image;
	}
	
	public ImagoDocViewer getViewer() {
		Container container = this.getParent();
		while (!(container instanceof ImagoDocViewer)) {
			container = container.getParent();
		}
		return (ImagoDocViewer) container;
	}
	
	// ===================================================================
	// Zoom management

	public double getZoom() {
		return this.zoom;
	}
	
	public void setZoom(double zoom) {
		this.zoom = zoom;
	}

	public BufferedImage getBufferedImage() {
		return image;
	}

	public void setBufferedImage(BufferedImage image) {
		this.image = image;
	}

	// ===================================================================
	// Display methods

	public void refreshDisplay(){
//		System.out.println("ImageDisplay: refreshDisplay");
		updateOffset();
	}
	
//	public void repaint() {
//		super.repaint();
//		System.out.println("ImageDisplay: repaint");
//	}
	
	public Point2D displayToImage(Point point) {
		double x = (point.x - this.offsetX) / zoom;
		double y = (point.y - this.offsetY) / zoom;
		return new Point2D(x, y);
	}

	/**
	 * @return the current offset no which image is displayed. Always greater
	 *         than 0 for each coordinate.
	 */
	public Point getOffset() {
		return new Point(this.offsetX, this.offsetY);
	}

	/**
	 * Compute new offset such that image is either in center position, or at
	 * left-most or top most position.
	 */
	protected void updateOffset() {
		Dimension dim0 = this.getSize();
		Dimension dim = this.getDisplaySize();

		this.offsetX = (int) Math.max(0, Math.floor((dim0.width - dim.width) * .5));
		this.offsetY = (int) Math.max(0, Math.floor((dim0.height - dim.height) * .5));
	}

	public Dimension getPreferredSize() {
		return getDisplaySize();
	}
	
	public Dimension getDisplaySize() {
		int width = (int) Math.ceil(this.image.getWidth() * zoom);
		int height = (int) Math.ceil(this.image.getHeight() * zoom);
		return new Dimension(width, height);
	}
	
	public void paintComponent(Graphics g) {
//		System.out.println("ImageDisplay.paintComponent()");
		Dimension dim = this.getDisplaySize();
		g.drawImage(this.image, offsetX, offsetY, dim.width, dim.height, null);
		
//		Graphics2D g2 = (Graphics2D) g;
//		g2.translate(offsetX, offsetY);
//		//g2.scale(zoom, zoom);
//		g2.setColor(new Color(255, 0, 0));
//		Shape rect = new Rectangle2D.Double(20 * zoom, 30 * zoom, 101 * zoom, 121 * zoom); 
//		g2.draw(rect);
	}

}
