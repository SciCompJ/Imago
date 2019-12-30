/**
 * 
 */
package imago.gui.panel;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class CollapsiblePanel extends JPanel implements MouseListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private JPanel titlePanel;
    private JLabel plusMinusLabel = new JLabel("+");
    private JPanel content;
    
    boolean expanded = false;

    public CollapsiblePanel(String title, JPanel content)
    {
        this.content = content;
        setLayout( new BorderLayout() );
        
        titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel(title);
        titlePanel.add(plusMinusLabel);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.PAGE_START);

        this.content.setVisible(false);
        add(this.content, BorderLayout.CENTER);

        titlePanel.addMouseListener(this);
    }

    
    @Override
    public Dimension getMaximumSize()
    {
        Dimension size = getPreferredSize();
        size.width = Integer.MAX_VALUE;

        return size;
    }

    @Override
    public void mouseClicked(MouseEvent evt)
    {
        if (evt.getSource() == titlePanel)
        {
            toggleVisibility();
        }
    }


    @Override
    public void mouseEntered(MouseEvent arg0)
    {
    }


    @Override
    public void mouseExited(MouseEvent arg0)
    {
    }


    @Override
    public void mousePressed(MouseEvent arg0)
    {
    }


    @Override
    public void mouseReleased(MouseEvent arg0)
    {
    }

    
    private void toggleVisibility()
    {
        this.expanded = !this.expanded;
        
        plusMinusLabel.setText(this.expanded ? "-" : "+");
        content.setVisible(this.expanded);

        revalidate();
        repaint();
    }
    
    private static void createAndShowGUI()
    {
        Box content = Box.createVerticalBox();

        content.add( createCollapsiblePanel("Red", Color.RED, 50));
        content.add( createCollapsiblePanel("Blue", Color.BLUE, 100));
        content.add( createCollapsiblePanel("Green", Color.GREEN, 200));

        JFrame frame = new JFrame("Expanding Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.add( content);
        frame.add( new JScrollPane(content));
        frame.setLocationByPlatform( true );
        frame.setSize(200, 300);
        frame.setVisible( true );
    }


    private static CollapsiblePanel createCollapsiblePanel(String title, Color background, int preferredHeight)
    {
        JPanel content = new JPanel();
        content.setBackground(background);
        content.setPreferredSize(new Dimension(100, preferredHeight));

        return new CollapsiblePanel(title, content);
    }


    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                createAndShowGUI();
            }
        });
    }


}
