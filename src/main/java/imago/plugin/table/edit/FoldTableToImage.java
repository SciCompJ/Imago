/**
 * 
 */
package imago.plugin.table.edit;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.table.TableFrame;
import imago.plugin.table.TablePlugin;
import net.sci.array.scalar.Float32Array;
import net.sci.image.Image;
import net.sci.table.Table;

/**
 * @author dlegland
 *
 */
public class FoldTableToImage implements TablePlugin
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

        // TODO: check table is all numeric

        int sizeX;
        int sizeY;
        int sizeZ;

        // loop until valid values are entered
        while (true)
        {
            // create dialog to setup options
            GenericDialog dlg = new GenericDialog(frame, "Fold Table");
            dlg.addNumericField("Image Size X: ", nr, 0);
            dlg.addNumericField("Image Size Y: ", 1, 0);
            dlg.addNumericField("Image Size Z: ", 1, 0);
            
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
        
        // fill result array with first column content
        Float32Array res;
        if (sizeZ == 1)
        {
            res = Float32Array.create(sizeX, sizeY);
        }
        else
        {
            res = Float32Array.create(sizeX, sizeY, sizeZ);
        }
        
        Float32Array.Iterator iter = res.iterator();
        for (int r = 0; r < nr; r++)
        {
            iter.setNextValue(table.getValue(r, 0));
        }
        
        Image image = new Image(res);
        image.setName(table.getName() + "-fold");
        
        // add the image document to GUI
        frame.createImageFrame(image);
    }

}
