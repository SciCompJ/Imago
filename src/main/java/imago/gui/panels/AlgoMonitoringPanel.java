/**
 * 
 */
package imago.gui.panels;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * A simple panel composed of a label and a progress bar, used to monitor
 * progression of algorithms.
 */
public class AlgoMonitoringPanel extends JPanel
{
    /**
     * to comply with Swing conventions
     */
    private static final long serialVersionUID = 1L;


    JLabel statusLabel = new JLabel(" ");

    JProgressBar progressBar;
    
    public AlgoMonitoringPanel()
    {
        super();
        
        setupLayout();
        this.invalidate();
    }
    
    private void setupLayout()
    {
        // initialize widgets
        this.statusLabel = new JLabel(" ");
        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setStringPainted(true);
        
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(statusLabel);
        add(progressBar);
    }
    
    public void setStatusMessage(String string)
    {
        this.statusLabel.setText(string);
    }
    
    /**
     * Update the progress bar with the given amount in percent.
     * 
     * @param percent the current progress, between 0 and 100.
     */
    public void setProgressPercent(int percent)
    {
        this.progressBar.setValue(percent);
        this.progressBar.invalidate();
        this.validate();
        this.repaint();
    }
}
