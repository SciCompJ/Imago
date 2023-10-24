/**
 * 
 */
package imago.gui.image.tools;

import imago.gui.image.ImageDisplay;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImagoTool;

import java.awt.Point;
import java.awt.event.MouseEvent;

import net.sci.geom.geom2d.Point2D;

/**
 * The default tool for choosing pixel on an image.
 * @author David Legland
 *
 */
public class SelectionTool extends ImagoTool {

	double x1, y1;
	double x2, y2;
	int state = 0;
	
	public SelectionTool(ImageFrame viewer, String name) {
		super(viewer, name);
	}

	/* (non-Javadoc)
	 * @see imago.gui.ImagoTool#select()
	 */
	@Override
	public void select() {
		System.out.println("choose the 'selection' tool");
	}

	/* (non-Javadoc)
	 * @see imago.gui.ImagoTool#deselect()
	 */
	@Override
	public void deselect() {
		System.out.println("deselected the 'selection' tool");
	}

	/**
	 * When the button is pressed, the current mouse position is registered, 
	 * and state of the tool is changed.
	 */
	@Override
	public void mousePressed(MouseEvent evt) {
		// Coordinate of mouse cursor
		ImageDisplay display = (ImageDisplay) evt.getSource();
		Point point = new Point(evt.getX(), evt.getY());
		Point2D pos = display.displayToImage(point);
		double x = pos.x();
		double y = pos.y();

		System.out.println("Mouse pressed at ("+ x + " ; " + y );
	}
}
