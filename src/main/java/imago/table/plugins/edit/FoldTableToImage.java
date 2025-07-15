/**
 * 
 */
package imago.table.plugins.edit;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.table.TableFrame;
import imago.table.plugins.TableFramePlugin;
import net.sci.array.numeric.Float32Array;
import net.sci.array.numeric.Int16Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array;
import net.sci.image.Image;
import net.sci.image.ImageType;
import net.sci.table.Column;
import net.sci.table.IntegerColumn;
import net.sci.table.Table;

/**
 * @author dlegland
 *
 */
public class FoldTableToImage implements TableFramePlugin
{

    /**
     * Default constructor.
     */
    public FoldTableToImage()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get table references by the frame
        Table table = ((TableFrame) frame).getTable();
        int nr = table.rowCount();

        // peek the first column
        Column col = table.column(0);
        
        // TODO: check table is all numeric

        int sizeX;
        int sizeY;
        int sizeZ;
        boolean labelMap = false;

        // loop until valid values are entered
        while (true)
        {
            // create dialog to setup options
            GenericDialog dlg = new GenericDialog(frame, "Fold Table");
            dlg.addNumericField("Image Size X: ", nr, 0);
            dlg.addNumericField("Image Size Y: ", 1, 0);
            dlg.addNumericField("Image Size Z: ", 1, 0);
            if (col instanceof IntegerColumn)
            {
                dlg.addCheckBox("Create Label Image", false);
            }
            // wait for user validation
            dlg.showDialog();
            if (dlg.wasCanceled())
            {
                return;
            }

            // parse dialog inputs
            sizeX = (int) dlg.getNextNumber();
            sizeY = (int) dlg.getNextNumber();
            sizeZ = (int) dlg.getNextNumber();
            if (col instanceof IntegerColumn)
            {
                labelMap = dlg.getNextBoolean();
            }
            
            // check input validity
            if (sizeX * sizeY * sizeZ == nr)
            {
                break;
            }
            else
            {
                ImagoGui.showErrorDialog(frame, "Product of image dimensions must match row number: " + nr, "User Input Error");
            }
        }
        
        // determines size of output image
        int[] dims = (sizeZ == 1) ? new int[] {sizeX, sizeY} : new int[] {sizeX, sizeY, sizeZ};  
        
        // choose array factory depending on column type
        ScalarArray.Factory<?> factory = null;
        if (col instanceof IntegerColumn)
        {
            if (labelMap)
            {
                factory = UInt8Array.defaultFactory;
            }
            else
            {
                factory = Int16Array.defaultFactory;
            }
        }
        else
        {
            factory = Float32Array.defaultFactory;
        }
        
        // fill result array with first column content
        ScalarArray<?> res = factory.create(dims);
        
        Image image;
        if (labelMap)
        {
            ScalarArray.Iterator<?> iter = res.iterator();
            int labelMax = 0;
            for (int r = 0; r < nr; r++)
            {
                int value = (int) col.getValue(r) + 1;
                labelMax = Math.max(value, labelMax);
                iter.setNextValue(value);
            }
            image = new Image(res, ImageType.LABEL);
            image.getDisplaySettings().setDisplayRange(new double[] {0, labelMax});
        }
        else
        {
            ScalarArray.Iterator<?> iter = res.iterator();
            for (int r = 0; r < nr; r++)
            {
                iter.setNextValue(table.getValue(r, 0));
            }
            image = new Image(res);
        }
        image.setName(table.getName() + "-fold");

        // add the image document to GUI
        ImageFrame.create(image, frame);
    }
}
