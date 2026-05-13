/**
 * 
 */
package imago.shape.plugins.edit;

import java.awt.Color;
import java.util.Collection;

import imago.app.ImagoApp;
import imago.app.shape.Shape;
import imago.app.shape.Style;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import net.sci.geom.geom2d.Domain2D;

/**
 * Opens a dialog to choose an image, and copies the selected shapes in the
 * "shapes" field of the chosen image.
 */
public class CopyToImageShapes implements ShapeManagerPlugin
{
    private enum CommonColors
    {
        RED("Red", Color.RED),
        GREEN("Green", Color.GREEN),
        BLUE("Blue", Color.BLUE),
        CYAN("Cyan", Color.CYAN),
        MAGENTA("Magenta", Color.MAGENTA),
        YELLOW("Yellow", Color.YELLOW),
        WHITE("White", Color.WHITE),
        BLACK("Black", Color.BLACK),
        GRAY("Gray", Color.GRAY),
        DARK_GRAY("Light Gray", Color.DARK_GRAY),
        LIGHT_GRAY("Dark Gray", Color.LIGHT_GRAY),
        DARK_RED("Dark Red", Color.RED.darker()),
        DARK_GREEN("Dark Green", Color.GREEN.darker()),
        DARK_BLUE("Dark Blue", Color.BLUE.darker()),
        DARK_CYAN("Dark Cyan", Color.CYAN.darker()),
        DARK_MAGENTA("Dark Magenta", Color.MAGENTA.darker()),
        DARK_YELLOW("Dark Yellow", Color.YELLOW.darker());
        
        String label;
        Color color;
        
        private CommonColors(String label, Color color)
        {
            this.label = label;
            this.color = color;
        }
        
        public Color getColor()
        {
            return this.color;
        }
        
        @Override
        public String toString()
        {
            return this.label;
        }
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        Collection<GeometryHandle> handles = sm.getSelectedHandles();
        boolean allDomains = handles.stream().allMatch(h -> h.getGeometry() instanceof Domain2D);
        
        ImagoApp app = frame.getGui().getAppli();
        String[] imageNames = ImageHandle.getAllNames(app).toArray(new String[0]);
        
        GenericDialog dlg = new GenericDialog(frame, "Add to Image Shapes");
        dlg.addChoice("Image to Update", imageNames, imageNames[0]);
        dlg.addEnumChoice("Line Color: ", CommonColors.class, CommonColors.BLUE);
        dlg.addNumericField("Line Width:", 1.0, 2);
        if (allDomains)
        {
            dlg.addCheckBox("Fill", true);
            dlg.addEnumChoice("Fill Color: ", CommonColors.class, CommonColors.CYAN);
            dlg.addSlider("Fill Opacity", 0, 100, 50);
        }
        
        // show dialog and wait for user
        dlg.showDialog();
        if (dlg.wasCanceled()) 
        {
            return;
        }
        
        // parse dialog options
        ImageHandle imageHandle =  ImageHandle.findFromName(app, dlg.getNextChoice());
        Color color = ((CommonColors) dlg.getNextEnumChoice()).getColor();
        double lineWidth = dlg.getNextNumber();
        Style style = new Style().setLineWidth(lineWidth).setLineColor(color);
        if (allDomains)
        {
            boolean fill = dlg.getNextBoolean();
            Color fillColor =  ((CommonColors) dlg.getNextEnumChoice()).getColor();
            double fillOpacity = dlg.getNextNumber();
            System.out.println("base opacity: " + fillOpacity);
            style.setFillVisible(fill).setFillColor(fillColor).setFillOpacity(fillOpacity / 100.0);
        }
                
        for (GeometryHandle handle : sm.getSelectedHandles())
        {
            Shape shape = new Shape(handle.getGeometry(), style);
            imageHandle.addShape(shape);
        }
        
        imageHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
    }
}
