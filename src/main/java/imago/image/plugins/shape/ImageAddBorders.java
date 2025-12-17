/**
 * 
 */
package imago.image.plugins.shape;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.plugins.ImageFramePlugin;
import net.sci.array.Array;
import net.sci.array.shape.Padding;
import net.sci.image.Image;

/**
 * Add values around borders of image.
 * 
 * The type of the value to add is determined by image type. 
 */
public class ImageAddBorders implements ImageFramePlugin
{
    enum Strategy
    {
        BLACK("Black", Padding.Mode.ZERO), 
        WHITE("White", Padding.Mode.TYPE_MAX),
        REPLICATE("Replicate", Padding.Mode.REPLICATE),
        MIRROR("Mirror", Padding.Mode.MIRROR),
        PERIODIC("Periodic", Padding.Mode.PERIODIC);
        
        String label;
        Padding.Mode mode;
        
        private Strategy(String label, Padding.Mode mode)
        {
            this.label = label;
            this.mode = mode;
        }
        
        public Padding.Mode getPaddingMode()
        {
            return mode;
        }
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();
        int nd = array.dimensionality();
        
        // create a dialog for the user to choose options
        GenericDialog gd = new GenericDialog(frame, "Extend Borders");
        for (int d = 0; d < nd; d++)
        {
            gd.addNumericField("Padding dim. " + (d+1), 10, 0);
        }
        gd.addEnumChoice("Padding mode", Strategy.class, Strategy.REPLICATE);
        gd.addCheckBox("Create View", false);
        
        // wait the user to choose
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        int[] padSizes = new int[nd];
        for (int d = 0; d < nd; d++)
        {
            padSizes[d] = (int) gd.getNextNumber();
        }
        Strategy strategy = (Strategy) gd.getNextEnumChoice(); 
        boolean createView = gd.getNextBoolean();
        
        Padding algo = new Padding(padSizes, strategy.mode);
        Array<?> res = createView ? algo.createView(array) : algo.process(array);
        
        Image resultImage = new Image(res, image);
        resultImage.setName(image.getName() + "-addBorders");
        
        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }
    
    
    public boolean isEnabled(ImagoFrame frame)
    {
        if (!(frame instanceof ImageFrame)) return false;
        Image image = ((ImageFrame) frame).getImageHandle().getImage();
        return image != null;
    } 
    
}
