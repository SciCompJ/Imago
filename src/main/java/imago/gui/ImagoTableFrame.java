/**
 * 
 */
package imago.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import imago.gui.util.RowNumberTable;
import net.sci.table.Table;

/**
 * @author dlegland
 *
 */
public class ImagoTableFrame extends ImagoFrame
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // ===================================================================
    // Class variables

    Table table;
    
    // ===================================================================
    // Constructor
    
    /**
     * @param parent
     */
    public ImagoTableFrame(ImagoFrame parent, Table table)
    {
        super(parent, "Table Frame");
        this.table = table;

        // create menu
        GuiBuilder builder = new GuiBuilder();
        builder.createMenuBar(this);
        
//        // Create a status bar
//        this.statusBar = new StatusBar();
//
//        // create default viewer for image
//        createDefaultImageViewer();
        
        // layout the frame
        setupLayout();
         
        this.setTitle("Table Example");
        this.pack();
        this.setVisible(true);
 
        
        doLayout();
        updateTitle();
        
        // setup window listener
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                gui.removeFrame(ImagoTableFrame.this);
                ImagoTableFrame.this.dispose();
            }           
        });
        
        putFrameMiddleScreen();
    }
    
    private void setupLayout() 
    {
        // put into global layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.GREEN);

        // Table header
        String[] colNames = table.getColumnNames();
         
        // Convert numeric values to table of objects
        int nRows = table.getRowNumber();
        int nCols = table.getColumnNumber();
        Object[][] data = new Object[nRows][nCols];
        for (int i = 0; i < nRows; i++)
        {
            Object[] row = new Object[nCols];
            for (int j = 0; j < nCols; j++)
            {
                row[j] = table.get(i, j);
            }
            data[i] = row;
        };
        
        // create JTable object
        JTable jtable = new JTable(data, colNames);
        
        //add the table to the frame
        JScrollPane scrollPane = new JScrollPane(jtable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // decorate the scroll panel with label column
        JTable rowTable = new RowNumberTable(jtable);
        scrollPane.setRowHeaderView(rowTable);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
        
//        mainPanel.add(imageView);
//        mainPanel.add(this.statusBar, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    private void putFrameMiddleScreen()
    {
        // set up frame size depending on screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(800, screenSize.width - 100);
        int height = Math.min(700, screenSize.width - 100);
        Dimension frameSize = new Dimension(width, height);
        this.setSize(frameSize);

        // set up frame position depending on frame size
        int posX = (screenSize.width - width) / 4;
        int posY = (screenSize.height - height) / 4;
        this.setLocation(posX, posY);
    }

    // ===================================================================
    // General methods
    
    public void updateTitle()
    {
        // table name
        String name;
//        String name = this.table.getName();
//        if (name == null || name.isEmpty()) 
//        {
            name = "No Name";
//        }
        
        String dimString = "(unknown size)";
        int dim[] = this.table.getSize();
        if (dim.length == 2) 
        {
            dimString = dim[0] + "x" + dim[1];
        } 
        else if (dim.length == 3) 
        {
            dimString = dim[0] + "x" + dim[1] + "x" + dim[2];
        } 
        else if (dim.length == 4) 
        {
            dimString = dim[0] + "x" + dim[1] + "x" + dim[2] + "x" + dim[3];
        }
        
        String titleString = name + " - " + dimString;
        this.setTitle(titleString);
    }
    
    public Table getTable()
    {
        return this.table;
    }
    
    // ===================================================================
    // Display management methods

}