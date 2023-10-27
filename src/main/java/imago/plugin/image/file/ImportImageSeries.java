/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.FileFilter;

import net.sci.array.Array;
import net.sci.image.Image;
import imago.Imago;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;

/**
 * Imports a series of images and stores them into a 3D image.
 * 
 * @author dlegland
 *
 */
public class ImportImageSeries implements FramePlugin
{
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // opens a dialog to choose the file
        File file = frame.getGui().chooseFileToOpen(frame, "Open Image Series", ImageFileFilters.TIFF);
        
        // Check the chosen file is valid
        if (file == null || !file.isFile())
        {
            return;
        }

        // create a dialog to choose files to read
        GenericDialog gd = new GenericDialog(frame, "Import Series");
        gd.addNumericField("First Image", 1, 0);
        gd.addNumericField("Increment", 1, 0);
        gd.addTextField("Contains", "");

        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        
        int firstImageIndex = (int) gd.getNextNumber() - 1;
        int imageIndexIncr = (int) gd.getNextNumber();
        String stringToContains = gd.getNextString();
        
        String ext = findExtension(file.getName());
        
        String pattern;
        FileFilter fileFilter;
        if (stringToContains.isEmpty())
        {
            pattern = "*." + ext;
            fileFilter = new FileFilter(){
                @Override
                public boolean accept(File file)
                {
                    return file.getName().endsWith("." + ext);
                }
            };
        }
        else
        {
            pattern = "*" + stringToContains + "*." + ext;
            fileFilter = new FileFilter(){
                @Override
                public boolean accept(File file)
                {
                    String name = file.getName();
                    if (!name.endsWith("." + ext)) return false;
                    if (!name.contains(stringToContains)) return false;
                    return true;
                }
            };
        }
        System.out.println("Import files with pattern: " + pattern);
        
        File[] fileList = file.getParentFile().listFiles(fileFilter);
        
        Image firstImage = Imago.readImage(fileList[firstImageIndex], frame);

        // count the number of images to read
        int nImages2 = 0;
        for (int i = firstImageIndex; i < fileList.length; i += imageIndexIncr)
        {
            nImages2++;
        }
        
        // Allocate array for image
        @SuppressWarnings("unchecked")
        Array<Object> array0 = (Array<Object>) firstImage.getData();
        int sizeX = array0.size(0);
        int sizeY = array0.size(1);
        int[] dims = new int[]{sizeX, sizeY, nImages2};
        Array<Object> array = array0.newInstance(dims);
        
        // read each image to populate the array
        int[] pos = new int[3];
        pos[2] = 0;
        for (int i = firstImageIndex; i < fileList.length; i += imageIndexIncr)
        {
            Image image = Imago.readImage(fileList[i], frame);
            Array<?> sliceArray = image.getData();
            
            int[] slicePos = new int[2];
            for (int y = 0; y < sizeY; y++)
            {
                slicePos[1] = pos[1] = y;
                for (int x = 0; x < sizeX; x++)
                {
                    slicePos[0] = pos[0] = x;
                    array.set(pos, sliceArray.get(slicePos));
                }
            }

            pos[2]++;
        }
        
        Image image = new Image(array, firstImage);
        
        // add the image document to GUI
        ImageFrame.create(image, frame);
    }

    private String findExtension(String fileName)
    {
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index+1);
    }
}
