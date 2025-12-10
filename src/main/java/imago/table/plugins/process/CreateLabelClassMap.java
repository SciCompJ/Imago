/**
 * 
 */
package imago.table.plugins.process;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.table.TableFrame;
import net.sci.array.Array;
import net.sci.array.numeric.Int32Array2D;
import net.sci.array.numeric.IntArray;
import net.sci.array.numeric.IntArray2D;
import net.sci.image.Image;
import net.sci.image.ImageType;
import net.sci.table.CategoricalColumn;
import net.sci.table.Column;
import net.sci.table.IntegerColumn;
import net.sci.table.Table;

/**
 * Generate a color image from 1) a label map and 2) a data table with row names
 * corresponding to labels, and a categorical column corresponding to the class
 * to display.
 */
public class CreateLabelClassMap implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public CreateLabelClassMap()
    {
    }


    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        if (!(frame instanceof TableFrame)) return;
        Table table = ((TableFrame) frame).getTable();
        
        // retrieve list of images
        ImagoGui gui = frame.getGui();
        ImagoApp app = gui.getAppli();
        Collection<String> imageNames = ImageHandle.getAllNames(app);

        // if no image is open, simply return
        if (imageNames.size() == 0)
        {
            return;
        }
        
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = imageNameArray[0];
        
        String[] colNames = table.getColumnNames();
        
        GenericDialog gd = new GenericDialog(frame, "Create Label Class Map");
        gd.addChoice("Label Map Image:", imageNameArray, firstImageName);
        gd.addChoice("Class Column:", colNames, colNames[0]);
        
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        
        // set up current parameters
        Image refImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        int classColumnIndex = gd.getNextChoiceIndex();
        Column classColumn = table.column(classColumnIndex);
        if (!(classColumn instanceof CategoricalColumn))
        {
            if (classColumn instanceof IntegerColumn)
            {
                classColumn = CategoricalColumn.convert(classColumn);
            }
            else
            {
                ImagoGui.showErrorDialog(frame, "Requires a Categorical Column", "Column Error");
                return;
            }
        }
        
        // retrieve row names
        String[] rowNames = table.getRowNames();
        if (rowNames == null)
        {
            ImagoGui.showErrorDialog(frame, "Requires a table with row names", "Column Error");
        }
        
        // parse labels from row names
        Map<Integer,Integer> labelToRow = new HashMap<>();
        for (int r = 0; r < table.rowCount(); r++)
        {
            int label = Integer.parseInt(rowNames[r]);
            labelToRow.put(label, r);
        }
        
        Array<?> array = refImage.getData();
        if (!(array instanceof IntArray))
        {
            ImagoGui.showErrorDialog(frame, "Label Map imageust contain integer values", "Column Error");
            return;
        }
        IntArray2D<?> labelMap = IntArray2D.wrap((IntArray<?>) array);
        int sizeX = labelMap.size(0);
        int sizeY = labelMap.size(1);
        Int32Array2D res = Int32Array2D.create(sizeX, sizeY);
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                int label = labelMap.getInt(x, y);
                if (label == 0) continue;
                if (labelToRow.containsKey(label))
                {
                    int row = labelToRow.get(label);
                    int value = ((CategoricalColumn) classColumn).getLevelIndex(row);
                    res.setInt(x, y, value + 1);
                }
            }
        }
        
        Image resImage = new Image(res, ImageType.LABEL, refImage);
        resImage.setName(refImage.getName() + "-classes");
        
        // update display settings
        CategoricalColumn col2 = ((CategoricalColumn) classColumn);
        int nClasses = col2.levelNames().length;
        resImage.getDisplaySettings().setDisplayRange(new double[] {0, nClasses});
        
        // TODO: add color management
//        Color[] groupColors = col2.levelColors();
        
//        // create default label Color Model
//        ColorModel cm = createColorModel(groupColors, Color.WHITE);
//        resPlus.getProcessor().setColorModel(cm);
        
        // add the image document to GUI
        ImageFrame.create(resImage, frame);
    }

}
