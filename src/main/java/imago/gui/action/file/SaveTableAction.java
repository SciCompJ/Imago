/**
 * 
 */
package imago.gui.action.file;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.ImagoTableFrame;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import net.sci.table.Table;
import net.sci.table.io.DelimitedTableWriter;


/**
 * @author David Legland
 *
 */
public class SaveTableAction extends ImagoAction
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private JFileChooser saveWindow = null;
    
    public SaveTableAction(ImagoFrame frame, String name)
    {
        super(frame, name);
    }
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		// create file dialog if it doesn't exist
		if (saveWindow == null)
		{
			saveWindow = new JFileChooser(".");
			// openWindow.setFileFilter(fileFilter);
		}

		// Open dialog to choose the file
		int ret = saveWindow.showSaveDialog(this.frame.getWidget());
		if (ret != JFileChooser.APPROVE_OPTION)
		{
			return;
		}

		// Check the chosen file
		File file = saveWindow.getSelectedFile();
	
		Table table = ((ImagoTableFrame) frame).getTable();
		
		try
		{
		    new DelimitedTableWriter().writeTable(table, file);
		} 
		catch (IOException ex)
		{
			ex.printStackTrace(System.err);
			// custom title, error icon
			ImagoGui.showErrorDialog(this.frame,
					"Could not write the table.", "Table I/O Error");
			return;
		}
		catch (Exception ex)
		{
            ImagoGui.showErrorDialog(this.frame,
                    "Could not write the table.", "Table I/O Error");
			ex.printStackTrace(System.err);
			return;
		}
	}
}
