/**
 * 
 */
package imago.image.plugin.edit;

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
import net.sci.image.io.tiff.TiffTag;

/**
 * Show the list of tags retrieved from an images stored in TIFF format.
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
        
        // display tags on console
        @SuppressWarnings("unchecked")
        Map<Integer, TiffTag> tiffTags = (Map<Integer, TiffTag>) image.metadata.get("tiff-tags");
        for (TiffTag tag : tiffTags.values())
        {
            String desc = tag.name == null ? "" : " (" + tag.name + ")";
            String info = String.format("Tag code: %5d %-30s", tag.code, desc);
            System.out.println(info + "\tType=" + tag.type + ", \tcount=" + tag.count + ", content=" + tag.content);
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
            Map<Integer, TiffTag> tiffTags = (Map<Integer, TiffTag>) image.metadata.get("tiff-tags");
            
            // Table header
            String[] colNames = new String[]{"Code", "Name", "Origin", "Value"};
            int nRows = tiffTags.size();
             
            // Convert numeric values to table of objects
            int nCols = colNames.length;
            Object[][] data = new Object[nRows][nCols];
            int iRow = 0;
            for (TiffTag tag : tiffTags.values())
            {
                Object[] row = new Object[nCols];
                row[0] = tag.code;
                row[1] = tag.name;
                row[2] = tag.tagSet == null ? "Unknown" : tag.tagSet.getName();
                row[3] = createContentString(tag.content);
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

        private String createContentString(Object obj)
        {
            if (isArray(obj))
            {
                if (obj instanceof int[])
                {
                    return convertIntArray((int[]) obj);
                }
                else if (obj instanceof byte[])
                {
                    return "byte[]";
                }
                else if (obj instanceof int[][])
                {
                    return convertIntIntArray((int[][]) obj);
                }
                else if (obj instanceof short[])
                {
                    return convertShortArray((short[]) obj);
                }
                else if (obj instanceof double[])
                {
                    return convertDoubleArray((double[]) obj);
                }
                else
                {
                    return "Array (unknown type)";
                }
            }
            else if (obj == null)
            {
                return "null";
            }
            else
            {
                return obj.toString();
            }
        }
        
        private boolean isArray(Object obj)
        {
            return obj!=null && obj.getClass().isArray();
        }
        
        private String convertIntArray(int[] obj)
        {
            StringBuffer buffer = new StringBuffer("Int[]{");
            if (obj.length > 0)
            {
                buffer.append(obj[0]);
            }
            for (int i = 1; i < obj.length; i++)
            {
                buffer.append(", " + obj[i]);
            }
            buffer.append("}");
            return buffer.toString();
        }

        private String convertIntIntArray(int[][] obj)
        {
            int nr = obj.length;
            if (nr == 0)
            {
                return "Int[][]{}";
            }
            
            int nc = obj[0].length;
            if (nc == 0)
            {
                return "Int[][]{}";
            }
            
            StringBuffer buffer = new StringBuffer("Int[][]{");
            for (int iRow = 0; iRow < nr; iRow++)
            {
                buffer.append("{");
                buffer.append(obj[iRow][0]);
                for (int iCol= 1; iCol < nc; iCol++)
                {
                    buffer.append(", " + obj[iRow][iCol]);
                }
                buffer.append("}");
            }
            
            buffer.append("}");
            return buffer.toString();
        }
        
        private String convertShortArray(short[] obj)
        {
            StringBuffer buffer = new StringBuffer("Short[]{");
            if (obj.length > 0)
            {
                buffer.append(obj[0]);
            }
            for (int i = 1; i < obj.length; i++)
            {
                buffer.append(", " + obj[i]);
            }
            buffer.append("}");
            return buffer.toString();
        }
        
        private String convertDoubleArray(double[] obj)
        {
            StringBuffer buffer = new StringBuffer("Double[]{");
            if (obj.length > 0)
            {
                buffer.append(obj[0]);
            }
            for (int i = 1; i < obj.length; i++)
            {
                buffer.append(", " + obj[i]);
            }
            buffer.append("}");
            return buffer.toString();
        }
	}
}
