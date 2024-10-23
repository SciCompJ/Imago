/**
 * 
 */
package imago.plugin.table.process;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TablePlugin;
import net.sci.table.CategoricalColumn;
import net.sci.table.Table;
import net.sci.table.process.Aggregate;

/**
 * 
 */
public class AggregateTableWithColumn implements TablePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get table references by the frame
        Table table = ((TableFrame) frame).getTable();
        
        String[] colNames = table.getColumnNames();
        
        // create dialog to setup options
        GenericDialog dlg = new GenericDialog(frame, "Aggregate");
        dlg.addChoice("Groups column: ", colNames, colNames[0]);
        
        // wait for user validation
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // parse dialog inputs
        int indCol = dlg.getNextChoiceIndex();
        
        CategoricalColumn groups = (CategoricalColumn) table.column(indCol);
        int[] columnIndices = new int[table.columnCount()-1];
        for (int i = 0; i < indCol; i++)
        {
            columnIndices[i] = i;
        }
        for (int i = indCol + 1; i < table.columnCount(); i++)
        {
            columnIndices[i-1] = i;
        }
        Table tmp = Table.selectColumns(table, columnIndices);
        
        Table res = Aggregate.aggregate(tmp, groups);
        
        // add the new frame to the GUI
        TableFrame.create(res, frame);
    }

}
