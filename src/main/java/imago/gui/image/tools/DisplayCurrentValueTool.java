/**
 * 
 */
package imago.gui.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Locale;

import imago.gui.image.ImageDisplay;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.image.ImageTool;
import imago.gui.panels.StatusBar;
import net.sci.array.Array;
import net.sci.array.color.RGB16Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.IntArray;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.VectorArray;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class DisplayCurrentValueTool extends ImageTool
{

	public DisplayCurrentValueTool(ImageFrame viewer, String name)
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
		ImageViewer imageView = frame.getImageViewer();
		Image image = imageView.getImage();
		Array<?> array = image.getData();

		// Coordinate of mouse cursor in panel coordinates
		ImageDisplay display = (ImageDisplay) evt.getSource();
		Point point = new Point(evt.getX(), evt.getY());
		
		// convert coordinates to image reference system
		Point2D pos2d = display.displayToImage(point);
		double x = pos2d.x();
		double y = pos2d.y();

		// convert to integer indices
        int indx = (int) Math.round(x);
        int indy = (int) Math.round(y);
		int indz = 0;

        // Check mouse cursor is in image bounds
        if (indx < 0 || indx > image.getSize(0) - 1)
            return;
        if (indy < 0 || indy > image.getSize(1) - 1)
            return;

		// String patterns for representing position
        String floatFormat = "%.4g";
		String posString = "";
		
        // create the position array
		int nd = array.dimensionality();
        int[] pos = new int[nd];

        // populate the position array depending on array dimensionality
        pos[0] = indx;
        pos[1] = indy;

        if (nd == 2)
		{
//            String format = "pos=(" + floatFormat + ", " + floatFormat + ")";
            String format = String.format("pos=(%s, %s)", floatFormat, floatFormat);
            posString = String.format(Locale.ENGLISH, format, x, y);
		}
		else if (nd == 3)
		{
//		    if (imageView instanceof StackSliceViewer)
//		    {
    			indz = imageView.getSlicingPosition(2);
                pos[2] = indz;

//    			String format = "pos=(%g, %g, %d)";
    			String format = String.format("pos=(%s, %s, %s)", floatFormat, floatFormat, "%d");
    			posString = String.format(Locale.ENGLISH, format, x, y, indz);
//		    }
		}

		// Create string for representing image value
		String valueString = createValueString(array, pos);

		// Concatenate the information and update status bar
		StatusBar statusBar = frame.getStatusBar();
		String format = "%1$s %2$s";
		String label = String.format(format, posString, valueString);
		statusBar.setCursorLabel(label);
	}

	/**
     * Creates string for representing image value at a given position
     *
     * @param array
     *            the array containing image values
     * @param pos
     *            the position
     * @return a string representing the value
     */
	private static final String createValueString(Array<?> array, int[] pos)
	{
        if (Scalar.class.isAssignableFrom(array.elementClass()))
        {
            if (array instanceof IntArray)
            {
                // Case of integer scalar images
                int val = ((IntArray<?>) array).getInt(pos);
                return String.format("val=%d", val);
            } 
            else
            {
                // Case of floating-point scalar images
                double val = ((ScalarArray<?>) array).getValue(pos);
                return String.format(Locale.US, "val=%g", val);
            }
        } 
        else if (array instanceof RGB8Array)
        {
            // Case of RGB color images
            int[] rgb = ((RGB8Array) array).get(pos).getSamples();
            return String.format("rgb=[%d,%d,%d]", rgb[0], rgb[1], rgb[2]);
        } 
        else if (array instanceof RGB16Array)
        {
            // Case of RGB color images
            int[] rgb = ((RGB16Array) array).get(pos).getSamples();
            return String.format("rgb=[%d,%d,%d]", rgb[0], rgb[1], rgb[2]);
        } 
        else if (array instanceof VectorArray)
        {
            // in case of Vector array, compute the norm of the pixel for display
            double[] values = ((VectorArray<?,?>) array).getValues(pos);
            double norm = 0;
            for (int c = 0; c < values.length; c++)
                norm += values[c] * values[c];
            norm = Math.sqrt(norm);
            return String.format(Locale.US, "norm=%g", norm);
        } 
        else
        {
            throw new IllegalArgumentException(
                    "Unable to manage image of class " + array.getClass());
        }
	}
}
