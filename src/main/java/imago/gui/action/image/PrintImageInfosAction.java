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
import net.sci.image.ImageAxis;
import net.sci.image.NumericalAxis;

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

        System.out.println("Axes calibration:");
        int d = 0;
        for (ImageAxis axis : image.getAxes())
        {
            System.out.print("  Axis[" + (d++) + "]: ");
            if (axis instanceof NumericalAxis)
            {
                NumericalAxis axis2 = (NumericalAxis) axis;
                System.out.print(axis2.getSpacing() + " " + axis2.getUnitName());
            }
            System.out.println();
        }
    }
}
