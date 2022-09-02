/**
 * 
 */
package imago.gui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * A collection of static methods for facilitating the creation of GUIs.
 * 
 * @author dlegland
 */
public class GuiHelper
{
    /**
     * Puts the given frame in the center of the screen.
     * 
     * @param frame
     */
    public static final void centerFrame(Frame frame)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        
        int posX = screenSize.width / 2 - frameSize.width / 2;
        int posY = Math.max((screenSize.height - frameSize.height) / 4, 0);
        frame.setLocation(posX, posY);
    }
    
    public static final JSpinner createNumberSpinner(double value, double minVal, double maxVal, double step)
    {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, minVal, maxVal, step));
        
        // some hack to make the text fully editable when focus is gained
        // see: https://stackoverflow.com/questions/15328185/make-jspinner-select-text-when-focused
        JTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        textField.addFocusListener(new FocusAdapter()
        {
            public void focusGained(final FocusEvent e)
            {
                SwingUtilities.invokeLater(() -> {textField.selectAll(); textField.grabFocus();});
            }
        });
        return spinner;
    }

    
    
    public JPanel createOptionsPanel(String title)
    {
        JPanel panel = new JPanel();
        panel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(title)));
        panel.setAlignmentX(0.0f);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        return panel;
    }
    
    public void addInLine(JPanel panel, Component... comps)
    {
        addInLine(panel, FlowLayout.LEFT, comps);
    }
    
    public void addInLine(JPanel panel, int alignment, Component... comps)
    {
        JPanel rowPanel = new JPanel(new FlowLayout(alignment));
        rowPanel.setAlignmentX(0.0f);
        for (Component c : comps)
        {
            rowPanel.add(c);
        }
        panel.add(rowPanel);
    }
}
