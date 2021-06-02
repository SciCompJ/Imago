package imago.plugin.plugin.crop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.app.ImagoApp;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ShapeNode;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoEmptyFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.dialogs.AlgoProgressMonitor;
import imago.gui.tool.SelectPolygonTool;
import imago.gui.viewer.StackSliceViewer;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.polygon.LineString2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;

/**
 * Start the CreateSurface3DPlugin plugin.
 * 
 * Opens a dialogs with various widgets, most of them being available once an image is loaded.
 * 
 * @author dlegland
 *
 */
public class CreateSurface3DPlugin implements FramePlugin, ListSelectionListener
{
    ImagoFrame parentFrame;
    
    ImageFrame imageFrame;
    JFrame jframe;
    
    Surface3D surf3d;
    
    
    // menu items
    JMenuItem loadPolylinesItem;
    JMenuItem savePolylinesItem;
    
    JLabel imageNameLabel;
    
    // buttons
    JButton openImageButton;
    JButton addPolylineButton;
    JButton removePolylineButton;
    JButton interpolateButton;
    JButton unfoldImageButton;

    // the widget that displays the names of base polylines
    JList<String> roiList;
    
    private JFileChooser openWindow = null;
    private JFileChooser saveWindow = null;

    
    public CreateSurface3DPlugin()
    {
        System.out.println("create the CreateSurface3D plugin");
    }
    
    @Override
    public void run(ImagoFrame parentFrame, String args)
    {
        System.out.println("start CreateSurface3D plugin");
        this.parentFrame = parentFrame;
        this.jframe = createFrame(parentFrame.getWidget());
    }
    
    private JFrame createFrame(JFrame parentFrame)
    {
        JFrame frame = new JFrame("Create Surface 3D");
        
        createMenu(frame);
        setupLayout(frame);
        
        
        java.awt.Point pos = parentFrame.getLocation();
        frame.setLocation(pos.x + 20, pos.y + 20);
        frame.setVisible(true);
        return frame;
    }
    
    private void createMenu(JFrame frame)
    {
        // init menu items
        loadPolylinesItem = new JMenuItem("Load Polylines...");
        loadPolylinesItem.addActionListener(evt -> loadPolylines());
        savePolylinesItem = new JMenuItem("Save Polylines...");
        savePolylinesItem.addActionListener(evt -> savePolylines());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(loadPolylinesItem);
        fileMenu.addSeparator();
        fileMenu.add(savePolylinesItem);
        
        
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
    }
    
    private void setupLayout(JFrame frame)
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        
        JPanel controlsPanel = new JPanel();
        
        // create the buttons
        openImageButton = new JButton("Open Image...");
        openImageButton.addActionListener(evt -> onOpenImageButton());
        addPolylineButton = new JButton("Add Polyline");
        addPolylineButton.addActionListener(evt -> onAddPolylineButton());
        addPolylineButton.setEnabled(false);
        removePolylineButton = new JButton("Remove Polyline");
        removePolylineButton.addActionListener(evt -> onRemovePolylineButton());
        removePolylineButton.setEnabled(false);
        interpolateButton = new JButton("Interpolate");
        interpolateButton.addActionListener(evt -> onInterpolatePolylinesButton());
        interpolateButton.setEnabled(false);
        unfoldImageButton = new JButton("Unfold Image...");
        unfoldImageButton.addActionListener(evt -> onUnfoldImageButton());
        unfoldImageButton.setEnabled(false);

        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel btnPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        btnPanel.add(new JLabel(" "));
        btnPanel.add(openImageButton);
        btnPanel.add(addPolylineButton);
        btnPanel.add(removePolylineButton);
        btnPanel.add(interpolateButton);
        btnPanel.add(unfoldImageButton);
        controlsPanel.add(btnPanel);
        mainPanel.add(controlsPanel);
        
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label =new JLabel("Polyline list:");
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        listPanel.add(label, BorderLayout.NORTH);
        
        roiList = new JList<String>(new String[] {});
        roiList.setVisibleRowCount(12);
        roiList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roiList.addListSelectionListener(this);
        
