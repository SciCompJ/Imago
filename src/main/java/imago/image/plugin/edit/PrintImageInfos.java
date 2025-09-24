/**
 * 
 */
package imago.image.plugin.edit;

import java.awt.Dimension;
import java.util.ArrayList;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoTextFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.binary.RunLengthBinaryArray2D;
import net.sci.array.binary.RunLengthBinaryArray3D;
import net.sci.axis.Axis;
import net.sci.image.Calibration;
import net.sci.image.DisplaySettings;
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
        String sizeText = "Image size: " + image.getSize(0);
        for (int d = 1; d < nd; d++)
        {
            sizeText += " x " + image.getSize(d);
        }
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
        textLines.add("  Name: " + channelAxis.getName());
        textLines.add("  String: " + channelAxis.toString());
        
        textLines.add("Display settings:");
        DisplaySettings ds = image.getDisplaySettings();
        double[] displayRange = ds.getDisplayRange();
        textLines.add(String.format("  Display range: [%f ; %f]", displayRange[0], displayRange[1]));
//        textLines.add(String.format("  Color map: ", displayRange[0], displayRange[1]));
        textLines.add(String.format("  Background Color: %s", ds.getBackgroundColor().toString()));
        
        // Show technical info about Array instance
        Array<?> array = image.getData();
        textLines.add("Inner representation:");
        textLines.add("  Array class: " + array.getClass());
        if (array instanceof RunLengthBinaryArray2D)
        {
            textLines.add("  Number of runs: " + ((RunLengthBinaryArray2D) array).runCount());
        }
        else if (array instanceof RunLengthBinaryArray3D)
        {
            textLines.add("  Number of runs: " + ((RunLengthBinaryArray3D) array).runCount());
        }
        
        ImagoTextFrame newFrame = new ImagoTextFrame(frame, "Image Info", textLines);
        newFrame.getWidget().pack();
        newFrame.getWidget().setSize(new Dimension(600, 400));
        newFrame.setVisible(true);
    }
}
