/**
 * 
 */
package imago.plugin.table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.table.TableFrame;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableReader;


/**
 * Opens a dialog to choose a table file, typically in CSV format, and opens the
 * selected file in a new image frame.
 * 
 * @author David Legland
 *
 */
public class OpenTable implements FramePlugin
{
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String options)
	{
        // If options is given, use it to choose the file
        if (options != null && !options.isEmpty())
        {
            String fileName = FramePlugin.parseOptionsString(options).get("fileName");
            if (fileName != null)
            {
                Table table = readTable(fileName, frame);
                if (table == null) return;

                // setup meta-data
                table.setName(fileName);
                
                // add the new frame to the GUI
                TableFrame.create(table, frame);
                return;
            }
        }

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
	
    private Table readTable(String pathToFile, ImagoFrame frame)
    {
        // builds a "standard" CSV format table reader
        DelimitedTableReader reader = DelimitedTableReader.builder()
                .delimiters(",")
                .readHeader(true)
                .readRowNames(false)
                .build();
        
        // First try to read the table from within the jar
        try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(pathToFile))
        {
            if (stream != null)
            {
                return reader.readTable(stream);
            }
        }
        catch(Exception ex)
        {
            // could not find within jar, so continue with local file system
        }
        
        // If table could not be found, try with more standard method
        try 
        {
            return reader.readTable(new File(pathToFile));
        }
        catch (FileNotFoundException ex)
        {
            // ex.printStackTrace(System.err);
            frame.showErrorDialog("Could not find the file: " + pathToFile);
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
            frame.showErrorDialog(ex.getMessage(), "File Input Error");
        }
        
        return null;
    }
}
