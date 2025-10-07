/**
 * 
 */
package imago.image.plugin.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.EnumSet;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import net.sci.image.Image;
import net.sci.image.io.RawImageReader;
import net.sci.image.io.RawImageReader.DataType;

/**
 * Opens a dialog to read image raw data.
 * 
 * @author dlegland
 *
 */
public class ImportImageRawData implements FramePlugin
{
    
    /**
     * Default empty constructor.
     */
    public ImportImageRawData()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // opens a dialog to choose the file
        File file = frame.getGui().chooseFileToOpen(frame, "Open Image");
        if (file == null)
        {
            return;
        }
        
        // Check the chosen file is valid
        if (!file.isFile())
        {
            ImagoGui.showErrorDialog(frame,
                    "Could not find the selected file: " + file.getName(),
                    "Image I/O Error");
            return;
        }
        
        // Opens a dialog to select image size and type
        GenericDialog gd = new GenericDialog(frame, "Import Raw Data");
        gd.addNumericField("Size X ", 200, 0);
        gd.addNumericField("Size Y ", 200, 0);
        gd.addNumericField("Size Z ", 1, 0);
        gd.addChoice("Data Type ", EnumSet.allOf(DataType.class), DataType.UINT8);
        gd.addChoice("Byte Order ", new String[]{"Little Endian", "Big Endian"}, "Little Endian");
        gd.addCheckBox("Virtual Image", false);
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
        boolean virtualImage = gd.getNextBoolean();
        
        // dimensions of image data array
        int[] dims = sizeZ == 1 ? new int[]{sizeX, sizeY} : new int[]{sizeX, sizeY, sizeZ};
        
        // check validity of virtual image option
        if (virtualImage && dims.length != 3)
        {
            System.out.println("virtual images are available only for 3D image data");
            virtualImage = false;
        }
            
        if (virtualImage)
        {
            if (type != DataType.UINT8 && type != DataType.UINT16 && type != DataType.FLOAT32)
            {
                ImagoGui.showErrorDialog(frame,
                        "Can not create virtual image for type: " + type.name(),
                        "Image Import Error");
                return;
            }
        }

        System.out.println("import raw data from file: " + file.getName());
        RawImageReader reader;
        try
        {
            // try to read image
            reader = new RawImageReader(file, dims, type, byteOrder);
            Image image = virtualImage ? reader.readVirtualImage3D() : reader.readImage();
            
            // setup image metadata
            String fileName = file.getName();
            image.setNameFromFileName(fileName);

            // add the image document to GUI
            ImageFrame.create(image, frame);
        } 
        catch (IOException ex)
        {
            System.err.println(ex);
            return;
        }
    }
}
