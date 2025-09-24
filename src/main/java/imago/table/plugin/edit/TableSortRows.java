/**
 * 
 */
package imago.table.plugin.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import imago.table.plugin.TableFramePlugin;
import net.sci.axis.Axis;
import net.sci.table.Column;
import net.sci.table.Table;

/**
 * Sort rows of a table based on the values within one of the columns.
 * 
 * @author dlegland
 *
 */
public class TableSortRows implements TableFramePlugin
{
    /**
     * Default empty constructor.
     */
    public TableSortRows()
    {
    }
    
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Get the data table
        if (!(frame instanceof TableFrame))
        {
            return;
        }
        Table table = ((TableFrame) frame).getTable();

        // get general info from table
        String[] colNames = table.getColumnNames();

        // Display dialog for choosing options
        GenericDialog dlg = new GenericDialog(frame, "Sort Table Rows");
        dlg.addChoice("Column", colNames, colNames[0]);
        dlg.addChoice("Operation", SortOrder.getAllLabels(), SortOrder.INCREASING.toString());
        
        int colIndex = 0;
        SortOrder order = null;
        
        dlg.showDialog();
        // wait for user input
        if (dlg.wasCanceled()) 
        {
            return;
        }
        
        // Parse dialog contents
        colIndex = table.findColumnIndex(dlg.getNextChoice());
        order = SortOrder.fromLabel(dlg.getNextChoice());
        
        // identify index of rows to keep
        int[] rowIndices = findRowIndices(table.column(colIndex), order);
        
        // create a new table from filtered columns
        Column[] newCols = table.columns().stream()
                .map(col -> col.selectRows(rowIndices))
                .toArray(Column[]::new);
        Table res = Table.create(newCols);
        
        // Default name for table
        String tableName = table.getName();
        if (tableName == null || tableName.length() == 0)
        {
            tableName = "data";
        }
        res.setName(tableName + "-rowSel");

        Axis rowAxis = table.getRowAxis();
        if (rowAxis != null)
        {
            res.setRowAxis(rowAxis.selectElements(rowIndices));
        }
        
        // add the new frame to the GUI
        TableFrame.create(res, frame);
    }
    
    private int[] findRowIndices(Column column, SortOrder order)
    {
        List<Integer> indexList = new ArrayList<>(column.length());
        for (int i = 0; i < column.length(); i++)
        {
            indexList.add(i);
        }
        
        Comparator<Integer> comparator;
        if (order == SortOrder.INCREASING)
        {
            comparator = new Comparator<Integer>()
            {
                @Override
                public int compare(Integer index1, Integer index2)
                {
                    return Double.compare(column.getValue(index1), column.getValue(index2));
                }
            };
        }
        else
        {
            comparator = new Comparator<Integer>()
            {
                @Override
                public int compare(Integer index1, Integer index2)
                {
                    return Double.compare(column.getValue(index2), column.getValue(index1));
                }
            };
            
        }
        
        Collections.sort(indexList, comparator);
        
        // return as an array of int
        return indexList.stream().mapToInt(i->i).toArray();
    }
    
    public enum SortOrder
    {
        INCREASING("Increasing"),
        DECREASING("Decreasing");
        
        private String label;
        
        private SortOrder(String label)
        {
            this.label = label;
        }
        
        /**
         * Converts this relational operator into a string.
         * 
         * @return a String representation of this RelationalOperator
         */
        public String toString() 
        {
            return this.label;
        }
        
        /**
         * Returns the list of labels for this enumeration.
         * 
         * @return the list of labels for this enumeration.
         */
        public static String[] getAllLabels()
        {
            return Stream.of(SortOrder.values())
                    .map(op -> op.label)
                    .toArray(String[]::new);
        }
        
        /**
         * Determines the RelationalOperator enumeration item from its label.
         * 
         * @param label
         *            the label of the RelationalOperator
         * @return the parsed RelationalOperator
         * @throws IllegalArgumentException
         *             if label is not recognized.
         */
        public static SortOrder fromLabel(String label)
        {
            for (SortOrder item : SortOrder.values()) 
            {
                if (item.label.equalsIgnoreCase(label)) return item;
            }
            throw new IllegalArgumentException("Unable to parse RelationalOperator with label: " + label);
        }
    }
}
