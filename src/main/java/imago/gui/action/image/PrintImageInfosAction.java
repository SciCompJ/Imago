/**
 * 
 */
package imago.gui.action.image;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.image.Image;

/**
 * @author dlegland
 *
 */
public class PrintImageInfosAction extends ImagoAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PrintImageInfosAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("print image info:");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		int nd = image.getDimension();
		
        System.out.println("Image name: " + image.getName());
        System.out.println("Image file: " + image.getFilePath());
        
        System.out.print("Image size: [");
        for (int d = 0; d < nd-1; d++)
            System.out.print(image.getSize(d) + ", ");
        System.out.println(image.getSize(nd-1) + "]");

        double[] calib = image.getSpatialCalibration().getResolutions();
        System.out.print("Spatial Calibration: [");
        for (int d = 0; d < nd-1; d++)
            System.out.print(calib[d] + ", ");
        System.out.println(calib[nd-1] + "]");
        System.out.print("Spatial Calibration Unit: " +  image.getSpatialCalibration().getUnit());
        
    }

}
