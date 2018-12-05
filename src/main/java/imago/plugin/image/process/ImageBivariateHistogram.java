/**
 * 
 */
package imago.plugin.image.process;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.scalar.Float32Array2D;
import net.sci.array.scalar.Int32Array2D;
import net.sci.array.scalar.IntArray2D;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.image.ColorMaps;
import net.sci.image.Image;

/**
 * Computes a bivariate histogram between two images with same size.
 * 
 * @author David Legland
 *
 */
public class ImageBivariateHistogram implements Plugin
{
	public ImageBivariateHistogram()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("morphological reconstruction");

		ImagoGui gui = frame.getGui();
		ImagoApp app = gui.getAppli();
		Collection<String> imageNames = app.getImageDocumentNames();

		// Case of no open document with image
		if (imageNames.size() < 2)
		{
		    frame.showErrorDialog("Requires at least two open images to work");
			return;
		}
		
		String[] imageNameArray = imageNames.toArray(new String[]{});
		String firstImageName = imageNameArray[0];
		String secondImageName = imageNameArray[Math.min(1, imageNameArray.length-1)];
        		
		// Creates the dialog
		GenericDialog gd = new GenericDialog(frame, "Morpho. Rec.");
		gd.addChoice("First image: ", imageNameArray, firstImageName);
        gd.addNumericField("Min Bound: ", 0, 2);
        gd.addNumericField("Max Bound: ", 255, 2);
        gd.addNumericField("Number of bins: ", 256, 0);
		gd.addChoice("Second image: ", imageNameArray, secondImageName);
        gd.addNumericField("Min Bound: ", 0, 2);
        gd.addNumericField("Max Bound: ", 255, 2);
        gd.addNumericField("Number of bins: ", 256, 0);
        gd.addCheckBox("Log results", true);
        gd.addCheckBox("remove first bins", false);
        gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// parse dialog results
		Image image1 = app.getDocumentFromName(gd.getNextChoice()).getImage();
        double minBound1 = gd.getNextNumber();
        double maxBound1 = gd.getNextNumber();
        int nBins1 = (int) gd.getNextNumber();
		Image image2 = app.getDocumentFromName(gd.getNextChoice()).getImage();
        double minBound2 = gd.getNextNumber();
        double maxBound2 = gd.getNextNumber();
        int nBins2 = (int) gd.getNextNumber();
        boolean logResults = gd.getNextBoolean();
        boolean removeFirstBins = gd.getNextBoolean();

		// extract arrays and check dimensions
		Array<?> array1 = image1.getData();
		Array<?> array2 = image2.getData();
        if (!Arrays.isSameSize(array1, array2))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays should have same dimensions", "Dimension Error");
            return;
        }
        if (!(array1 instanceof ScalarArray) || !(array2 instanceof ScalarArray))
        {
            ImagoGui.showErrorDialog(frame, "Requires scalar arrays as input", "Data Type Error");
            return;
        }
		
        // create result array
        IntArray2D<?> histo = Int32Array2D.create(nBins1, nBins2);
        
        // Iterate over pixel positions to compute bivariate histogram
        for (int[] pos : array1.positions())
        {
            double value1 = ((ScalarArray<?>) array1).getValue(pos);
            double value2 = ((ScalarArray<?>) array2).getValue(pos);
            int ind1 = (int) Math.min(Math.max(Math.floor(nBins1 * (value1 - minBound1) / (maxBound1 - minBound1)), 0), nBins1-1);
            int ind2 = (int) Math.min(Math.max(Math.floor(nBins2 * (value2 - minBound2) / (maxBound2 - minBound2)), 0), nBins2-1);
            histo.setInt(ind1, ind2, histo.getInt(ind1, ind2) + 1);
        }

        ScalarArray2D<?> result = histo;
        if (logResults)
        {
            result = Float32Array2D.create(nBins1, nBins2);
            for (int ind2 = 0; ind2 < nBins2; ind2++)
            {
                for (int ind1 = 0; ind1 < nBins1; ind1++)
                {
                    result.setValue(ind1, ind2, Math.log(histo.getValue(ind1, ind2) + 1));
                }
            }
        }
        
        if (removeFirstBins)
        {
            ScalarArray2D<?> result2 = (ScalarArray2D<?>) result.newInstance(new int[] { nBins1 - 1, nBins2 - 1 });
            for (int ind2 = 0; ind2 < nBins2 - 1; ind2++)
            {
                for (int ind1 = 0; ind1 < nBins1 - 1; ind1++)
                {
                    result2.setValue(new int[] {ind1, ind2}, result.getValue(ind1, ind2));
                }
            }
            result = result2;
        }
        
        
        Image resultImage = new Image(result);
		resultImage.setName(image1.getName() + "-" + image2.getName() + "-jointHist");
		resultImage.getDisplaySettings().setColorMap(ColorMaps.JET.createColorMap(256));
		
		// add the image document to GUI
		gui.addNewDocument(resultImage);
	}
}
