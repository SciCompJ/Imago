/**
 * 
 */
package imago.gui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import imago.app.ImagoApp;
import imago.app.TableHandle;
import imago.gui.GuiBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.axis.Axis;
import net.sci.axis.CategoricalAxis;
import net.sci.table.NumericColumn;
import net.sci.table.Table;

/**
 * An Imago Frame that displays a Table.
 * 
 * @author dlegland
 *
 */
public class TableFrame extends ImagoFrame
{
    // ===================================================================
    // Static methods
    
    /**
     * Creates a new frame for displaying a table, located with respect to the
     * specified frame.
     * 
     * Creates a new handle from a table, adds it to the application, and
     * returns a new frame associated to this document.
     * 
     * @param table
     *            the image to display
     * @param parentFrame
     *            (optional) an existing frame used to locate the new frame. If
     *            null, a new ImagoGui is created.
     * @return a new frame displaying the input table
     */
    public static final TableFrame create(Table table, ImagoFrame parentFrame)
    {
        // retrieve gui, or create one if necessary
        ImagoGui gui = parentFrame != null ? parentFrame.getGui() : new ImagoGui(new ImagoApp());
        ImagoApp app = gui.getAppli();
        
        // create a handle for the image
        TableHandle parentHandle = null;
        if (parentFrame != null && parentFrame instanceof TableFrame)
        {
            parentHandle = ((TableFrame) parentFrame).handle;
        }
        TableHandle handle = TableHandle.create(app, table, parentHandle);

        // Create the frame
        TableFrame frame = new TableFrame(gui, handle);
        gui.updateFrameLocation(frame, parentFrame);

        // link the frames
        gui.addFrame(frame);
        if (parentFrame != null)
        {
            parentFrame.addChild(frame);
        }
        
        frame.setVisible(true);
        return frame;
    }
    
    
    public static final Collection<TableFrame> getTableFrames(ImagoGui gui)
    {
        ArrayList<TableFrame> res = new ArrayList<TableFrame>();
        for (ImagoFrame frame : gui.getFrames())
        {
            if (frame instanceof TableFrame)
            {
                res.add((TableFrame) frame);
            }
        }
        
        return res;
    }
    
    /**
     * Returns the TableFrame in the specified GUI instance that contains the
     * table with the specified name.
     * 
     * @param gui
     *            the GUI to explore.
     * @param name
     *            the name of the table within the frame
     * @return the frame containing the table, or null if no such Table exists.
     */
    public static final TableFrame getTableFrame(ImagoGui gui, String name)
    {
        for (TableFrame frame : getTableFrames(gui))
        {
            if (name.equals(frame.getTable().getName()))
            {
                return frame;
            }
        }
        
        return null;
    }
    
    
    
    // ===================================================================
    // Class variables

    /** The handle to the table displayed in this frame.*/
    TableHandle handle;
    
    /**
     * The table.
     */
    Table table;

    
    // ===================================================================
    // Constructor
    
    /**
     * @param parent 
     */
    public TableFrame(ImagoGui gui, TableHandle handle)
    {
        super(gui, "Table Frame");
        this.handle = handle;
        this.table = handle.getTable();

        // create menu
        GuiBuilder builder = new GuiBuilder(this);
        builder.createMenuBar();
        
        // layout the frame
        setupLayout();
         
        this.setTitle("Table Example");
        this.jFrame.pack();
        this.setVisible(true);
 
        
        jFrame.doLayout();
        updateTitle();
        
        // setup window listener
        this.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.jFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                gui.removeFrame(TableFrame.this);
                TableFrame.this.close();
            }           
        });
        
        putFrameMiddleScreen();
    }
    
    private void setupLayout() 
    {
        // put into global layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.GREEN);
        
        JTable jtable = createJTable();
        jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        //add the table to the frame
        JScrollPane scrollPane = new JScrollPane(jtable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // decorate the scroll panel with row index
        // (if row names are specified, they are included as an additional column)
        JTable rowTable = new RowNumberTable(jtable);
        scrollPane.setRowHeaderView(rowTable);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
        
        this.jFrame.setContentPane(mainPanel);
    }
    
    private JTable createJTable()
    {
        // table size
        int nRows = table.rowCount();
        int nCols = table.columnCount();
        
        // retrieve and check row axis
        Axis rowAxis = table.getRowAxis();
        boolean hasValidRowAxis = false;
        if (rowAxis != null)
        {
            if (((CategoricalAxis) rowAxis).itemNames() != null)
            {
                hasValidRowAxis = true;
            }
        }

        // If row names are specified, add another column as first column
        int columnOffset = hasValidRowAxis ? 1 : 0;
        
        // Ensure the table has valid column names
        String[] colNames = new String[nCols + columnOffset];
        if (hasValidRowAxis)
        {
            colNames[0] = "Row Names";
            if (rowAxis.getName() != null && !rowAxis.getName().isBlank())
            {
                colNames[0] = rowAxis.getName();
            }
        }
        for (int iCol = 0; iCol < table.columnCount(); iCol++)
        {
            String colName = table.getColumnName(iCol);
            
            // in case of numeric column, tries to append the unit name, 
            // using html formatting to display it on another line
            if (table.column(iCol) instanceof NumericColumn numCol)
            {
                String unitName = numCol.getUnitName();
                if (unitName != null && !unitName.isBlank())
                {
                    colName = String.format("<html><center>%s<br>(%s)</center></html>", colName, numCol.getUnitName());
                }
            }
            colNames[iCol + columnOffset] = colName;
        }
        
        // Convert numeric values to table of objects
        Object[][] data = new Object[nRows][nCols + columnOffset];
        for (int iRow = 0; iRow < nRows; iRow++)
        {
            Object[] row = data[iRow];
            
            if (hasValidRowAxis)
            {
                row[0] = ((CategoricalAxis) rowAxis).itemName(iRow);
            }
            
            for (int iCol = 0; iCol < nCols; iCol++)
            {
                row[iCol + columnOffset] = table.get(iRow, iCol);
            }
            data[iRow] = row;
        };
        
        // create JTable object
        JTable jtable = new JTable(data, colNames);
        
        // some setup
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) jtable.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        jtable.getTableHeader().setPreferredSize(new Dimension(jtable.getColumnModel().getTotalColumnWidth(), 32));
        
        return jtable;
    }
    
    private void putFrameMiddleScreen()
    {
        // set up frame size depending on screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(800, screenSize.width - 100);
        int height = Math.min(700, screenSize.width - 100);
        Dimension frameSize = new Dimension(width, height);
        this.jFrame.setSize(frameSize);

        // set up frame position depending on frame size
        int posX = (screenSize.width - width) / 4;
        int posY = (screenSize.height - height) / 4;
        this.jFrame.setLocation(posX, posY);
    }

    // ===================================================================
    // General methods
    
    public void updateTitle()
    {
        // table name
        String name = this.handle.getName();
        if (name == null || name.isEmpty()) 
        {
            name = "No Name";
        }
        
        String dimString = "(unknown size)";
        int dim[] = this.table.size();
        dimString = dim[0] + "x" + dim[1];
        
        String titleString = name + " - " + dimString;
        this.setTitle(titleString);
    }
    
    public TableHandle getTableHandle()
    {
        return this.handle;
    }
    
    public Table getTable()
    {
        return this.table;
    }
    
    // ===================================================================
    // Display management methods

}
