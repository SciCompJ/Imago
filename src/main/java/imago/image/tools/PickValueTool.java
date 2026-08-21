package imago.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;

import imago.app.UserPreferences;
import imago.image.ImageFrame;
import imago.image.ImageTool;
import imago.image.viewers.ImageDisplay;
import imago.image.viewers.XYImageViewer;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.color.RGB8Array2D;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;

public class PickValueTool extends ImageTool
{
    /**
     * Default constructor.
     * 
     * @param viewer
     *            reference to the image viewer frame
     * @param name
     *            the name of this tool
     */
    public PickValueTool(ImageFrame viewer, String name)
    {
        super(viewer, name);
    }

    @Override
    public void mousePressed(MouseEvent evt)
    {
        // check viewer class
        if (!(this.frame.getImageViewer() instanceof XYImageViewer)) return;
        XYImageViewer viewer = (XYImageViewer) this.frame.getImageViewer();
        
        // retrieve image data
        Image image = viewer.getImage();
        Array<?> array = image.getData();
        if (!array.isModifiable())
        {
            return;
        }

        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        
        // convert to array coord
        int xi = (int) Math.round(pos.x());
        int yi = (int) Math.round(pos.y());
        
        System.out.println("[DrawValue] Mouse pressed at (" + xi + " ; " + yi);
        
        // check position is within array bounds
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        if (xi < 0 || yi < 0) return;
        if (xi >= sizeX || yi >= sizeY) return;
        
        UserPreferences prefs = frame.getGui().getAppli().userPreferences;
        
        Array2D<?> slice = viewer.getCurrentSlice();
        if (slice.elementInstanceOf(Scalar.class))
        {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            ScalarArray2D<?> scalar2d = ScalarArray2D.wrap(ScalarArray.wrap((Array<? extends Scalar>) slice));
            double value = scalar2d.getValue(xi, yi);
            prefs.brushValue = value;
        } 
        else if (slice.elementInstanceOf(RGB8.class))
        {
            RGB8Array2D array2d = RGB8Array2D.wrap(RGB8Array.wrap(slice));
            RGB8 rgbValue = array2d.get(xi, yi);
            prefs.brushColor = rgbValue;
        }
        
        this.frame.getImageViewer().refreshDisplay();
        this.frame.repaint();
    }
}
