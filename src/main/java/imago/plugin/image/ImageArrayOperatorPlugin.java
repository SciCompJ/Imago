/**
 * 
 */
package imago.plugin.image;

import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;

import java.util.Locale;

import imago.gui.FramePlugin;
import net.sci.array.ArrayOperator;
import net.sci.image.Image;

/**
 * 
 * Encapsulates an instance of ArrayOperator into a Plugin, such that the
 * operator can be run on the array of the image contained within the frame
 * calling this plugin.
 * 
 * @author dlegland
 *
 */
public class ImageArrayOperatorPlugin implements FramePlugin
{    
    ArrayOperator operator;
    
    String opName = null;
    
    String newNamePattern = null;
    
    /**
     * Creates a new plugin from an operator.
     * 
     * @param operator
     *            the instance of ArrayOperator
     */
    public ImageArrayOperatorPlugin(ArrayOperator operator)
    {
        this.operator = operator;
    }

    /**
     * Creates a new plugin from an operator.
     * 
     * @param operator
     *            the instance of ArrayOperator
     */
    public ImageArrayOperatorPlugin(ArrayOperator operator, String opName, String newNamePattern)
    {
        this.operator = operator;
        this.opName = opName;
        this.newNamePattern = newNamePattern;
    }

    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        
        // run the operator on current image, using the dedicated method in ImageFrame instance
        Image result; 
        if (opName != null)
            result = imageFrame.runOperator(opName, operator, image);
        else
            result = imageFrame.runOperator(operator, image);
        
        // optionally creates new name
        if (newNamePattern != null)
        {
            String newName = String.format(Locale.ENGLISH, newNamePattern, image.getName());
            result.setName(newName);
        }
        
        // display result image in a new frame
        imageFrame.createImageFrame(result);
    }

    /**
     * Overrides the default implementation to returns the result of
     * 'canProcess' computed on the inner array operator.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        Image image = ((ImageFrame) frame).getImageHandle().getImage();
        if (image == null)
            return false;

        return operator.canProcess(image.getData());
    }
}
