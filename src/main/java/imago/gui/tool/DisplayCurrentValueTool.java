/**
 * 
 */
package imago.gui.tool;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Locale;

import imago.gui.ImageViewer;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoTool;
import imago.gui.panel.StatusBar;
import imago.gui.viewer.ImageDisplay;
import imago.gui.viewer.StackSliceViewer;
import net.sci.array.Array;
import net.sci.array.data.Array2D;
import net.sci.array.data.Array3D;
import net.sci.array.data.IntArray;
import net.sci.array.data.ScalarArray;
import net.sci.array.data.VectorArray;
import net.sci.array.data.color.RGB8Array;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class DisplayCurrentValueTool extends ImagoTool
{

	public DisplayCurrentValueTool(ImagoDocViewer viewer, String name)
	{
		super(viewer, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see imago.gui.ImagoTool#select()
	 */
	@Override
	public void select()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see imago.gui.ImagoTool#deselect()
	 */
	@Override
	public void deselect()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent evt)
	{
		// Extract image
		ImageViewer imageView = viewer.getImageView();
		Image image = imageView.getImage();
		Array<?> img = image.getData();

		// Coordinate of mouse cursor in panel corodinates
		ImageDisplay display = (ImageDisplay) evt.getSource();
		Point point = new Point(evt.getX(), evt.getY());
		
		// convert coordinates to image reference system
		Point2D pos = display.displayToImage(point);
		double x = pos.getX();
		double y = pos.getY();

		// convert to integer indices
        int indx = (int) Math.round(x);
        int indy = (int) Math.round(y);
		int indz = 0;

        // Check mouse cursor is in image bounds
        if (indx < 0 || indx > image.getSize(0) - 1)
            return;
        if (indy < 0 || indy > image.getSize(1) - 1)
            return;

		// Create string for representing position
		String posString = null;
		if (img instanceof Array2D)
		{
            String format = "pos=(%.2f, %.2f)";
            posString = String.format(Locale.ENGLISH, format, x, y);

		}
		else if (img instanceof Array3D)
		{
			indz = ((StackSliceViewer) imageView).getSliceIndex();
			String format = "pos=(%.2f, %.2f, %d)";
			posString = String.format(Locale.ENGLISH, format, x, y, indz);
		}

		// create the position array
		int[] inds = new int[img.dimensionality()];
		inds[0] = indx;
		inds[1] = indy;
		if (img instanceof Array3D)
		{
			inds[2] = indz;
		}

		// Create string for representing image value
		String valueString = null;
		if (img instanceof ScalarArray)
		{
			if (img instanceof IntArray)
			{
				// Case of integer scalar images
				int val = ((IntArray<?>) img).getInt(inds);
				valueString = String.format("val=%d", val);

			} 
			else
			{
				// Case of floating-point scalar images
				double val = ((ScalarArray<?>) img).getValue(inds);
				valueString = String.format(Locale.US, "val=%g", val);
			}

		} 
		else if (img instanceof RGB8Array)
		{
			// Case of RGB color images
			int[] rgb = ((RGB8Array) img).get(inds).getSamples();
			valueString = String.format("rgb=[%d,%d,%d]", rgb[0], rgb[1], rgb[2]);

		} 
		else if (img instanceof VectorArray)
		{
			// in case of Vector array, compute the norm of the pixel for display
			double[] values = ((VectorArray<?>) img).getValues(inds);
			double norm = 0;
			for (int c = 0; c < values.length; c++)
				norm += values[c] * values[c];
			norm = Math.sqrt(norm);
			valueString = String.format(Locale.US, "norm=%g", norm);
		} 
		else
		{
			throw new IllegalArgumentException(
					"Unable to manage image of class " + img.getClass());
		}

		// Concatenate the information and update status bar
		StatusBar statusBar = viewer.getStatusBar();
		String format = "%1$s %2$s";
		String label = String.format(format, posString, valueString);
		statusBar.setCursorLabel(label);
	}
}
