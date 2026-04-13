/**
 * 
 */
package imago.gui.panels;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author David Legland
 *
 */
public class StatusBar extends JPanel
{
	/**
	 * to comply with Swing conventions
	 */
	private static final long serialVersionUID = 1L;

	/** the label used to explain what to do with the tool.*/
	JLabel toolLabel = new JLabel("");
	
	JLabel cursorLabel = new JLabel("");

	AlgoMonitoringPanel algoMonitorPanel;
	
	public StatusBar()
	{
		// creates a pane for cursor info
		JPanel cursorPanel = new JPanel();
		cursorPanel.setLayout(new BoxLayout(cursorPanel, BoxLayout.PAGE_AXIS));
		cursorPanel.add(this.toolLabel);
		cursorPanel.add(this.cursorLabel);

		// creates a pane for monitoring algorithms
		algoMonitorPanel = new AlgoMonitoringPanel();
		
		// layout panels onto main panel
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(cursorPanel);
		this.add(Box.createHorizontalGlue());
		this.add(algoMonitorPanel);
        
		this.invalidate();
	}
	
	public String getToolLabel()
	{
		return toolLabel.getText();
	}

	public void setToolLabel(String toolLabel) 
	{
		this.toolLabel.setText(toolLabel);
	}

	public String getCursorLabel() 
	{
		return cursorLabel.getText();
	}

	public void setCursorLabel(String cursorLabel) 
	{
		this.cursorLabel.setText(cursorLabel);
	}

	public void setCurrentStepLabel(String string)
	{
		this.algoMonitorPanel.setStatusMessage(string);
	}
	
	/**
	 * Update the progress bar with the given amount in percent.
	 * 
	 * @param percent the current progress, between 0 and 100.
	 */
	public void setProgressBarPercent(int percent)
	{
	    this.algoMonitorPanel.setProgressPercent(percent);
	}
}
