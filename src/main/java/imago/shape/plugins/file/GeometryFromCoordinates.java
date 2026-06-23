/**
 * 
 */
package imago.shape.plugins.file;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import imago.table.TableHandle;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.MultiPoint2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.LineString2D;
import net.sci.geom.polygon2d.LinearRing2D;
import net.sci.geom.polygon2d.Polygon2D;
import net.sci.table.Column;
import net.sci.table.NumericColumn;
import net.sci.table.Table;

/**
 * 
 */
public class GeometryFromCoordinates implements ShapeManagerPlugin
{

    String[] geomTypes = new String[] {"MultiPoint", "Polyline (Open)", "Polyline (Closed)", "Polygon"};
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        // collect the names of frames containing tables
        ImagoApp app = frame.getGui().getAppli();
        String[] tableNames = TableHandle.getAllNames(app).toArray(new String[]{});

        // Create Dialog for choosing table
        GenericDialog dlg1 = new GenericDialog(frame, "Import Coordinates");
        dlg1.addChoice("Table:", tableNames, tableNames[0]);
        // Display dialog and wait for OK or Cancel
        dlg1.showDialog();
        if (dlg1.wasCanceled())
        {
            return;
        }
       
        String tableName = dlg1.getNextChoice();
        Table table = TableHandle.findFromName(app, tableName).getTable();
        
        // Create another Dialog for choosing coordinates column
        String[] colNames = table.getColumnNames();
        
        GenericDialog dlg2 = new GenericDialog(frame, "Import Coordinates");
        dlg2.addChoice("X Coords:", colNames, colNames[0]);
        dlg2.addChoice("Y Coords:", colNames, colNames[0]);
        dlg2.addChoice("Geometry:", geomTypes, geomTypes[0]);
        
        // Display dialog and wait for OK or Cancel
        dlg2.showDialog();
        if (dlg2.wasCanceled())
        {
            return;
        }
        
        Column col1 = table.column(dlg2.getNextChoiceIndex());
        Column col2 = table.column(dlg2.getNextChoiceIndex());
        if (!(col1 instanceof NumericColumn) || !(col2 instanceof NumericColumn))
        {
            ImagoGui.showErrorDialog(frame, "Table columns must contain numeric values", "Import Error");
            return;
        }
        double[] xCoords = ((NumericColumn) col1).getValues();
        double[] yCoords = ((NumericColumn) col2).getValues();
        
        Point2D[] points = new Point2D[xCoords.length];
        for (int i = 0; i < xCoords.length; i++)
        {
            points[i] = Point2D.of(xCoords[i], yCoords[i]);
        }
        
        int geomIndex = dlg2.getNextChoiceIndex();
        Geometry2D geom = switch (geomIndex)
        {
            case 0 -> {
                MultiPoint2D mp = MultiPoint2D.create(points.length);
                for (Point2D p : points)
                {
                    mp.addPoint(p);
                }
                yield mp;
            }
            case 1 -> LineString2D.create(points);
            case 2 -> LinearRing2D.create(points);
            case 3 -> Polygon2D.create(points);
            
            default -> throw new IllegalArgumentException("Unexpected value: " + geomIndex);
        };
        
        GeometryHandle handle = GeometryHandle.create(app, geom);
        handle.setName(table.getName());
        
        ((ShapeManager) frame).updateInfoTable();
    }

}
