/**
 * 
 */
package imago.plugin.table.edit;

import java.util.ArrayList;

import imago.app.ImagoApp;
import imago.app.TableHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.table.TableFrame;
import net.sci.table.Column;
import net.sci.table.Table;

/**
 * @author dlegland
 *
 */
public class MergeTablesByColumns implements FramePlugin
{
    
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // collect the names of frames containing tables
        ArrayList<String> tableNames = findTableNameList(frame.getGui());
        
        // do not continue if no table exist
        if (tableNames.size() == 0)
        {
            return;
        }
        
        // Convert table name list to String array
        String[] tableNameArray = tableNames.toArray(new String[]{});
        String firstTableName = tableNameArray[0];
                
        // Create Dialog for choosing image names
        GenericDialog dialog = new GenericDialog(frame, "Merge tables");
        dialog.addChoice("First Table:", tableNameArray, firstTableName);
        dialog.addChoice("Second Table:", tableNameArray, firstTableName);

        // Display dialog and wait for OK or Cancel
        dialog.showDialog();
        if (dialog.wasCanceled())
        {
            return;
        }
        
        String tableName1 = dialog.getNextChoice();
        String tableName2 = dialog.getNextChoice();
        
        // retrieve tables by their names
        ImagoApp app = frame.getGui().getAppli();
        Table table1 = TableHandle.findFromName(app, tableName1).getTable();
        Table table2 = TableHandle.findFromName(app, tableName2).getTable();
        if (table1.rowCount() != table2.rowCount())
        {
            throw new RuntimeException("Both tables must have same number of rows");
        }
        
        // concatenate columns
        ArrayList<Column> columns = new ArrayList<Column>(table1.columnCount() + table2.columnCount());
        for (Column col : table1.columns())
        {
            columns.add(col);
        }
        for (Column col : table2.columns())
        {
            columns.add(col);
        }
        
        // create table
        Table res = Table.create(columns.toArray(new Column[] {}));
//        Table res = Table.create(table1.rowCount(), columns.size());
//        for (int i = 0; i < columns.size(); i++)
//        {
//            res.setColumnValues(i, columns.get(i).getValues());
//        }
        res.setName(table1.getName() + "+" + table2.getName());
        res.setRowNames(table1.getRowNames());
        
        // add the new frame to the GUI
        TableFrame.create(res, frame);
    }
    
    private ArrayList<String> findTableNameList(ImagoGui gui)
    {
        ArrayList<String> tableNames = new ArrayList<>();
        gui.getFrames().stream()
            .filter(frame -> frame instanceof TableFrame)
            .forEach(frame -> tableNames.add(((TableFrame) frame).getTable().getName()));
        return tableNames;
    }
    
    public boolean isEnabled(ImagoFrame frame)
    {
        return findTableNameList(frame.getGui()).size() > 0;
    }
}
