/**
 * 
 */
package imago.plugin.image;

import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
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
public class ImageOperatorPlugin implements FramePlugin
{    
    /**
     * The instance of ImageOperator that will transform an image into another
     * image.
     */
    ImageOperator operator;
    
    /**
     * Default empty constructor that allows direct specialization.
     */
    public ImageOperatorPlugin(ImageOperator operator)
    {
        this.operator = operator;
    }
    
    /**
     * @return the ImageOperator encapsulated by the plugin.
     */
    public ImageOperator operator()
    {
        return this.operator;
    }
    
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
    	Image image = ((ImageFrame) frame).getImageHandle().getImage();

    	// initialize listener and timer
//        operator.addAlgoListener(frame);
        long t0 = System.nanoTime();
        Image result = operator.process(image);
        long t1 = System.nanoTime();
        
        // cleanup listener and status bar
//        op.removeAlgoListener(this);
        if (frame instanceof ImageFrame)
        {
            ImageFrame imageFrame = ((ImageFrame) frame);
            imageFrame.getStatusBar().setProgressBarPercent(0);
            // display elapsed time
            String opName = operator.getClass().getSimpleName();
            imageFrame.showElapsedTime(opName, (t1 - t0) / 1_000_000.0, image);
        }
        
        // add the image document to GUI
        frame.createImageFrame(result);
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
        Image image = ((ImageFrame) frame).getImageHandle().getImage();
        if (image == null)
            return false;

        return operator.canProcess(image);
    }
}
