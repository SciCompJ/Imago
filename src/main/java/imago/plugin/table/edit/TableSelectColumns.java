/**
 * 
 */
package imago.plugin.table.edit;

import java.util.ArrayList;

import net.sci.table.Table;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TableFramePlugin;

/**
 * Opens a dialog to choose a selection of columns, and returns a new Table
 * containing only the selected columns.
 * 
 * @author dlegland
 */
public class TableSelectColumns implements TableFramePlugin
{

    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Get the data table
        if (!(frame instanceof TableFrame))
        {
            return;
        }
        Table table = ((TableFrame) frame).getTable();

        // get general info from table
        int nCols = table.columnCount();
        String[] colNames = table.getColumnNames();

        // Display dialog for choosing options
        GenericDialog dlg = new GenericDialog(frame, "Select Columns");

        // add one check box for each column
        int nCols2 = Math.min(nCols, 20);
        for (int i = 0; i < nCols2; i++)
        {
            dlg.addCheckBox(colNames[i], false);
        }
        dlg.showDialog();

        // wait for user input
        if (dlg.wasCanceled()) 
        {
            return;
        }
        
        // Parse dialog contents
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < nCols2; i++)
        {
            if (dlg.getNextBoolean())
            {
                indices.add(i);
            }
        }
        
        // Default name for table
        String tableName = table.getName();
        if (tableName == null || tableName.length() == 0)
        {
            tableName = "data";
        }
        
        int[] columnIndices = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++)
        {
            columnIndices[i] = indices.get(i);
        }
        Table res = Table.selectColumns(table, columnIndices);
        res.setName(tableName + "-colSel");
        
        // add the new frame to the GUI
        TableFrame.create(res, frame);
    }
}
