/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.vector.VectorArray;
import net.sci.axis.CategoricalAxis;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.table.Table;


/**
 * Convert a vector (or color) image into a data table.
 * 
 * @author David Legland
 *
 */
public class VectorImageToTable implements FramePlugin
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
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
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
		int nChannels = vectorArray.channelCount();
		
		GenericDialog dlg = new GenericDialog(frame, "Convert To Table");
		dlg.addCheckBox("Include Coords", true);
		
		// Display dialog and wait for OK or Cancel
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }

        boolean includeCoords = dlg.getNextBoolean();
        
        // check table can be created
        long nElems = array.elementCount();
        if (nElems > Integer.MAX_VALUE)
        {
            throw new RuntimeException("Array has too many elements to be transformed as Table");
        }

        // input image dimensions
        int nRows = (int) nElems;
        int nDims = array.dimensionality();
        
        Calibration calib = image.getCalibration();
        
        // get channel names from image calibration
        CategoricalAxis channelAxis = calib.getChannelAxis();
        String[] channelNames = new String[nChannels];
        for (int c = 0; c < nChannels; c++)
        {
            channelNames[c] = channelAxis.getItemName(c);
        }
        
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
            for (int d = 0; d < nDims; d++)
            {
                colNames[d] = calib.getAxis(d).getShortName();
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

        // add the new frame to the GUI
        frame.getGui().createTableFrame(table, frame);
	}
}
