/**
 * 
 */
package imago.plugin.image.edit;

import java.awt.Dimension;
import java.util.ArrayList;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.frames.ImagoTextFrame;
import net.sci.axis.Axis;
import net.sci.image.Calibration;
import net.sci.image.Image;

/**
 * @author dlegland
 *
 */
public class PrintImageInfos implements FramePlugin
{
	public PrintImageInfos() 
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
		
		ArrayList<String> textLines = new ArrayList<String>();

		textLines.add("Image name: " + image.getName());
		textLines.add("Image file: " + image.getFilePath());
        
        int nd = image.getDimension();
        String sizeText = "Image size: [" + image.getSize(0);
        for (int d = 1; d < nd; d++)
        {
            sizeText += ", " + image.getSize(d);
        }
        sizeText += "]";
        textLines.add(sizeText);

        // Show infos about axes (usually space+time)
        textLines.add("Axes calibration:");
        Calibration calib = image.getCalibration();
        for (int d = 0; d < image.getDimension(); d++)
        {
            textLines.add("  Axis[" + d + "]: " + calib.getAxis(d));
        }

        // Show infos about channels
        textLines.add("Channels info:");
        Axis channelAxis = calib.getChannelAxis();
        textLines.add("  name: " + channelAxis.getName());
        textLines.add("  string: " + channelAxis.toString());
        
        // Show technical info about Array instance
        textLines.add("Inner representation:");
        textLines.add("  Array class: " + image.getData().getClass());
        
        ImagoTextFrame newFrame = new ImagoTextFrame(frame, "Image Info", textLines);
        newFrame.getWidget().pack();
        newFrame.getWidget().setSize(new Dimension(600, 400));
        newFrame.setVisible(true);
    }
}
