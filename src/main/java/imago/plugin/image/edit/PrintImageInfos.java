/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Image;
import net.sci.image.ImageAxis;
import net.sci.image.NumericalAxis;

/**
 * @author dlegland
 *
 */
public class PrintImageInfos implements Plugin
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
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image = doc.getImage();

		int nd = image.getDimension();
		
        System.out.println("Image name: " + image.getName());
        System.out.println("Image file: " + image.getFilePath());
        
        System.out.print("Image size: [");
        for (int d = 0; d < nd-1; d++)
            System.out.print(image.getSize(d) + ", ");
        System.out.println(image.getSize(nd-1) + "]");

        System.out.println("Axes calibration:");
        int d = 0;
        for (ImageAxis axis : image.getAxes())
        {
            System.out.print("  Axis[" + (d++) + "]: ");
            if (axis instanceof NumericalAxis)
            {
                NumericalAxis axis2 = (NumericalAxis) axis;
//                System.out.print(axis2.getSpacing() + " " + axis2.getUnitName());
                System.out.print(axis2);
            }
            System.out.println();
        }
    }
}
