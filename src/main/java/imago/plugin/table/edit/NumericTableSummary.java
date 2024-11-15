/**
 * 
 */
package imago.plugin.table.edit;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.table.TableFrame;
import net.sci.table.NumericTable;
import net.sci.table.Table;

/**
 * Display some info on the table: table name, and short summary of the content of
 * each column.
 * 
 * @see imago.plugin.image.edit.PrintImageInfos;
 */
public class NumericTableSummary implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Get the data table
        if (!(frame instanceof TableFrame))
        {
            return;
        }
        Table table = ((TableFrame) frame).getTable();

        NumericTable table2 = NumericTable.keepNumericColumns(table);
        
        NumericTable res = new net.sci.table.process.NumericTableSummary().process(table2);
        res.setName(table.getName() + "-summary");
        
        // add the new frame to the GUI
        TableFrame.create(res, frame);
    }
    
}
