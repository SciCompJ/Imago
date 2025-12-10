/**
 * 
 */
package imago.image.plugins.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComboBox;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.image.plugins.ImageFramePlugin;
import imago.table.TableFrame;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.Array3D;
import net.sci.array.numeric.UInt8Array2D;
import net.sci.image.Image;
import net.sci.table.Column;
import net.sci.table.NumericColumn;
import net.sci.table.Table;

/**
 * Crops a series of Thumbnail images from an image and a list of positions
 * corresponding to thumbnail centers. Concatenates the results into a new 3D
 * image.
 */
public class ImageCropThumbnailList implements ImageFramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageCropThumbnailList()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        ImagoGui gui = frame.getGui();
        
        ImageViewer viewer = iframe.getImageViewer();
        Image image = viewer.getImage();
        Array<?> array = image.getData();
        if (array.dimensionality() != 2)
        {
            throw new RuntimeException("Requires a 2D image");
        }
        
        // default image size
        int sizeX_init = 200;
        int sizeY_init = 200;

        // retrieve list of tables
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
        GenericDialog gd = new GenericDialog(frame, "Crop Thumbnails");
        JComboBox<String> tableCombo = gd.addChoice("Table", tableNames, tableNames[0]);
        JComboBox<String> xPosCombo = gd.addChoice("X-Position", colNames, colNames[0]);
        JComboBox<String> yPosCombo = gd.addChoice("Y-Position", colNames, colNames[0]);
        gd.addNumericField("Size X: ", sizeX_init, 0);
        gd.addNumericField("Size Y: ", sizeY_init, 0);

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
        
        // show dialog and wait for user validation
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // retrieve user choices
        String tableName = gd.getNextChoice();
        String xColName = gd.getNextChoice();
        String yColName = gd.getNextChoice();
        int sizeX = (int) gd.getNextNumber();
        int sizeY = (int) gd.getNextNumber();
        
        table = TableFrame.getTableFrame(gui, tableName).getTable();
        Column xColumn = table.column(table.findColumnIndex(xColName));
        Column yColumn = table.column(table.findColumnIndex(yColName));
        if (!(xColumn instanceof NumericColumn)) throw new RuntimeException("Requires numeric column for x-coordinates");
        if (!(yColumn instanceof NumericColumn)) throw new RuntimeException("Requires numeric column for y-coordinates");

        int n = xColumn.length();
        
        int[][] cropPositions = new int[n][2];
        for (int i = 0; i < n; i++)
        {
            cropPositions[i][0] = (int) Math.round(xColumn.getValue(i));
            cropPositions[i][1] = (int) Math.round(yColumn.getValue(i));
        }
        
        // compute 3D array
        Array<?> res = cropThumbnails(array, new int[] {sizeX, sizeY}, cropPositions);
        
        // create  result image
        Image resImage = new Image(res, image);
        resImage.setName(image.getName() + "-thumbs");

        // add the image document to GUI
        ImageFrame.create(resImage, frame);
    }
    
    private static final <T> Array<T> cropThumbnails(Array<T> array, int[] size, int[][] cropCenters)
    {
        int n = cropCenters.length;
        ArrayList<Array<T>> arrays = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
        {
            arrays.add(crop(array, size, cropCenters[i]));
        }
        return createStack(arrays);
    }
    
    private static final <T> Array<T> crop(Array<T> array, int[] size, int[] cropCenter)
    {
        int centerX = cropCenter[0];
        int centerY = cropCenter[1];
        int sizeX = size[0];
        int sizeY = size[1];
        
        Array<T> res = array.newInstance(size);
        
        int[] pos0 = new int[2];
        int[] pos2 = new int[2];
        for (int y = Math.max(centerY - sizeY/2, 0); y < Math.min(centerY + (sizeY+1)/2, array.size(1)); y++)
        {
            pos0[1] = y;
            pos2[1] = y - centerY + sizeY/2;
            for (int x = Math.max(centerX - sizeX/2, 0); x < Math.min(centerX + (sizeX+1)/2, array.size(0)); x++)
            {
                pos0[0] = x;
                pos2[0] = x - centerX + sizeX/2;
                res.set(pos2, array.get(pos0));
            }
        }
        
        return res;
    }
    
    private static final <T> Array<T> createStack(Collection<Array<T>> arrays)
    {
        int n = arrays.size();
        Array<T> refArray = arrays.iterator().next();
        Array3D<T> res = Array3D.wrap(refArray.newInstance(new int[] {refArray.size(0), refArray.size(1), n}));
        
        Iterator<Array<T>> iter = arrays.iterator();
        int i = 0;
        while (iter.hasNext())
        {
            res.setSlice(i++, Array2D.wrap(iter.next()));
        }
        return res;
    }
    
    public static final void main(String... args)
    {
        UInt8Array2D array = UInt8Array2D.create(500, 500);
        int[][] positions = new int[][] {{100, 100}, {120, 80}, {300, 200}, {250, 350}};
        int[] size = new int[] {100, 100};
        
        Array<?> res = cropThumbnails(array, size, positions);
        
        new Image(res).show();
    }
}
