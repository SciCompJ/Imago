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
import net.sci.table.io.DelimitedTableReader;


/**
 * @author David Legland
 *
 */
public class OpenTableAction extends ImagoAction
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private JFileChooser openWindow = null;
    
    public OpenTableAction(ImagoFrame frame, String name)
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
		if (openWindow == null)
		{
			openWindow = new JFileChooser(".");
			// openWindow.setFileFilter(fileFilter);
		}

		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(this.frame.getWidget());
		if (ret != JFileChooser.APPROVE_OPTION)
		{
			return;
		}

		// Check the chosen file is state
		File file = openWindow.getSelectedFile();
		if (!file.isFile())
		{
			return;
		}

		Table table;
		try
		{
		    table = new DelimitedTableReader().readTable(file);
		} 
		catch (IOException ex)
		{
			ex.printStackTrace(System.err);
			// custom title, error icon
			ImagoGui.showErrorDialog(this.frame,
					"Could not read the table.", "Table I/O Error");
			return;
		}
		catch (Exception ex)
		{
            ImagoGui.showErrorDialog(this.frame,
                    "Could not read the table.", "Table I/O Error");
			ex.printStackTrace(System.err);
			return;
		}

		table.setName(file.getName());
		
        // add the new frame to the GUI
		ImagoTableFrame frame = new ImagoTableFrame(this.frame, table);
        this.gui.addFrame(frame); 
	}
}
