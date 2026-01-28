/**
 * 
 */
package imago.table.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableReader;


/**
 * Opens a sample table embedded within the jar file of the application.
 * 
 */
public class OpenDemoTable implements FramePlugin
{
    String pathToResource;
    
	public OpenDemoTable(String pathToResource) 
	{
	    this.pathToResource = pathToResource;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
    public void run(ImagoFrame frame, String args)
    {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(pathToResource);
        if (stream == null)
        {
            throw new IllegalArgumentException("Could not retrieve table: " + pathToResource);
        }
        
        DelimitedTableReader reader = new DelimitedTableReader()
                .setDelimiters(",")
                .setReadRowNames(false);
	    Table table;
        try
        {
            table = reader.readTable(stream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
	    
        String fileName = Paths.get(pathToResource).getFileName().toString();
        table.setName(fileName);
        
        // add the new frame to the GUI
        TableFrame.create(table, frame);
	}

}
