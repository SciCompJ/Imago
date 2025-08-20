/**
 * 
 */
package imago.plugin.image.edit;

import java.util.stream.Stream;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.image.Image;
import net.sci.image.ImageType;

/**
 * Changes the type of the current image.
 * 
 * @author dlegland
 *
 */
public class SetImageType implements FramePlugin
{
    enum Type
    {
        INTENSITY("Intensity", ImageType.INTENSITY),
        DISTANCE("Distance", ImageType.DISTANCE),
        DIVERGING("Diverging", ImageType.DIVERGING),
        ANGLE("Angle", ImageType.ANGLE),
        LABEL("Label", ImageType.LABEL),
        ;
        
        private final String label;
        
        private ImageType imageType;
        
        private Type(String label, ImageType imageType)
        {
            this.label = label;
            this.imageType = imageType;
        }
        
        public ImageType imageType()
        {
            return this.imageType;
        }
        
        public String toString() 
        {
            return this.label;
        }
        
        public static String[] getAllLabels()
        {
            return Stream.of(Type.values())
                    .map(op -> op.label)
                    .toArray(String[]::new);
        }
        
        public static Type fromLabel(String label)
        {
            for (Type type : Type.values()) 
            {
                if (type.label.equalsIgnoreCase(label))
                    return type;
            }
            throw new IllegalArgumentException("Unable to parse Operation with label: " + label);
        }
    }
    
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageFrame imageFrame = (ImageFrame) frame;
        
        GenericDialog dlg = new GenericDialog(frame, "Set Image Type");
        dlg.addEnumChoice("New Type", Type.class, Type.INTENSITY);
        
        // wait for user validation or cancellation
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // retrieve new name of image
        Type newType = (Type) dlg.getNextEnumChoice();
        ImageType imageType = newType.imageType();
        
        Image image = imageFrame.getImageHandle().getImage();
        image.setType(imageType);
        imageType.setupCalibration(image);
        imageType.setupDisplaySettings(image);
        
        imageFrame.getImageViewer().refreshDisplay();
        imageFrame.repaint();
        imageFrame.updateTitle();
    }
}
