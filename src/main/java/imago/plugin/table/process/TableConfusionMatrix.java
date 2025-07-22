/**
 * 
 */
package imago.plugin.table.process;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TableFramePlugin;
import net.sci.table.Column;
import net.sci.table.Table;
import net.sci.table.process.ConfusionMatrix;

/**
 * Computes the confusion matrix of two categorical columns within a table.
 */
public class TableConfusionMatrix implements TableFramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get table references by the frame
        Table table = ((TableFrame) frame).getTable();
        if (table.columnCount() < 2)
        {
            throw new RuntimeException("Requires a column with at least two columns");
        }
        
        String[] colNames = table.getColumnNames();
        
        // create dialog to setup options
        GenericDialog dlg = new GenericDialog(frame, "Confusion Matrix");
        dlg.addChoice("First column: ", colNames, colNames[0]);
        dlg.addChoice("Second column: ", colNames, colNames[1]);
        
        // wait for user validation
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // parse dialog inputs
        int indCol1 = dlg.getNextChoiceIndex();
        int indCol2 = dlg.getNextChoiceIndex();
        
        Column col1 = table.column(indCol1);
        Column col2 = table.column(indCol2);
        Table res = new ConfusionMatrix().process(col1, col2);
        res.setName("Confusion Matrix");
        
        // add the new frame to the GUI
        TableFrame.create(res, frame);
    }

}
