/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.shape.Flip;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class ImageFlip implements FramePlugin
{
    int dim;

    public ImageFlip()
    {
        this.dim = 0;
    }

    public ImageFlip(int dim)
    {
        this.dim = dim;
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();

        if (this.dim > image.getDimension())
        { throw new IllegalArgumentException(String.format("Can now flip image of dim. %d along dim. %d", image.getDimension(), this.dim)); }

        Flip filter = new Flip(this.dim);
        Image result = image.apply(filter);

        // add the image document to GUI
        ImageFrame.create(result, frame);
    }

    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame)) return false;

        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null) return false;

        return true;
    }
}
