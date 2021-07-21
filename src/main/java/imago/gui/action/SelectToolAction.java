/**
 * 
 */
package imago.gui.action;

import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoTool;
import imago.gui.frames.ImageFrame;

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

	ImagoTool tool;
	
	public SelectToolAction(ImageFrame viewer, ImagoTool tool) {
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
		ImageViewer viewer = ((ImageFrame) this.frame).getImageView();
		viewer.setCurrentTool(tool);
	}

}