        JScrollPane listScroller = new JScrollPane(roiList);
        listScroller.setPreferredSize(new Dimension(140, 100));
        listPanel.add(listScroller, BorderLayout.CENTER);
        
        mainPanel.add(listPanel);
        
        imageNameLabel = new JLabel("Current Image: (none)");
        imageNameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.setLayout(new BorderLayout());
        frame.add(imageNameLabel, BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);
        
        frame.setSize(320, 360);
    }
    
    /**
     * Callback for the "Load Polylines" menu item.
     */
    public void loadPolylines()
    {
        System.out.println("Load polylines");
        
        if (!surf3d.getPolylinesNode().getSliceIndices().isEmpty())
        {
            int dialogResult = JOptionPane.showConfirmDialog(imageFrame.getWidget(),
                    "This frame contains Surface3D objects that will be removed.\nContinue anyway?",
                    "Surface3D Warning", JOptionPane.YES_NO_OPTION);
            if (dialogResult != JOptionPane.YES_OPTION)
            {
                return;
            }
        }

        // reset current state of the Crop3D plugin
        surf3d.initializeNodes();
        
        // create file dialog using last open path
        String lastPath = ".";
        openWindow = new JFileChooser(lastPath);
        openWindow.setDialogTitle("Read list of input polylines");
        openWindow.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));

        // Open dialog to choose the file
        int ret = openWindow.showOpenDialog(imageFrame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION) 
        {
            return;
        }

        // Check the chosen file is state
        File file = openWindow.getSelectedFile();
        if (!file.exists())
        {
            return;
        }
        
        try
        {
            surf3d.readPolylinesFromJson(file);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        updatePolylineListView();
        
        // need to call this to update items to display 
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    
    /**
     * Callback for the "Save Polylines" menu item.
     */
    public void savePolylines()
    {
        System.out.println("Save polylines");
        
        ImageSerialSectionsNode polyNode = surf3d.getPolylinesNode();
        if (polyNode == null)
        {
            System.err.println("Current image does not contain Surface3D polylines information");
            return;
        }
        
        // create file dialog using last save path
        String imageName = imageFrame.getImage().getName();
        saveWindow = new JFileChooser(new File(imageName + ".json"));
        saveWindow.setDialogTitle("Save list of input polygons");
        saveWindow.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));

        // Open dialog to choose the file
        int ret = saveWindow.showSaveDialog(imageFrame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION) 
        {
            return;
        }

        // Check the chosen file is valid
        File file = saveWindow.getSelectedFile();
        if (!file.getName().endsWith(".json"))
        {
            File parent = file.getParentFile();
            file = new File(parent, file.getName() + ".json");
        }
        
        try 
        {
            surf3d.savePolylinesAsJson(file);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Callback for the "Open Image..." button.
     */
    public void onOpenImageButton()
    {
        this.imageFrame = null;
        chooseInputImage(parentFrame);
        if (this.imageFrame == null)
        {
            return;
        }
        
        this.surf3d = new Surface3D(imageFrame);
        this.surf3d.addAlgoListener(imageFrame);
        
        this.surf3d.initializeNodes();

        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
        viewer.setCurrentTool(new SelectPolygonTool(imageFrame, "selectPolyline"));
        
        imageNameLabel.setText("Current Image: " + this.imageFrame.getImageHandle().getImage().getName());
        
        addPolylineButton.setEnabled(true);
        removePolylineButton.setEnabled(true);
        interpolateButton.setEnabled(true);
    }
    
    private void chooseInputImage(ImagoFrame parentFrame)
    {
        // create file dialog to read the 3D TIFF Image
        //        String lastPath = frame.getLastOpenPath();
        String lastPath = "D:/images/wheat/perigrain/Psiche_2018/HR/selection";
        JFileChooser openWindow = new JFileChooser(lastPath);
        openWindow.setDialogTitle("Choose TIFF 3D Image");
        openWindow.setFileFilter(new FileNameExtensionFilter("TIFF files (*.tif, *.tiff)", "tif", "tiff"));

        // Open dialog to choose the file
        int ret = openWindow.showOpenDialog(parentFrame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION) 
        {
            return;
        }

        // Check the chosen file is valid
        File file = openWindow.getSelectedFile();
        if (!file.isFile()) 
        {
            return;
        }

        // eventually keep path for future opening
        String path = file.getPath();
        lastPath = parentFrame.getLastOpenPath();
        if (lastPath == null || lastPath.isEmpty())
        {
            System.out.println("update frame path");
            parentFrame.setLastOpenPath(path);
        }


        // Read a 3D virtual image from the chosen file
        Image image;
        try 
        {
            TiffImageReader reader = new TiffImageReader(file);
            image = reader.readVirtualImage3D();
        }
        catch (Exception ex) 
        {
            System.err.println(ex);
            ImagoGui.showErrorDialog(parentFrame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
            return;
        }

        // add the image document to GUI
        this.imageFrame = parentFrame.createImageFrame(image);
        this.imageFrame.setLastOpenPath(path);
    }

    /**
     * Callback for the "Add Polyline" button.
     */
    public void onAddPolylineButton()
    {
        ImageViewer viewer = imageFrame.getImageView();

        StackSliceViewer piv = (StackSliceViewer) viewer;
        int sliceIndex = piv.getSliceIndex();
        
        Geometry2D selection = piv.getSelection();
        if (!(selection instanceof Polyline2D))
        {
            System.out.println("requires selection to be a simple polyline");
            return;
        }
        
        // add polyline to current slice
        Polyline2D poly = (Polyline2D) selection;
        surf3d.addPolyline(sliceIndex, poly);
        
        updatePolylineListView();
        
        // clear selection of current viewer
        piv.setSelection(null);
        
        // need to call this to update items to display 
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    /**
     * Callback for the "Remove Polygon" button.
     * 
     * Removes the polygon identified by the line selected in the polygon list,
     * and updates the display.
     */
    public void onRemovePolylineButton()
    {
        // retrieve index of selected slice polygon
        int index = roiList.getSelectedIndex();
        if (index == -1)
        {
            return;
        }
        
        String name = roiList.getSelectedValue();
        int sliceIndex = Integer.parseInt(name.substring(6).trim());

        
        ImageSerialSectionsNode group = surf3d.getPolylinesNode();
        group.removeSliceNode(sliceIndex);
        
        updatePolylineListView();
        
        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    private void updatePolylineListView()
    {
        ImageSerialSectionsNode group = surf3d.getPolylinesNode();
        
        
        Collection<Integer> sliceIndices = group.getSliceIndices(); 
        int nPolys = sliceIndices.size();
        String[] strings = new String[nPolys];
        int i = 0;
        for (int sliceIndex : sliceIndices)
        {
            strings[i++] = "Slice "  + sliceIndex;
        }
        
        this.roiList.setListData(strings);
    }
    
    /**
     * Callback for the "Interpolate Polygons" button. The method retrieves the
     * list of manually selected polygons, and generate intermediate polygons
     * for other slices. Interpolated polygons are stored in the "interp" node
     * of the ImageHandle.
     * 
     * 
     * @see Crop3D.interpolatePolygons()
     */
    public void onInterpolatePolylinesButton()
    {
        // Configure a progress monitor to display progress
        AlgoProgressMonitor progress = new AlgoProgressMonitor(this.parentFrame, "Interpolate Polylines"); 
        this.surf3d.addAlgoListener(progress);
        
        // Run the process in a new Thread to avoid locking widget updates
        Thread t = new Thread()
        {
            public void run()
            {
                try 
                {
                    surf3d.interpolatePolylines();
                    progress.setProgressRatio(1.0);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace(System.err);
                    ImagoGui.showExceptionDialog(imageFrame, ex, "Interpolation Error");
                }
                finally
                {
                    surf3d.removeAlgoListener(progress);
                }
            }
        };
        t.start();
        
        this.unfoldImageButton.setEnabled(true);

        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    /**
     * Callback for the "Unfold Image" button. This opens a dialog to choose a
     * file in MetaImage format ("*.mhd"). The cropped image is stored in a pair
     * of files: the mhd file contains the header, while the raw file contains
     * the binary data.
     */
    public void onUnfoldImageButton()
    {
        ImageSerialSectionsNode polyNode = surf3d.getPolylinesNode();
        if (polyNode == null)
        {
            System.err.println("Current image does not contain Surface3D polyline information");
            return;
        }
        
        // Compute default values
        ShapeNode shapeNode = (ShapeNode) polyNode.children().iterator().next().children().iterator().next();
        LineString2D poly = (LineString2D) shapeNode.getGeometry();
        int defaultPointNumber = (int) Math.round(poly.length());

        // Choose option for new image
        GenericDialog dlg = new GenericDialog(parentFrame, "Unfold Image");
        dlg.addNumericField("Width", defaultPointNumber, 0, "The number of points to re-interpolate polylines");
        dlg.addNumericField("Min. Depth", -20, 0);
        dlg.addNumericField("Max. Depth", +20, 0);
        
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        int pointNumber = (int) dlg.getNextNumber();
        int minDepth = (int) dlg.getNextNumber();
        int maxDepth = (int) dlg.getNextNumber();
        
                
        // create dialog to save file
        String imageName = imageFrame.getImage().getName();
        JFileChooser saveDlg = new JFileChooser(new File(imageName + "_surfFlat.mhd"));
        saveDlg.setDialogTitle("Select File for saving result");
        saveDlg.setFileFilter(new FileNameExtensionFilter("MetaImage File (*.mhd)", "mhd"));

        // Open dialog to choose the file
        int ret = saveDlg.showSaveDialog(imageFrame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION) 
        {
            return;
        }

        // Check the chosen file is valid
        File file = saveDlg.getSelectedFile();
        if (!file.getName().endsWith(".mhd"))
        {
            File parent = file.getParentFile();
            file = new File(parent, file.getName() + ".mhd");
        }
        final File finalFile = file;
        
        
        // Configure a progress monitor to display progress
        AlgoProgressMonitor progress = new AlgoProgressMonitor(this.parentFrame, "Compute 3D Crop"); 
        this.surf3d.addAlgoListener(progress);
        
        // Run the process in a new Thread to avoid locking widget updates
        Thread t = new Thread()
        {
            public void run()
            {
                try 
                {
                    surf3d.flattenSurface3d(pointNumber, minDepth, maxDepth, finalFile);
                    progress.setProgressRatio(1.0);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace(System.err);
                    ImagoGui.showExceptionDialog(imageFrame, ex, "Crop 3D Error");
                }
                finally
                {
                    surf3d.removeAlgoListener(progress);
                }
            }
        };
        t.start();
    }

    /**
     * Called when the selected polygon in the polygon list is updated.
     * 
     * The method identifies the new current slice by parsing the name of the
     * selected polygon, and updates the viewer accordingly.
     * 
     * @param evt
     *            the event sent by the list widget
     */
    @Override
    public void valueChanged(ListSelectionEvent evt)
    {
        if (evt.getValueIsAdjusting() == false)
        {
            int index = roiList.getSelectedIndex();
            if (index == -1)
            {
                return;
            }
            
            String name = roiList.getSelectedValue();
            int sliceIndex = Integer.parseInt(name.substring(6).trim());
            
             
            // need to call this to update items to display
            StackSliceViewer viewer = (StackSliceViewer) imageFrame.getImageView();
            viewer.setSliceIndex(sliceIndex);
            
            viewer.refreshDisplay(); 
            viewer.repaint();
        }
    }

    /**
     * Static method used for debugging purpose.
     * 
     * @param args
     *            optional arguments not used in practice.
     */
    public static final void main(String... args)
    {
        System.out.println("run...");
        ImagoFrame parentFrame = new ImagoEmptyFrame(new ImagoGui(new ImagoApp()));
        
        CreateSurface3DPlugin plugin = new CreateSurface3DPlugin();
        
        plugin.run(parentFrame, null);
    }
}
