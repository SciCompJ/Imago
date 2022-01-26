/**
 * 
 */
package imago.plugin.image;

import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
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

    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImage();
        
        // initialize listener and timer
        operator.addAlgoListener(frame);
        long t0 = System.nanoTime();
        Image result = imageFrame.runOperator(operator, image);
        long t1 = System.nanoTime();
        
        // cleanup listener and status bar
        operator.removeAlgoListener(frame);
        imageFrame.getStatusBar().setProgressBarPercent(0);
        
        // display elapsed time
        String opName = operator.getClass().getSimpleName();
        imageFrame.showElapsedTime(opName, (t1 - t0) / 1_000_000.0, image);
        
        // add the image document to GUI
        frame.getGui().createImageFrame(result, frame);
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
        Image image = ((ImageFrame) frame).getImage();
        if (image == null)
            return false;

        return operator.canProcess(image.getData());
    }
}
