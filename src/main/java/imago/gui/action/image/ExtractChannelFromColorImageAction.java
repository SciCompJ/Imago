/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.data.Float64Array;
import net.sci.array.data.ScalarArray;
import net.sci.array.data.UInt8Array;
import net.sci.array.data.color.RGB8Array;
import net.sci.image.Image;


/**
 * Extract a speciofied channel from color image
 * 
 * @author David Legland
 *
 */
public class ExtractChannelFromColorImageAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ExtractChannelFromColorImageAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("extract a channel from color image");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
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
		if (!(array instanceof RGB8Array))
		{
            ImagoGui.showErrorDialog(frame, "Requires a RGB8 image", "Data Type Error");
			return;
		}

		String[] channelNames = new String[]{"Red", "Green", "Blue", "Hue", "Saturation", "Luminance"};
		
		GenericDialog dlg = new GenericDialog(this.frame, "Extract Channel");
		dlg.addChoice("Channel Name ", channelNames, channelNames[5]);
		dlg.addCheckBox("8-bits result", true);
		
		// Display dialog and wait for OK or Cancel
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // extract user choices
        int channelIndex = dlg.getNextChoiceIndex();
        boolean convertToUInt8 = dlg.getNextBoolean();

        // allocate memory for result array
        ScalarArray<?> channelArray = null;
        if (convertToUInt8)
        {
            channelArray = UInt8Array.create(array.getSize());
        }
        else
        {
            channelArray = Float64Array.create(array.getSize());
        }
        
        // create iterators
        RGB8Array.Iterator rgb8Iter = ((RGB8Array) array).iterator();
        ScalarArray.Iterator<?> channelIter = channelArray.iterator();
        
        // constant to eventually normalize between 0 and 255
        double k = convertToUInt8 ? 255.0 : 1.0;
        
        // fill result array with adequate channel
        switch(channelIndex)
        {
        case 0: // Red
            while(rgb8Iter.hasNext() && channelIter.hasNext())
            {
                channelIter.setNextValue(rgb8Iter.next().getValue(0));
            }
            break;
        case 1: // Green
            while(rgb8Iter.hasNext() && channelIter.hasNext())
            {
                channelIter.setNextValue(rgb8Iter.next().getValue(1));
            }
            break;
        case 2: // Blue
            while(rgb8Iter.hasNext() && channelIter.hasNext())
            {
                channelIter.setNextValue(rgb8Iter.next().getValue(2));
            }
            break;
        case 3: // Hue
            while(rgb8Iter.hasNext() && channelIter.hasNext())
            {
                channelIter.setNextValue(rgb8Iter.next().getHue() * k);
            }
            break;
        case 4: // Saturation
            while(rgb8Iter.hasNext() && channelIter.hasNext())
            {
                channelIter.setNextValue(rgb8Iter.next().getSaturation() * k);
            }
            break;
        case 5: // Luminance
            while(rgb8Iter.hasNext() && channelIter.hasNext())
            {
                channelIter.setNextValue(rgb8Iter.next().getLuminance() * k);
            }
            break;
        }
        
		Image resultImage = new Image(channelArray, image);
		String name = image.getName();
		resultImage.setName(name + "-" + channelNames[channelIndex]);
				
		// add the image document to GUI
		this.gui.addNewDocument(resultImage); 
	}

}
