package imago.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;

import imago.app.UserPreferences;
import imago.image.ImageFrame;
import imago.image.ImageTool;
import imago.image.viewers.ImageDisplay;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.Array3D;
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
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        
        Image image = this.frame.getImageViewer().getImage();
        Array<?> array = image.getData();
        if (!array.isModifiable())
        {
            return;
        }
        
        
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
        Array2D<?> currentSlice = wrapSlice(array);
        
        if (Scalar.class.isAssignableFrom(array.elementClass()))
        {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            ScalarArray2D<?> scalar2d = ScalarArray2D.wrap(ScalarArray.wrap((Array<? extends Scalar>) currentSlice));
            double value = scalar2d.getValue(xi, yi);
            prefs.brushValue = value;
        } 
        else if (RGB8.class.isAssignableFrom(array.elementClass()))
        {
            RGB8Array2D array2d = RGB8Array2D.wrap(RGB8Array.wrap(currentSlice));
            RGB8 rgbValue = array2d.get(xi, yi);
            prefs.brushColor = rgbValue;
        }
        
        this.frame.getImageViewer().refreshDisplay();
        this.frame.repaint();
    }
    
    private Array2D<?> wrapSlice(Array<?> array)
    {
        // select the 2D array to update
        if (array.dimensionality() == 2)
        {
            return Array2D.wrap(array);
        }
        else if (array.dimensionality() == 3)
        {
            int zi = this.frame.getImageViewer().getSlicingPosition(2);
            return Array3D.wrap(array).slice(zi);
        }
        
        throw new RuntimeException("Requires either a 2D or a 3D array");
    }
}
