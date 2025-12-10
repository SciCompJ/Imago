/**
 * 
 */
package imago.image.plugins.edit;

import javax.swing.JComboBox;

import imago.app.shape.MarkerType;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.table.TableFrame;
import net.sci.geom.geom2d.Point2D;
import net.sci.table.Column;
import net.sci.table.NumericColumn;
import net.sci.table.Table;

/**
 * Opens a dialog to choose a table and two columns within the table, and adds a
 * new annotation corresponding to points with coordinates from the table.
 */
public class AddPointShapeFromTable implements FramePlugin
{
    /**
     * Default empty constructor
     */
    public AddPointShapeFromTable()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
        
        // retrieve list of tables
        ImagoGui gui = frame.getGui();
        String[] tableNames = TableFrame.getTableFrames(gui).stream()
                .map(frm -> frm.getTable())
                .map(tbl -> tbl.getName())
                .toArray(String[]::new);
        if (tableNames.length == 0) 
        {
            System.out.println("no table open");
            return;
        }
        
        Table table = TableFrame.getTableFrame(gui, tableNames[0]).getTable();
        String[] colNames = table.getColumnNames();
        
        // setup a dialog to choose table and two columns
        GenericDialog gd = new GenericDialog(frame, "Add Point Shape");
        JComboBox<String> tableCombo = gd.addChoice("Table", tableNames, tableNames[0]);
        JComboBox<String> xPosCombo = gd.addChoice("X-Position", colNames, colNames[0]);
        JComboBox<String> yPosCombo = gd.addChoice("Y-Position", colNames, colNames[0]);
        gd.addEnumChoice("Marker Type", MarkerType.class, MarkerType.PLUS);
        gd.addNumericField("Marker Size", 6, 0);
        
        // updates the combo box containing column names when table changes 
        tableCombo.addActionListener(evt -> {
            String name = tableNames[tableCombo.getSelectedIndex()];
            String[] colNames2 = TableFrame.getTableFrame(gui, name).getTable().getColumnNames();
            xPosCombo.removeAllItems();
            yPosCombo.removeAllItems();
            for (String colName : colNames2)
            {
                xPosCombo.addItem(colName);
                yPosCombo.addItem(colName);
            }
        });
        
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // retrieve user choices
        String tableName = gd.getNextChoice();
        String xColName = gd.getNextChoice();
        String yColName = gd.getNextChoice();
        MarkerType markerType = (MarkerType) gd.getNextEnumChoice();
        int markerSize = (int) gd.getNextNumber();
        
        // retrieve relevant columns from table
        table = TableFrame.getTableFrame(gui, tableName).getTable();
        Column xColumn = table.column(table.findColumnIndex(xColName));
        Column yColumn = table.column(table.findColumnIndex(yColName));
        if (!(xColumn instanceof NumericColumn)) throw new RuntimeException("Requires numeric column for x-coordinates");
        if (!(yColumn instanceof NumericColumn)) throw new RuntimeException("Requires numeric column for y-coordinates");
        
        // iterate over pairs of coordinates to create point annotations
        for (int i = 0; i < xColumn.length(); i++)
        {
            Point2D point = new Point2D(xColumn.getValue(i), yColumn.getValue(i));
            Shape shape = new Shape(point);
            shape.getStyle()
                .setMarkerType(markerType)
                .setMarkerSize(markerSize)
                .setLineColor(java.awt.Color.BLUE)
                ;
            handle.addShape(shape);
        }
        handle.notifyImageHandleChange();
    }
    
}
