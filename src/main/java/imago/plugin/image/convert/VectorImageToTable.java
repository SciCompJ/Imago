/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.ImagoTableFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;
import net.sci.table.Table;


/**
 * Convert a vector (or color) image into a data table.
 * 
 * @author David Legland
 *
 */
public class VectorImageToTable implements Plugin
{
	public VectorImageToTable()
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("convert a vector image into a data table");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image = doc.getImage();
		
		if (image == null)
		{
			return;
		}
		Array<?> array = image.getData();
		if (array == null)
		{
			return;
		}
		if (!(array instanceof VectorArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires a Vector image", "Data Type Error");
			return;
		}

		VectorArray<?> vectorArray = (VectorArray<?>) array;
		int nChannels = vectorArray.channelNumber();
		
		GenericDialog dlg = new GenericDialog(frame, "Convert To Table");
		dlg.addCheckBox("Include Coords", true);
		
		// Display dialog and wait for OK or Cancel
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }

        boolean includeCoords = dlg.getNextBoolean();
        
        // create default channel names
        String[] channelNames = new String[nChannels];
        int nDigits = (int) Math.ceil(Math.log10(nChannels));
        String pattern = "C%0" + nDigits + "d";
        for (int c = 0; c < nChannels; c++)
        {
            channelNames[c] = String.format(pattern, c);
        }
        int nRows = array.elementNumber();
        int nDims = array.dimensionality();
        
        // create the table
        Table table;
        if (includeCoords)
        {
            int nCols = nDims + nChannels;
            table = Table.create(nRows, nCols);
            int row = 0;
            for (int[] pos : vectorArray.positions())
            {
                for (int d = 0; d < nDims; d++)
                {
                    table.setValue(row, d, pos[d]);
                }
                double[] values = vectorArray.getValues(pos);
                for (int c = 0; c < nChannels; c++)
                {
                    table.setValue(row, nDims + c, values[c]);
                }
                row++;
            }
            
            String[] colNames = new String[nCols];

            // create column names for dimensions
            String[] dimDigits = new String[]{"X", "Y", "Z", "T", "U", "V"};
            for (int d = 0; d < nDims; d++)
            {
                colNames[d] = dimDigits[d];
            }

            // create column names for channels
            for (int c = 0; c < nChannels; c++)
            {
                colNames[nDims + c] = channelNames[c];
            }

            table.setColumnNames(colNames);
        }
        else
        {
            table = Table.create(nRows, nChannels);
            int row = 0;
            for (int[] pos : vectorArray.positions())
            {
                double[] values = vectorArray.getValues(pos);
                for (int c = 0; c < nChannels; c++)
                {
                    table.setValue(row, c, values[c]);
                }
                row++;
            }

            table.setColumnNames(channelNames);
        }
        
        table.setName(image.getName() + "-values");
        ImagoTableFrame tableFrame = new ImagoTableFrame(frame, table);
        
        // add the new frame to the GUI
        frame.getGui().addFrame(tableFrame); 

	}

}
