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
public class ZoomOutAction extends ImagoAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ZoomOutAction(ImagoFrame gui, String name) {
		super(gui, name);
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
		
		ImageViewer view = iframe.getImageView();
		double zoom = view.getZoom();
		zoom = zoom / 2;
		view.setZoom(zoom);
		
		view.invalidate();
		iframe.validate();
		iframe.repaint();
	}

}
