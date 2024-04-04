/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.FileFilter;

import net.sci.array.Array;
import net.sci.array.scalar.UInt16Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;
import net.sci.image.io.*;
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
    @SuppressWarnings("unchecked")
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

        String ext = findExtension(file.getName());
        int nFiles = file.getParentFile().listFiles(pathName -> pathName.getName().endsWith("." + ext)).length;
        
        // create a dialog to choose files to read
        GenericDialog gd = new GenericDialog(frame, "Import Series");
        gd.addNumericField("First Image", 1, 0);
        gd.addNumericField("Last Image", nFiles, 0);
        gd.addNumericField("Increment", 1, 0);
        gd.addTextField("File Name Pattern", "*.*");
        gd.addCheckBox("Virtual", false);

        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        
        int firstImageIndex = (int) gd.getNextNumber() - 1;
        int lastImageIndex = (int) gd.getNextNumber() - 1;
        int imageIndexIncr = (int) gd.getNextNumber();
        String stringToContains = gd.getNextString();
        boolean virtual = gd.getNextBoolean();
        
        String pattern;
        FileFilter fileFilter;
        if (stringToContains.equals("*.*") || stringToContains.isEmpty())
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
        fileList = selectFiles(fileList, firstImageIndex, lastImageIndex, imageIndexIncr);
        
        
        // read first image to retrieve image dimensions
        Image firstImage = Imago.readImage(fileList[firstImageIndex], frame);
        Array<?> array0 = (Array<?>) firstImage.getData();
        int sizeX = array0.size(0);
        int sizeY = array0.size(1);

        // count the number of images to read
        int nImages = fileList.length;
        
        Array<?> array;
        if (virtual)
        {
            if (array0 instanceof UInt8Array)
            {
                array = new FileListUInt8ImageSeries(fileList, sizeX, sizeY);
            }
            else if (array0 instanceof UInt16Array)
            {
                array = new FileListUInt16ImageSeries(fileList, sizeX, sizeY);
            }
            else
            {
                throw new RuntimeException("Virtual image series implemented only for UInt8 and UInt16 arrays");
            }
        }
        else
        {
            // Allocate array for image
            int[] dims = new int[]{sizeX, sizeY, nImages};
            array = array0.newInstance(dims);
            
            // read each image to populate the array
            int[] pos = new int[3];
            pos[2] = 0;
            for (int i = 0; i < nImages; i++)
            {
                // display progress if possible
                if (frame instanceof ImageFrame)
                {
                    int progress = (int) (i * 100.0 / nImages);
                    ((ImageFrame) frame).getStatusBar().setProgressBarPercent(progress);
                }
                
                // read current slice
                Image image = Imago.readImage(fileList[i], frame);
                Array<?> sliceArray = image.getData();
                
                // fill 3D array with current slice content
                int[] slicePos = new int[2];
                for (int y = 0; y < sizeY; y++)
                {
                    slicePos[1] = pos[1] = y;
                    for (int x = 0; x < sizeX; x++)
                    {
                        slicePos[0] = pos[0] = x;
                        ((Array<Object>) array).set(pos, sliceArray.get(slicePos));
                    }
                }
    
                pos[2]++;
            }
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
    
    private static final File[] selectFiles(File[] initialList, int first, int last, int step)
    {
        int nFiles = (last - first) / step;
        File[] fileList = new File[nFiles];
        for (int i = 0, index = first; i < nFiles; i++, index+=step)
        {
            fileList[i] = initialList[index];
        }
        return fileList;
    }
    
}
