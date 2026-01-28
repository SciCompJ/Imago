/**
 * 
 */
package imago.shape.plugins.edit;

import java.awt.Color;

import imago.app.ImagoApp;
import imago.app.shape.Shape;
import imago.app.shape.Style;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;

/**
 * Opens a dialog to choose an image, and copies the selected shapes in the
 * "shapes" field of the chosen image.
 */
public class CopyToImageShapes implements ShapeManagerPlugin
{
    private static final Color[] colors = new Color[] {
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.CYAN,
            Color.MAGENTA,
            Color.YELLOW,
            Color.WHITE,
            Color.BLACK,
            Color.GRAY,
            Color.DARK_GRAY,
            Color.LIGHT_GRAY,
            Color.RED.darker(),
            Color.GREEN.darker(),
            Color.BLUE.darker(),
            Color.CYAN.darker(),
            Color.MAGENTA.darker(),
            Color.YELLOW.darker(),
    };
    
    private static final String[] colorNames = new String[] {
            "Red",
            "Green",
            "Blue",
            "Cyan",
            "Magenta",
            "Yellow",
            "White",
            "Black",
            "Gray",
            "Dark Gray",
            "Light Gray",
            "Dark Red",
            "Dark Green",
            "Dark Blue",
            "Dark Cyan",
            "Dark Magenta",
            "Dark Yellow",
    };
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        ImagoApp app = frame.getGui().getAppli();
        String[] imageNames = ImageHandle.getAllNames(app).toArray(new String[0]);
        
        GenericDialog dlg = new GenericDialog(frame, "Add to Image Shapes");
        dlg.addChoice("Image to Update", imageNames, imageNames[0]);
        dlg.addChoice("Line Color: ", colorNames, colorNames[0]);
        dlg.addNumericField("Line Width:", 1.0, 2);
        
        // show dialog and wait for user
        dlg.showDialog();
        if (dlg.wasCanceled()) 
        {
            return;
        }
        
        // parse dialog options
        ImageHandle imageHandle =  ImageHandle.findFromName(app, dlg.getNextChoice());
        Color color = colors[dlg.getNextChoiceIndex()];
        double lineWidth = dlg.getNextNumber();
        Style style = new Style().setLineWidth(lineWidth).setLineColor(color);
                
        for (GeometryHandle handle : sm.getSelectedHandles())
        {
            Shape shape = new Shape(handle.getGeometry(), style);
            imageHandle.addShape(shape);
        }
        
        imageHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
    }
}
