/**
 * 
 */
package imago.plugin.image;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Image;
import net.sci.image.ImageOperator;

/**
 * 
 * Encapsulates an instance of ImageOperator into a Plugin, such that the
 * operator can be run on the image contained within the frame calling this
 * plugin.
 * 
 * @author dlegland
 *
 */
public class ImageOperatorPlugin implements Plugin
{    
    ImageOperator operator;
    
    /**
     * Default empty constructor that allows direct specialization.
     */
    public ImageOperatorPlugin(ImageOperator operator)
    {
        this.operator = operator;
    }

    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getDocument();
        Image image = doc.getImage();

        Image result = operator.process(image);

        // add the image document to GUI
        frame.getGui().addNewDocument(result, doc);
    }

    /**
     * Overrides the default implementation to returns the result of
     * 'canProcess' computed on the inner image operator.
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
        ImageHandle doc = ((ImageFrame) frame).getDocument();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return operator.canProcess(image);
    }
}
