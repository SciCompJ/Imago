/**
 * 
 */
package imago.plugin.table;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.ImagoTableFrame;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableWriter;


/**
 * @author David Legland
 *
 */
public class SaveTable implements TablePlugin
{
    private JFileChooser saveWindow = null;
    
    public SaveTable()
    {
    }
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
    public void run(ImagoFrame frame, String args)
	{
		// create file dialog if it doesn't exist
		if (saveWindow == null)
		{
			saveWindow = new JFileChooser(".");
			// openWindow.setFileFilter(fileFilter);
		}

		// Open dialog to choose the file
		int ret = saveWindow.showSaveDialog(frame.getWidget());
		if (ret != JFileChooser.APPROVE_OPTION)
		{
			return;
		}

		// Check the chosen file
		File file = saveWindow.getSelectedFile();
	
		// get table references by the frame
		Table table = ((ImagoTableFrame) frame).getTable();
		
		try
		{
		    new DelimitedTableWriter().writeTable(table, file);
		} 
		catch (IOException ex)
		{
			ex.printStackTrace(System.err);
			// custom title, error icon
			ImagoGui.showErrorDialog(frame,
					"Could not write the table.", "Table I/O Error");
			return;
		}
		catch (Exception ex)
		{
            ImagoGui.showErrorDialog(frame,
                    "Could not write the table.", "Table I/O Error");
			ex.printStackTrace(System.err);
			return;
		}
	}
}
