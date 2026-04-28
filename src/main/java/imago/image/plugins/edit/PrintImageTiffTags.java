/**
 * 
 */
package imago.image.plugins.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.table.RowNumberTable;
import imago.gui.FramePlugin;
import net.sci.image.Image;
import net.sci.image.io.tiff.Entry;
import net.sci.image.io.tiff.TiffTag;

/**
 * Show the list of tags retrieved from an images stored in TIFF format.
 * 
 * @see imago.image.plugins.edit.PrintImageFileTiffTags
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
        
        // tries to display in a frame
        TiffTagsDisplayFrame tagsFrame = new TiffTagsDisplayFrame(frame, image);
        tagsFrame.setVisible(true);
    }
    
    class TiffTagsDisplayFrame extends ImagoFrame
    {
        Image image;
        
        protected TiffTagsDisplayFrame(ImagoFrame parent, Image image)
        {
            super(parent, "Tiff Tags");
            this.image = image;
            
            setupLayout();
            
            JFrame frame = (JFrame) getWidget();
            frame.pack();
            this.setVisible(true);

            // setup window listener
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent evt)
                {
                    TiffTagsDisplayFrame.this.close();
                }           
            });
        }
        
        private void setupLayout() 
        {
            // put into global layout
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.GREEN);

            // retrieve the map of tags
            @SuppressWarnings("unchecked")
            Map<Integer, Entry> entries = (Map<Integer, Entry>) image.metadata.get("tiff-tags");
            Map<Integer, TiffTag> tiffTags = TiffTag.getAllTags();
            
            // Table header
            String[] colNames = new String[]{"Code", "Name", "Origin", "Value"};
            int nRows = entries.size();
             
            // Convert numeric values to table of objects
            int nCols = colNames.length;
            Object[][] data = new Object[nRows][nCols];
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
            
            // create JTable object
            JTable jtable = new JTable(data, colNames);
            
            //add the table to the frame
            JScrollPane scrollPane = new JScrollPane(jtable);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            
            // decorate the scroll panel with label column
            JTable rowTable = new RowNumberTable(jtable);
            scrollPane.setRowHeaderView(rowTable);
            scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
            
            ((JFrame) this.getWidget()).setContentPane(mainPanel);
        }
	}
}
