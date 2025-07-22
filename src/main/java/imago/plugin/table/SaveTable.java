/**
 * 
 */
package imago.plugin.table;

import java.io.File;
import java.io.IOException;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.table.TableFrame;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableWriter;


/**
 * Opens a dialog to choose a file to save a table.
 * 
 * @author David Legland
 *
 */
public class SaveTable implements TableFramePlugin
{
    public SaveTable()
    {
    }
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
    public void run(ImagoFrame frame, String args)
	{
        // get table references by the frame
        Table table = ((TableFrame) frame).getTable();
        
        // opens a dialog to choose the file
        File file = frame.getGui().chooseFileToSave(frame, "Save Table", table.getName() + ".csv");
        if (file == null)
        {
            return;
        }
        
        try
        {
            new DelimitedTableWriter().writeTable(table, file);
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
            // custom title, error icon
            ImagoGui.showErrorDialog(frame, "Could not write the table.", "Table I/O Error");
            return;
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
            ImagoGui.showErrorDialog(frame, "Could not write the table.", "Table I/O Error");
            return;
        }
    }
}
