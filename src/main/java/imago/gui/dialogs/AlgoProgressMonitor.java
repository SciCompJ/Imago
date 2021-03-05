/**
 * 
 */
package imago.gui.dialogs;

import javax.swing.ProgressMonitor;

import imago.gui.ImagoFrame;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;

/**
 * Encapsulates a progress monitor, and updates it according to the AlgoEvent
 * received by this AlgoListener.
 * 
 * @author dlegland
 *
 */
public class AlgoProgressMonitor implements AlgoListener
{
    ProgressMonitor progressMonitor;
    
    /**
     * Creates a new AlgoProgressMonitor from a parent frame, with the specified
     * title.
     * 
     * @param parentFrame
     *            the parent frame used to localise the dialog
     * @param title
     *            the title of the progress dialog
     */
    public AlgoProgressMonitor(ImagoFrame parentFrame, String title)
    {
        // create the progress monitor
        progressMonitor = new ProgressMonitor(parentFrame.getWidget(), title, "", 0, 100);
        
        // add specific setup
        progressMonitor.setMillisToDecideToPopup(10);
        progressMonitor.setMillisToPopup(100);
        progressMonitor.setProgress(0);
    }
    
    /**
     * Updates the progress dialog with the specified ratio, between 0 and 1.
     * 
     * @param progressRatio
     *            the relative progress, as a floating point value between 0.0
     *            and 1.0.
     */
    public void setProgressRatio(double progressRatio)
    {
        int progress = (int) Math.round(100 * progressRatio);
        progressMonitor.setProgress(progress);
    }

    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
        int progress = (int) Math.round(evt.getProgressRatio() * 100);
        progressMonitor.setProgress(progress);
    }

    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
        progressMonitor.setNote(evt.getStatus());
    }
}
