/**
 * 
 */
package imago.plugin.image.shape;

import java.util.Collection;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import imago.gui.frames.TableFrame;
import net.sci.array.Array;
import net.sci.array.interp.LinearInterpolator2D;
import net.sci.array.interp.LinearInterpolator3D;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.array.scalar.UInt8Array2D;
import net.sci.array.scalar.UInt8Array3D;
import net.sci.geom.geom2d.AffineTransform2D;
import net.sci.geom.geom2d.MatrixAffineTransform2D;
import net.sci.geom.geom3d.AffineTransform3D;
import net.sci.geom.geom3d.DefaultAffineTransform3D;
import net.sci.image.Image;
import net.sci.register.image.TransformedImage2D;
import net.sci.register.image.TransformedImage3D;
import net.sci.table.Table;

/**
 * Choose a table, interpret the specified row as coefficients of a 2D or 3D
 * Affine transform, and apply the transform to the current image.
 * 
 * @author dlegland
 *
 */
public class ImageApplyTransformFromTable implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        System.out.println("transform image");

        // get current image
        ImageFrame iFrame = (ImageFrame) frame;
        Image image = iFrame.getImageHandle().getImage();
        
        
        Array<?> array = image.getData();
        int nDims = array.dimensionality();
        if (nDims != 2 && nDims != 3)
        {
            ImagoGui.showErrorDialog(frame, "Requires an image with 2D or 3D scalar array");
            return;
        }
        if (!(array instanceof ScalarArray2D<?> || array instanceof ScalarArray3D<?>))
        {
            ImagoGui.showErrorDialog(frame, "Requires an image with 2D scalar array");
            return;
        }

        // retrieve the table frames
        Collection<TableFrame> frames = TableFrame.getTableFrames(frame.getGui());
        int nTables = frames.size();
        String[] tableNames = new String[nTables];
        int i = 0;
        for (TableFrame f : frames) 
        {
            tableNames[i++] = f.getTable().getName();
        }
        
        GenericDialog gd = new GenericDialog(frame, "Transform Image");
        gd.addNumericField("Size X: ", array.size(0), 0);
        gd.addNumericField("Size Y: ", array.size(1), 0);
        if (nDims == 3)
        {
            gd.addNumericField("Size Z: ", array.size(2), 0);
        }
        gd.addChoice("Transform Table: ", tableNames, tableNames[0]);
        gd.addNumericField("Transform Row Index: ", 0, 0);
//        gd.addCheckBox("Create View", false);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        int newSizeX = (int) gd.getNextNumber();
        int newSizeY = (int) gd.getNextNumber();
        int newSizeZ = 0;
        if (nDims == 3)
        {
            newSizeZ = (int) gd.getNextNumber();
        }
        String tableName = gd.getNextChoice();
        int rowIndex = (int) gd.getNextNumber();
//        boolean createView = gd.getNextBoolean();
        
        TableFrame tableFrame = TableFrame.getTableFrame(frame.getGui(), tableName);
        if (tableFrame == null)
        {
            System.out.println("table frame is null");
            return;
        }
        
        Table table = tableFrame.getTable();
        if (table.rowCount() < rowIndex)
        {
            ImagoGui.showErrorDialog(frame, "Table must have at least " + (rowIndex + 1) + "rows");
        }
        
        int nRequiredCols = nDims == 2 ? 6 : 12;
        if (table.columnCount() < nRequiredCols)
        {
            ImagoGui.showErrorDialog(frame, "Table must have at least " + nRequiredCols + "columns");
        }

        Image resImage;
        if (nDims == 2)
        {
            // process 2D case
            double m00 = table.getValue(rowIndex, 0);
            double m01 = table.getValue(rowIndex, 1);
            double m02 = table.getValue(rowIndex, 2);
            double m10 = table.getValue(rowIndex, 3);
            double m11 = table.getValue(rowIndex, 4);
            double m12 = table.getValue(rowIndex, 5);
            AffineTransform2D transfo = new MatrixAffineTransform2D(m00, m01, m02, m10, m11, m12);
            LinearInterpolator2D interp = new LinearInterpolator2D((ScalarArray2D<?>) array);

            TransformedImage2D tim = new TransformedImage2D(interp, transfo);

            UInt8Array2D res = UInt8Array2D.create(newSizeX, newSizeY);
            res.fillValues((x,y) -> tim.evaluate(x, y));

            resImage = new Image(res, image);
        }
        else
        {
            // process 3D case
            double m00 = table.getValue(rowIndex, 0);
            double m01 = table.getValue(rowIndex, 1);
            double m02 = table.getValue(rowIndex, 2);
            double m03 = table.getValue(rowIndex, 3);
            double m10 = table.getValue(rowIndex, 4);
            double m11 = table.getValue(rowIndex, 5);
            double m12 = table.getValue(rowIndex, 6);
            double m13 = table.getValue(rowIndex, 7);
            double m20 = table.getValue(rowIndex, 8);
            double m21 = table.getValue(rowIndex, 9);
            double m22 = table.getValue(rowIndex, 10);
            double m23 = table.getValue(rowIndex, 11);
            AffineTransform3D transfo = new DefaultAffineTransform3D(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23);
            LinearInterpolator3D interp = new LinearInterpolator3D((ScalarArray3D<?>) array);

            TransformedImage3D tim = new TransformedImage3D(interp, transfo);

            UInt8Array3D res = UInt8Array3D.create(newSizeX, newSizeY, newSizeZ);
            res.fillValues((x,y,z) -> tim.evaluate(x, y, z));

            resImage = new Image(res, image);
        }

        iFrame.createImageFrame(resImage);
    }

    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        int nDims = image.getData().dimensionality();
        if (nDims != 2 && nDims != 3) return false;
        
        if(TableFrame.getTableFrames(frame.getGui()).isEmpty()) return false;
        
        return true;
    }
}
