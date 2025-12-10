/**
 * 
 */
package imago.chart.plugins;

import java.io.File;
import java.io.IOException;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.internal.chartpart.Chart;

import imago.chart.ChartFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;


/**
 * Opens a dialog to choose a file to save the image of a chart.
 * 
 * @author David Legland
 *
 */
public class SaveChart implements ChartPlugin
{
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
    public void run(ImagoFrame frame, String args)
	{
        // get table references by the frame
        Chart<?,?> chart = ((ChartFrame) frame).getChart();
        
        // opens a dialog to choose the file
        String defaultName = frame.getWidget().getTitle() + ".png";
        File file = frame.getGui().chooseFileToSave(frame, "Save Chart", defaultName,
                ChartImageFileFilters.COMMON, ChartImageFileFilters.PNG, ChartImageFileFilters.BMP,
                ChartImageFileFilters.JPEG, ChartImageFileFilters.GIF);
        if (file == null)
        {
            return;
        }
        
		try
		{
            if (file.getName().endsWith(".png"))
            {
                BitmapEncoder.saveBitmap(chart, file.getAbsolutePath(), BitmapFormat.PNG);
            }
            else if (file.getName().endsWith(".BMP"))
            {
                BitmapEncoder.saveBitmap(chart, file.getAbsolutePath(), BitmapFormat.BMP);
            }
            else if (file.getName().endsWith(".JPG"))
            {
                BitmapEncoder.saveBitmap(chart, file.getAbsolutePath(), BitmapFormat.JPG);
            }
            else if (file.getName().endsWith(".GIF"))
            {
                BitmapEncoder.saveBitmap(chart, file.getAbsolutePath(), BitmapFormat.GIF);
            }
            else
            {
                throw new RuntimeException("Unknown extension: " + file.getName());
            }
		} 
		catch (IOException ex)
		{
			ex.printStackTrace(System.err);
            ImagoGui.showErrorDialog(frame, "Could not write the chart.", "Chart I/O Error");
            return;
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
            ImagoGui.showErrorDialog(frame, "Could not write the chart.", "Chart I/O Error");
			return;
		}
	}
}
