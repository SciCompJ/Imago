/**
 * 
 */
package imago.table.plugin.edit;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import imago.table.plugin.TableFramePlugin;
import net.sci.axis.Axis;
import net.sci.axis.CategoricalAxis;
import net.sci.table.CategoricalColumn;
import net.sci.table.Table;

/**
 * 
 */
public class TableParseGroupFromRowNames implements TableFramePlugin
{
    /**
     * Default empty constructor.
     */
    public TableParseGroupFromRowNames()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        // Get the data table
        if (!(frame instanceof TableFrame))
        {
            return;
        }
        Table table = ((TableFrame) frame).getTable();
        
        Axis rowAxis = table.getRowAxis();
        if (rowAxis == null || !(rowAxis instanceof CategoricalAxis)) return;
        
        String[] rowNames = ((CategoricalAxis) rowAxis).itemNames();
        if (rowNames.length == 0) return;
        
        GenericDialog dlg = new GenericDialog(frame, "Table Parse Group");
        dlg.addMessage("Sample name: " + rowNames[0]);
        dlg.addTextField("Group Name", "group");
        dlg.addNumericField("Start Index", 0, 0);
        dlg.addNumericField("Character Count", 0, 0);
        dlg.addCheckBox("New Table", true);
        
        dlg.showDialog();
        // wait for user input
        if (dlg.wasCanceled()) 
        {
            return;
        }
        
        // Parse dialog contents
        String colName = dlg.getNextString();
        int startIndex = (int) dlg.getNextNumber();
        int charCount = (int) dlg.getNextNumber();
        boolean newTable = dlg.getNextBoolean();
        
        String[] itemNames = new String[rowNames.length];
        for (int iRow = 0; iRow < rowNames.length; iRow++)
        {
            String name = rowNames[iRow];
            if (startIndex + charCount <= name.length())
            {
                itemNames[iRow] = name.substring(startIndex, startIndex + charCount);
            }
            else
            {
                itemNames[iRow] = "(none)";
            }
        }
        
        CategoricalColumn newCol = CategoricalColumn.create(colName, itemNames);
        
        if (newTable)
        {
            Table res = Table.create(table.getRowAxis(), newCol);
            res.setName(table.getName() + "-" + colName);
            TableFrame.create(res, frame);
        }
        else
        {
            table.addColumn(newCol);
            ((TableFrame) frame).updateTableDisplay();
        }
    }
}
