package imago.table;

import java.awt.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * Use a JTable as a renderer for row names of a given main table. This table
 * must be added to the row header of the scrollpane that contains the main
 * table.
 * 
 * {@snipper lang="java" :
 *  JTable mainTable = new JTable(...);
 *  JScrollPane scrollPane = new JScrollPane(mainTable);
 *  JTable rowTable = new RowNameTable(mainTable);
 *  scrollPane.setRowHeaderView(rowTable);
 *  scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
 *      rowTable.getTableHeader());
 *  }
 * 
 * Ref: http://tips4java.wordpress.com/2008/11/18/row-number-table/
 */
public class RowNamesTable extends JTable implements ChangeListener, PropertyChangeListener
{
    /**
     * to comply with Swing
     */
    private static final long serialVersionUID = 1L;
    
    private JTable main;
    private String[] rowNames; 
    
    public RowNamesTable(JTable table, String[] rowNames)
    {
        main = table;
        this.rowNames = rowNames;
        
        main.addPropertyChangeListener(this);
        
        setFocusable(false);
        setAutoCreateColumnsFromModel(false);
        setModel(main.getModel());
        setSelectionModel(main.getSelectionModel());
        
        TableColumn column = new TableColumn();
        column.setHeaderValue(" ");
        addColumn(column);
        column.setCellRenderer(new RowNamesRenderer());
        
        getColumnModel().getColumn(0).setPreferredWidth(150);
        getColumnModel().getColumn(0).setResizable(true);
        
        setPreferredScrollableViewportSize(getPreferredSize());
    }
    
    @Override
    public void addNotify()
    {
        super.addNotify();
        
        Component c = getParent();
        
        // Keep scrolling of the row table in sync with the main table.
        
        if (c instanceof JViewport)
        {
            JViewport viewport = (JViewport) c;
            viewport.addChangeListener(this);
        }
    }
    
    /*
     * Delegate method to main table
     */
    @Override
    public int getRowCount()
    {
        return main.getRowCount();
    }
    
    @Override
    public int getRowHeight(int row)
    {
        return main.getRowHeight(row);
    }
    
    /*
     * This table does not use any data from the main TableModel, so just return
     * a value based on the row parameter.
     */
    @Override
    public Object getValueAt(int row, int column)
    {
        if (rowNames != null && row < rowNames.length)
        {
            return String.format("%d: %s", row + 1, rowNames[row]);
        }
        return String.format("%d", row + 1);
    }
    
    /*
     * Don't edit data in the main TableModel by mistake
     */
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }
    
    //
    // Implement the ChangeListener
    //
    public void stateChanged(ChangeEvent e)
    {
        // Keep the scrolling of the row table in sync with main table
        
        JViewport viewport = (JViewport) e.getSource();
        JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
    }
    
    //
    // Implement the PropertyChangeListener
    //
    public void propertyChange(PropertyChangeEvent e)
    {
        // Keep the row table in sync with the main table
        
        if ("selectionModel".equals(e.getPropertyName()))
        {
            setSelectionModel(main.getSelectionModel());
        }
        
        if ("model".equals(e.getPropertyName()))
        {
            setModel(main.getModel());
        }
    }
    
    /*
     * Borrow the renderer from JDK1.4.2 table header
     */
    private static class RowNamesRenderer extends DefaultTableCellRenderer
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        public RowNamesRenderer()
        {
            setHorizontalAlignment(JLabel.LEFT);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            if (table != null)
            {
                JTableHeader header = table.getTableHeader();
                
                if (header != null)
                {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }
            
            if (isSelected)
            {
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            setText((value == null) ? "" : value.toString());
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            
            return this;
        }
    }
}
