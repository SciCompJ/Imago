/**
 * 
 */
package imago.image.plugins.edit;

import java.util.Map;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.JTableFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.image.Image;
import net.sci.image.io.tiff.Entry;
import net.sci.image.io.tiff.TiffTag;

/**
 * Displays the list of tags retrieved from an image stored in TIFF format.
 * 
 * @see imago.image.plugins.file.PrintImageFileTiffTags
 * 
 * @author dlegland
 */
public class PrintImageTiffTags implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public PrintImageTiffTags()
    {
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        
        if (!image.metadata.containsKey("tiff-tags"))
        {
            frame.showMessage("This image does not contain any tag", "Show Tiff Tags");
            return;
        }
        
        Map<Integer, TiffTag> knownTags = TiffTag.getAllTags();
        
        // display tags on console
        @SuppressWarnings("unchecked")
        Map<Integer, Entry> entries = (Map<Integer, Entry>) image.metadata.get("tiff-tags");
        for (Entry entry : entries.values())
        {
            TiffTag tag = knownTags.get(entry.code);
            String id = tag == null ? "" : " (" + tag.name + ")";
            String info = String.format("Tag code: %5d %-30s", entry.code, id);
            System.out.println(info + "\tType=" + entry.type + ", \tcount=" + entry.count + ", content=" + entry.contentSummary());
        }

        Object[][] data = createTiffTagData(image);
        String[] colNames = new String[]{"Code", "Name", "Origin", "Value"};
        
        new JTableFrame(frame, "Tiff Tags", data, colNames).setVisible(true);
    }
    
    private static final Object[][] createTiffTagData(Image image)
    {
        // retrieve the map of tags
        @SuppressWarnings("unchecked")
        Map<Integer, Entry> entries = (Map<Integer, Entry>) image.metadata.get("tiff-tags");
        Map<Integer, TiffTag> tiffTags = TiffTag.getAllTags();
        
        // Table header
        int nRows = entries.size();
         
        // Convert numeric values to table of objects
        int nCols = 4;
        Object[][] data = new Object[nRows][4];
        int iRow = 0;
        for (Entry entry : entries.values())
        {
            Object[] row = new Object[nCols];
            row[0] = entry.code;
            TiffTag tag = tiffTags.get(entry.code);
            row[1] = tag != null ? tag.name : "Unknown";
            row[2] = tag != null ? (tag.tagSet != null ? tag.tagSet.getName() : "Unknown") : "Unknown";
            row[3] = entry.contentSummary();
            data[iRow++] = row;
        }
        
        return data;
    }
}
