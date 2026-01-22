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
    enum Marker
    {
        CROSS("Cross", MarkerType.CROSS),
        PLUS("Plus", MarkerType.PLUS),
        ASTERISK("Asterisk", MarkerType.ASTERISK),
        SQUARE("Square", MarkerType.SQUARE),
        DIAMOND("Diamond", MarkerType.DIAMOND),
        TRIANGLE_UP("Triangle (Up)", MarkerType.TRIANGLE_UP),
        TRIANGLE_DOWN("Triangle (Down)", MarkerType.TRIANGLE_DOWN),
        TRIANGLE_LEFT("Triangle (Left)", MarkerType.TRIANGLE_LEFT),
        TRIANGLE_RIGHT("Triangle (Right)", MarkerType.TRIANGLE_RIGHT);
        
        String label;
        MarkerType markerType;
        
        private Marker(String label, MarkerType markerType)
        {
            this.label = label;
            this.markerType = markerType;
        }
    }
    
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
        gd.addEnumChoice("Marker Type", Marker.class, Marker.PLUS);
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
        MarkerType markerType = ((Marker) gd.getNextEnumChoice()).markerType;
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
