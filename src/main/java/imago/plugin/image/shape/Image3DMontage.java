/**
 * 
 */
package imago.plugin.image.shape;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.Array3D;
import net.sci.image.Image;

/**
 * Create a 2D montage from a selection of slices within a 3D image.
 * 
 * @author dlegland
 *
 */
public class Image3DMontage implements FramePlugin
{

    /**
     * Default constructor.
     */
    public Image3DMontage()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        System.out.println("image 3D montage");

        // get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        Image image = iFrame.getImage();
        
        // check 3D
        if (image.getDimension() != 3)
        {
            frame.showErrorDialog("Requires a 3D image as input", "Dimensionality Error");
        }
        Array3D<?> array = Array3D.wrap(image.getData());
        
        
        // compute default values
        int sizeZ = image.getSize(2);
        double rootZ = Math.sqrt(sizeZ);
        int initRow = (int) Math.ceil(rootZ);
        int initCol = (int) Math.floor(rootZ);
        
        GenericDialog gd = new GenericDialog(frame, "Extract planar slice");
        gd.addNumericField("Columns ", initRow, 0);
        gd.addNumericField("Rows ", initCol, 0);
        gd.addNumericField("Interval ", 1, 0);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        int nCols = (int) gd.getNextNumber();
        int nRows = (int) gd.getNextNumber();
        int interval = (int) gd.getNextNumber();
        
        Array<?> res = process(array, nCols, nRows, interval);
        Image resultImage = new Image(res, image);
        resultImage.setName(image.getName() + "-montage");
        
        iFrame.createImageFrame(resultImage);
    }
    
    private <T> Array2D<T> process(Array3D<T> array, int nCols, int nRows, int interval)
    {
        // retrieve array size
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        int sizeZ = array.size(2);
        
        // allocate result array
        int newSizeX = sizeX * nCols;
        int newSizeY = sizeY * nRows;
        Array<T> res = array.newInstance(newSizeX, newSizeY);
        
        // prepare iteration
        int[] pos = new int[2];
        int sliceIndex = 0;
        int x0 = 0;
        int y0 = 0;
        
        // main iteration on tiles
        // use a label to break the loop when max number of slices is reached
        tiles:
        for (int c = 0; c < nCols; c++)
        {
            for (int r = 0; r < nRows; r++)
            {
                // copy the slice into the corresponding region
                Array2D<T> slice = array.slice(sliceIndex);
                for (int y = 0; y < sizeY; y++)
                {
                    pos[1] = y + y0;
                    for (int x = 0; x < sizeX; x++)
                    {
                        pos[0] = x + x0;
                        res.set(pos, slice.get(x, y));
                    }
                }
                
                sliceIndex += interval;
                if (sliceIndex >= sizeZ)
                {
                    break tiles;
                }
                
                // switch to next tile
                x0 += sizeX;
            }
            
            // restart at next row
            y0 += sizeY;
            x0 = 0;
        }
        
        return Array2D.wrap(res);
    }

}
