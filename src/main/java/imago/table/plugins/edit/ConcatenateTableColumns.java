/**
 * 
 */
package imago.table.plugins.edit;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import imago.table.TableHandle;
import net.sci.table.Table;
import net.sci.table.Tables;

/**
 * Creates a new table based on the concatenation of the columns of two input
 * tables.
 * 
 * @author dlegland
 */
public class ConcatenateTableColumns implements FramePlugin
{
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // collect the names of current tables
        ImagoApp app = frame.getGui().getAppli();
        String[] tableNames = TableHandle.getAllNames(app).toArray(new String[]{});
        
        // do not continue if no table exist
        if (tableNames.length == 0)
        {
            return;
        }
                
        // Create Dialog for choosing image names
        GenericDialog dialog = new GenericDialog(frame, "Merge tables");
        dialog.addChoice("First Table:", tableNames, tableNames[0]);
        dialog.addChoice("Second Table:", tableNames, tableNames[0]);

        // Display dialog and wait for OK or Cancel
        dialog.showDialog();
        if (dialog.wasCanceled())
        {
            return;
        }
        
        String tableName1 = dialog.getNextChoice();
        String tableName2 = dialog.getNextChoice();
        
        // retrieve tables by their names
        Table table1 = TableHandle.findFromName(app, tableName1).getTable();
        Table table2 = TableHandle.findFromName(app, tableName2).getTable();
        if (table1.rowCount() != table2.rowCount())
        {
            throw new RuntimeException("Both tables must have same number of rows");
        }
        
        Table res = Tables.concatenateColumns(table1, table2);
        
        // add the new frame to the GUI
        TableFrame.create(res, frame);
    }
    
    public boolean isEnabled(ImagoFrame frame)
    {
        return TableHandle.getAllNames(frame.getGui().getAppli()).size() > 0;
    }
}
