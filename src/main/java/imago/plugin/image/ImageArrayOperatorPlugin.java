/**
 * 
 */
package imago.plugin.image;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
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
public class ImageArrayOperatorPlugin implements Plugin
{    
    ArrayOperator operator;
    
    /**
     */
    public ImageArrayOperatorPlugin(ArrayOperator operator)
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
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();

        Image result = new Image(operator.process(image.getData()), image);

        // add the image document to GUI
        frame.getGui().addNewDocument(result, doc);
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
        if (!(frame instanceof ImagoDocViewer))
            return false;
        
        // check image
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return operator.canProcess(image.getData());
    }
}