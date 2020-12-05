/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.FramePlugin;
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
		System.out.println("print image info:");
		
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image = doc.getImage();

		int nd = image.getDimension();
		
        System.out.println("Image name: " + image.getName());
        System.out.println("Image file: " + image.getFilePath());
        
        System.out.print("Image size: [");
        for (int d = 0; d < nd-1; d++)
        {
            System.out.print(image.getSize(d) + ", ");
        }
        System.out.println(image.getSize(nd-1) + "]");

        // Show infos about axes (usually space+time)
        System.out.println("Axes calibration:");
        Calibration calib = image.getCalibration();
        for (int d = 0; d < image.getDimension(); d++)
        {
            System.out.println("  Axis[" + d + "]: " + calib.getAxis(d));
        }

        // Show infos about channels
        System.out.println("Channels info:");
        Axis channelAxis = calib.getChannelAxis();
        System.out.println("  name: " + channelAxis.getName());
        System.out.println("  string: " + channelAxis.toString());
    }
}
