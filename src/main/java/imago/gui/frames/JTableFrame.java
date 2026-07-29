/**
 * 
 */
package imago.gui.frames;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import imago.gui.ImagoFrame;
import imago.table.RowNumberTable;

/**
 * A simple frame containing only a JTable instance initialized with the content
 * of the array provided at construction.
 */
public class JTableFrame extends ImagoFrame
{
    /**
     * Creates a new frame displaying the content of the specified array of
     * data.
     * 
     * @param parent
     *            the parent frame
     * @param frameTitle
     *            the title of the frame
     * @param data
     *            the data table
     * @param colNames
     *            the names of the columns
     */
    public JTableFrame(ImagoFrame parent, String frameTitle, Object[][] data, String[] colNames)
    {
        super(parent, frameTitle);
        
        setupLayout(data, colNames);
        
        JFrame frame = (JFrame) getWidget();
        frame.pack();
        this.setVisible(true);

        // setup window listener
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                JTableFrame.this.close();
            }           
        });
    }

    private void setupLayout(Object[][] data, String[] colNames) 
    {
        // put into global layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // create JTable object
        JTable jtable = new JTable(data, colNames);
        
        //add the table to the frame
        JScrollPane scrollPane = new JScrollPane(jtable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // decorate the scroll panel with label column
        JTable rowTable = new RowNumberTable(jtable);
        scrollPane.setRowHeaderView(rowTable);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
        
        ((JFrame) this.getWidget()).setContentPane(mainPanel);
    }
}
