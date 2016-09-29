/**
 * 
 */
package imago.gui.panel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author David Legland
 *
 */
public class StatusBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** the label used to explain what to do with the tool.*/
	JLabel toolLabel = new JLabel("");
	
	JLabel cursorLabel = new JLabel("");

	public StatusBar() {
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(this.toolLabel);
		this.add(new JLabel(" "));
		this.add(this.cursorLabel);
		this.add(Box.createHorizontalGlue());
		this.invalidate();
	}
	
	public String getToolLabel() {
		return toolLabel.getText();
	}

	public void setToolLabel(String toolLabel) {
		this.toolLabel.setText(toolLabel);
	}

	public String getCursorLabel() {
		return cursorLabel.getText();
	}

	public void setCursorLabel(String cursorLabel) {
		this.cursorLabel.setText(cursorLabel);
	}

	
}
