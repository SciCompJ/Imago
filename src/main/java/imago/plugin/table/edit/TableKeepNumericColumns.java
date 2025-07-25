/**
 * 
 */
package imago.plugin.table.edit;

import imago.gui.ImagoFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TableFramePlugin;
import net.sci.table.NumericTable;
import net.sci.table.Table;


/**
 * Keep only numeric columns from a table and show the new table in a new frame.
 * 
 * @author David Legland
 *
 */
public class TableKeepNumericColumns implements TableFramePlugin
{
    public TableKeepNumericColumns()
    {
    }
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
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

	    NumericTable table2 = NumericTable.keepNumericColumns(table);
	    table2.setName(table.getName() + "-num");
        
        // add the new frame to the GUI
	    TableFrame.create(table2, frame);
	}
	
}
