/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.EnumSet;

import javax.swing.JFileChooser;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Image;
import net.sci.image.io.RawImageReader;
import net.sci.image.io.RawImageReader.DataType;

/**
 * @author dlegland
 *
 */
public class ImportImageRawData implements Plugin
{
    private JFileChooser openWindow = null;


    public ImportImageRawData()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        System.out.println("import raw data");

        // create file dialog if it doesn't exist
        if (openWindow == null)
        {
            openWindow = new JFileChooser(".");
//            openWindow.setFileFilter(new FileNameExtensionFilter("MetaImage files (*.mhd, *.mha)", "mhd", "mha"));
        }

        // Open dialog to choose the file
        int ret = openWindow.showOpenDialog(frame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        // Check the chosen file is value
        File file = openWindow.getSelectedFile();
        if (!file.isFile())
        {
            return;
        }

        GenericDialog gd = new GenericDialog(frame, "Import Raw Data");
        gd.addNumericField("Size X ", 200, 0);
        gd.addNumericField("Size Y ", 200, 0);
        gd.addNumericField("Size Z ", 1, 0);
        gd.addChoice("Data Type ", EnumSet.allOf(DataType.class), DataType.UINT8);
        gd.addChoice("Byte Order ", new String[]{"Little Endian", "Big Endian"}, "Little Endian");
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        int sizeX = (int) gd.getNextNumber();
        int sizeY = (int) gd.getNextNumber();
        int sizeZ = (int) gd.getNextNumber();
        DataType type = DataType.fromLabel(gd.getNextChoice());
        ByteOrder byteOrder = gd.getNextChoiceIndex() == 0 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

        int[] size = sizeZ == 1 ? new int[]{sizeX, sizeY} : new int[]{sizeX, sizeY, sizeZ}; 
        
        RawImageReader reader; 
        try
        {
            reader = new RawImageReader(file, size, type, byteOrder);
        } 
        catch (IOException ex)
        {
            System.err.println(ex);
            return;
        }
        
        
        // apply operator on current image
        Image image;
        try
        {
            image = reader.readImage();
        }
        catch (IOException ex)
        {
            System.err.println(ex);
            return;
        }
        
        // add the image document to GUI
        frame.getGui().addNewDocument(image);
    }
    
}