/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.Float64Array;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;

/**
 * @see CreateVectorImageRGB8View
 * 
 * @author David Legland
 *
 */
public class ConvertChannelsToDimension implements Plugin
{
	public ConvertChannelsToDimension()
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
	    System.out.println("vector image to scalar with one dimension more");

        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getDocument();
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

        // dimensions of input array
        VectorArray<?> vectorArray = (VectorArray<?>) array;
        int nd = array.dimensionality();
        int[] dims = vectorArray.size();
        int nChannels = vectorArray.channelNumber();
        
        // create result array
        int[] dims2 = new int[nd + 1];
        System.arraycopy(dims, 0, dims2, 0, nd);
        dims2[nd] = nChannels;
		Float64Array res = Float64Array.create(dims2);
		
		// iterate over positions of input array
		int[] pos2 = new int[nd + 1];
		for (int[] pos : vectorArray.positions())
		{
		    System.arraycopy(pos, 0, pos2, 0, nd);
		    double[] values = vectorArray.getValues(pos);
		    
		    for (int c = 0; c < nChannels; c++)
		    {
		        pos2[nd] = c;
		        res.setValue(values[c], pos2);
		    }
		}
		
		// create the image corresponding to channels concatenation
		Image resImage = new Image(res);
		resImage.setName(image.getName() + "-convert");
		

		// add the image document to GUI
		frame.getGui().addNewDocument(resImage);
	}
	
}
