/**
 * 
 */
package imago.gui;


import javax.swing.AbstractAction;

/**
 * @author David Legland
 *
 */
public abstract class ImagoAction extends AbstractAction {

	// ===================================================================
	// class variables

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected ImagoGui gui;

	protected ImagoFrame frame;

	protected String name;
	
	protected ImagoAction(ImagoFrame frame, String name) {
		this.frame = frame;
		this.name = name;
		this.gui = frame.gui;
	}
	
	public String getName() {
		return this.name;
	}
}
