/**
 * 
 */
package imago.plugin.table.edit;

import imago.gui.ImagoFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TablePlugin;
import net.sci.table.Table;

/**
 * @author dlegland
 *
 */
public class TransposeTable implements TablePlugin
{

    /**
     * 
     */
    public TransposeTable()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get table references by the frame
        Table table = ((TableFrame) frame).getTable();
        
        int nr = table.columnCount();
        int nc = table.rowCount();
        
        Table res = Table.create(nr, nc);
        res.setName(table.getName() + "-transpose");
        
        for (int r = 0; r < nr; r++)
        {
            for (int c = 0; c < nc; c++)
            {
                res.setValue(r,  c , table.getValue(c,  r));
            }
        }
        
        for (int r = 0; r < nr; r++)
        {
            res.setRowName(r, table.getColumnName(r));
        }
        for (int c = 0; c < nc; c++)
        {
            res.setColumnName(c, table.getRowName(c));
        }


        // add the new frame to the GUI
        frame.createTableFrame(res);
    }

}
