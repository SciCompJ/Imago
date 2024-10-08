/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.Float64VectorArray;
import net.sci.array.numeric.ScalarArray;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertDimensionToChannels implements FramePlugin
{
	public ConvertDimensionToChannels() 
	{
		super();
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
		if (!(array instanceof ScalarArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires a scalar array", "Data Type Error");
			return;
		}

        // dimensions of input array
        int nd = array.dimensionality();
        int[] dims = array.size();
        int nChannels = dims[nd-1];
        
        // create result array
        int[] dims2 = new int[nd - 1];
        System.arraycopy(dims, 0, dims2, 0, nd - 1);
        Float64VectorArray res = Float64VectorArray.create(dims2, nChannels);
        
        // iterate over positions of result array
        int[] pos = new int[nd];
        for (int[] pos2 : res.positions())
        {
            System.arraycopy(pos2, 0, pos, 0, nd-1);
            
            for (int c = 0; c < nChannels; c++)
            {
                pos[nd-1] = c;
                res.setValue(pos2, c, ((ScalarArray<?>) array).getValue(pos));
            }
        }

        // create the image corresponding to channels concatenation
        Image resultImage = new Image(res);
        resultImage.setName(image.getName() + "-convert");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

}
