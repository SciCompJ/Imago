/**
 * 
 */
package imago.plugin.image.shape;

import java.util.ArrayList;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import net.sci.array.Array2D;
import net.sci.array.Array3D;
import net.sci.image.Image;
import net.sci.image.process.shape.Montage;

/**
 * Generates a 2D image from a 3D image by arranging a selection of slices into
 * a planar montage.
 * 
 * @author dlegland
 *
 */
public class Image3DSliceMontage implements FramePlugin
{

    /**
     * Default constructor.
     */
    public Image3DSliceMontage()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        System.out.println("image 3D slice montage");

        // get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        Image image = iFrame.getImage();
        
        // check 3D
        if (image.getDimension() != 3)
        {
            frame.showErrorDialog("Requires a 3D image as input", "Dimensionality Error");
        }
        
        // retrieve 3D data
        Array3D<?> array = Array3D.wrap(image.getData());
        int sizeZ = image.getSize(2);
        
        // compute default values
        double rootZ = Math.sqrt(sizeZ);
        int initRow = (int) Math.ceil(rootZ);
        int initCol = (int) Math.floor(rootZ);
        
        // create dialog to choose input arguments
        GenericDialog gd = new GenericDialog(frame, "Slice Montage");
        gd.addNumericField("Columns ", initRow, 0);
        gd.addNumericField("Rows ", initCol, 0);
        gd.addNumericField("Interval ", 1, 0);
        gd.addNumericField("First slice", 0, 0);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        int nCols = (int) gd.getNextNumber();
        int nRows = (int) gd.getNextNumber();
        int zStep = (int) gd.getNextNumber();
        int z0 = (int) gd.getNextNumber();
        
        ArrayList<Array2D<?>> slices = new ArrayList<>();
        for (int z = z0; z < sizeZ; z+= zStep)
        {
            slices.add(array.slice(z));
        }
        
        Array2D<?> res = Montage.create(nCols, nRows, slices);

        Image resultImage = new Image(res, image);
        resultImage.setName(image.getName() + "-sliceMontage");
        
        iFrame.createImageFrame(resultImage);
    }
}
