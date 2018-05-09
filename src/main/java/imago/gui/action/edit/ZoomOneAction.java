/**
 * 
 */
package imago.gui.action.edit;

import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

/**
 * @author David Legland
 *
 */
public class ZoomOneAction extends ImagoAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ZoomOneAction(ImagoFrame frame, String name) {
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// Check type is image frame
		if (!(frame instanceof ImagoDocViewer))
			return;
		ImagoDocViewer iframe = (ImagoDocViewer) frame;
		
		ImageViewer display = iframe.getImageView();
		display.setZoom(1);
		
		display.invalidate();
		iframe.getWidget().validate();
		iframe.repaint();
	}

}
