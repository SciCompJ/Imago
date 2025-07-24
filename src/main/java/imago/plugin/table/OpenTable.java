/**
 * 
 */
package imago.plugin.table;

import java.io.File;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.table.TableFrame;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableReader;


/**
 * @author David Legland
 *
 */
public class OpenTable implements FramePlugin
{
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
        // opens a dialog to choose the file
        File file = frame.getGui().chooseFileToOpen(frame, "Open Table");
        if (file == null)
        {
            return;
        }
        
        // Check the chosen file is valid
        if (!file.isFile())
        {
            ImagoGui.showErrorDialog(frame,
                    "Could not find the selected file: " + file.getName(),
                    "Image I/O Error");
            return;
        }
        
        GenericDialog dlg = new GenericDialog(frame, "Table Options");
        dlg.addTextField("Delimiters", " \t");
        dlg.addCheckBox("Header", true);
        dlg.addNumericField("Skip lines", 0, 0);
        dlg.addCheckBox("Read Row Names", false);
        
        // wait for user input
        dlg.showDialog();
        if (dlg.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        DelimitedTableReader reader = DelimitedTableReader.builder()
                .delimiters(dlg.getNextString())
                .readHeader(dlg.getNextBoolean())
                .skipLines((int) dlg.getNextNumber())
                .readRowNames(dlg.getNextBoolean())
                .build();

        // try reading the table
        Table table;
        try
        {
            table = reader.readTable(file);
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
            ImagoGui.showErrorDialog(frame, "Could not read the table.", "Table I/O Error");
            return;
        }
        
        table.setName(file.getName());
        
        // add the new frame to the GUI
        TableFrame.create(table, frame);
    }
}
