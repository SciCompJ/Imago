/**
 * 
 */
package imago.plugin.plugin;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import imago.app.scene.Style;
import imago.gui.FramePlugin;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.LineString2D;
import net.sci.image.Image;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableReader;

/**
 * Imports a series of polylines obtained on XY cross sections of a 3D image.
 * 
 * Polylines are encapsulated into ShapeNode instances, each ShapeNode is
 * encapsulated into an "ImageSliceNode", and each Slice Node is added into a
 * "ImageSerialSectionsNode". The ImageSerialSectionsNode, at the end, is added
 * to the group node of the current Image Handler, using the file name as index.
 *
 * @see imago.app.scene.ShapeNode
 * 
 * @author dlegland
 *
 */
public class ImportImage3DPolylineSeries implements FramePlugin
{
    private JFileChooser openWindow = null;
    
    @Override
    public void run(ImagoFrame frame, String args)
    {
        File file = askForFile(frame);
        if (file == null || !file.exists())
        {
            return;
        }
        
        Table table = readTableFromFile(file);
        
        int nRows = table.rowCount();
        int nColumns = table.columnCount();
        System.out.println(table.rowCount() + " x " + table.columnCount());
        
        int nVertices = nColumns / 2;
        
        // setup drawing style
        Style style = new Style();
        style.setColor(Color.GREEN);
        style.setLineWidth(2.0);
        
        // create nodes corresponding to each polyline
        ImageSerialSectionsNode polylines = new ImageSerialSectionsNode(file.getName());
        for (int iRow = 0; iRow < nRows; iRow++)
        {
            double[] values = table.getRowValues(iRow);
            LineString2D polyline = LineString2D.create(nVertices);
            
            for (int iv = 0; iv < nVertices; iv++)
            {
                double x = values[iv];
                double y = values[iv + nVertices + 1];
                polyline.addVertex(new Point2D(x, y));
            }
            
            ImageSliceNode sliceNode = new ImageSliceNode(String.format("slice%03d", iRow), iRow);
            sliceNode.addNode(new ShapeNode("Curve", polyline, style));
            polylines.addSliceNode(sliceNode);
        }
        
        // get root node
        ImageFrame iFrame = (ImageFrame) frame;
        GroupNode rootNode = ((GroupNode) iFrame.getImageHandle().getRootNode());
        
        // remove old polyline node if it exists
        if (rootNode.hasChildWithName(polylines.getName()))
        {
            rootNode.removeNode(rootNode.getChild(polylines.getName()));
        }
        rootNode.addNode(polylines);
        
        // need to call this to update items to display 
        ImageViewer viewer = iFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    private File askForFile(ImagoFrame frame)
    {
        // create file dialog using last open path
        String lastPath = ".";
        openWindow = new JFileChooser(lastPath);
        openWindow.setDialogTitle("Import polyline series");
        openWindow.setFileFilter(new FileNameExtensionFilter("XY coords files (*.txt)", "txt"));
//        openWindow.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));

        // Open dialog to choose the file
        int ret = openWindow.showOpenDialog(frame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION) 
        {
            return null;
        }

        // Check the chosen file is state
        return openWindow.getSelectedFile();
    }
    
    private Table readTableFromFile(File file)
    {
        DelimitedTableReader reader = new DelimitedTableReader();
        reader.setReadHeader(true);
        reader.setDelimiters(" \t");
        reader.setReadRowNames(false);
        try
        {
            return reader.readTable(file);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Requires the parent frame to contain an instance of 3D image.
     * 
     * @param frame
     *            the parent frame
     * @return true if the parent frame is a 3D image viewer
     */
    public boolean isEnabled(ImagoFrame frame)
    {
        if (!(frame instanceof ImageFrame))
        {
            return false;
        }
        
        Image image = ((ImageFrame) frame).getImage();
        return image.getDimension() == 3;
    }
}
