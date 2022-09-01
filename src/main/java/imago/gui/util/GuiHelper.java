/**
 * 
 */
package imago.gui.util;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * A collection of static methods for facilitating the creation of GUIs.
 * 
 * @author dlegland
 */
public class GuiHelper
{
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
