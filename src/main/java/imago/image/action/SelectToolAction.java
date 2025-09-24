/**
 * 
 */
package imago.image.action;

import imago.gui.ImagoAction;
import imago.image.ImageFrame;
import imago.image.ImageTool;
import imago.image.ImageViewer;

import java.awt.event.ActionEvent;

/**
 * @author David Legland
 *
 */
public class SelectToolAction extends ImagoAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ImageTool tool;
	
	public SelectToolAction(ImageFrame viewer, ImageTool tool) {
		super(viewer, "select-" + tool.getName());
		this.tool = tool;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.out.println("Select tool: " + tool.getName());
		
		// get current frame
		ImageViewer viewer = ((ImageFrame) this.frame).getImageViewer();
		viewer.setCurrentTool(tool);
	}

}
