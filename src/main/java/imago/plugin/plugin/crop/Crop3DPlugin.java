package imago.plugin.plugin.crop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.app.ImagoApp;
import imago.app.scene.ImageSerialSectionsNode;
import imago.gui.FramePlugin;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.dialogs.AlgoProgressMonitor;
import imago.gui.frames.ImageFrame;
import imago.gui.frames.ImagoEmptyFrame;
import imago.gui.tool.SelectPolygonTool;
import imago.gui.viewer.StackSliceViewer;
import net.sci.array.scalar.UInt8Array3D;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;

/**
 * Start the Crop3D plugin.
 * 
 * Opens a dialogs with various widgets, most of them being available once an image is loaded.
 * 
 * @author dlegland
 *
 */
public class Crop3DPlugin implements FramePlugin, ListSelectionListener
{
    ImagoFrame parentFrame;
    
    ImageFrame imageFrame;
    JFrame jframe;
    
    Crop3D crop3d;
    
    
    // menu items
    JMenuItem loadAnalysisItem;
    JMenuItem loadPolygonsItem;
    JMenuItem saveAnalysisItem;
    JMenuItem savePolygonsItem;
    
    JLabel imageNameLabel;
    
    // buttons
    JButton openImageButton;
    JButton addPolygonButton;
    JButton removePolygonButton;
    JButton interpolateButton;
    JButton previewCropImageButton;
    JButton cropImageButton;
    
    // the widget that displays the names of base polygons
    JList<String> roiList;
    
    private JFileChooser openWindow = null;
    private JFileChooser saveWindow = null;

    private static FileFilter crop3dFileFilter = new FileNameExtensionFilter("Crop3D files (*.crop3d)", "crop3d");
    private static FileFilter jsonFileFilter = new FileNameExtensionFilter("All JSON files (*.json)", "json");

    String imagePath = null;
    String lastOpenPath = "D:/images/wheat/perigrain/Psiche_2018/HR/selection";

    public Crop3DPlugin()
    {
        System.out.println("create the crop3D plugin");
    }
    
    @Override
    public void run(ImagoFrame parentFrame, String args)
    {
        System.out.println("run Crop3D plugin");
        this.parentFrame = parentFrame;
        this.jframe = createFrame(parentFrame.getWidget());
    }
    
    private JFrame createFrame(JFrame parentFrame)
    {
        JFrame frame = new JFrame("Crop 3D");
        
        createMenu(frame);
        setupLayout(frame);
        
        java.awt.Point pos = parentFrame.getLocation();
        frame.setLocation(pos.x + 20, pos.y + 20);
        frame.setVisible(true);
        return frame;
    }
    
    private void createMenu(JFrame frame)
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        addMenuItem(fileMenu, "Load Crop3D File...", evt -> onLoadAnalysis());
        addMenuItem(fileMenu, "Load Polygons...", evt -> onLoadPolygons());
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Save Crop3D File...", evt -> onSaveAnalysis());
        addMenuItem(fileMenu, "Save Polygons...", evt -> onSavePolygons());
        
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
    }
    
    private JMenuItem addMenuItem(JMenu parentMenu, String label, Consumer<ActionEvent> action)
    {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(evt -> action.accept(evt));
        parentMenu.add(item);
        return item;
    }
    
    private void setupLayout(JFrame frame)
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        
        JPanel controlsPanel = new JPanel();
        
        // create the buttons
        openImageButton = new JButton("Open Image...");
        openImageButton.addActionListener(evt -> onOpenImageButton());
        addPolygonButton = new JButton("Add Polygon");
        addPolygonButton.addActionListener(evt -> onAddPolygonButton());
        addPolygonButton.setEnabled(false);
        removePolygonButton = new JButton("Remove Polygon");
        removePolygonButton.addActionListener(evt -> onRemovePolygonButton());
        removePolygonButton.setEnabled(false);
        interpolateButton = new JButton("Interpolate");
        interpolateButton.addActionListener(evt -> onInterpolatePolygonsButton());
        interpolateButton.setEnabled(false);
        previewCropImageButton = new JButton("Preview Crop");
        previewCropImageButton.addActionListener(evt -> onPreviewCropImageButton());
        previewCropImageButton.setEnabled(false);
        cropImageButton = new JButton("Crop Image...");
        cropImageButton.addActionListener(evt -> onCropImageButton());
        cropImageButton.setEnabled(false);
        
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel btnPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        btnPanel.add(new JLabel(" "));
        btnPanel.add(openImageButton);
        btnPanel.add(addPolygonButton);
        btnPanel.add(removePolygonButton);
        btnPanel.add(interpolateButton);
        btnPanel.add(previewCropImageButton);
        btnPanel.add(cropImageButton);
        controlsPanel.add(btnPanel);
        mainPanel.add(controlsPanel);
        
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label =new JLabel("Polygon list:");
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
     * Callback for the "Load Analysis" menu item.
     */
    public void onLoadAnalysis()
    {
        System.out.println("Load analysis");
        
        // create file dialog using last open path
        openWindow = new JFileChooser(this.lastOpenPath);
        openWindow.setDialogTitle("Read Crop3D analysis");
        openWindow.addChoosableFileFilter(crop3dFileFilter);
        openWindow.addChoosableFileFilter(jsonFileFilter);
        openWindow.setFileFilter(crop3dFileFilter);
        
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
        
        try 
        {
            Crop3D.loadAnalysis(file, this.parentFrame);
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
            throw new RuntimeException("Error during import of Crop3D analysis.", ex);
        }
    }
    
    /**
     * Callback for the "Load Polygons" menu item.
     */
    public void onLoadPolygons()
    {
        System.out.println("Load polygons");
        
        if (!crop3d.getPolygonsNode().getSliceIndices().isEmpty())
        {
            int dialogResult = JOptionPane.showConfirmDialog(imageFrame.getWidget(),
                    "This frame contains Crop3D objects that will be removed.\nContinue anyway?",
                    "Crop3D Warning", JOptionPane.YES_NO_OPTION);
            if (dialogResult != JOptionPane.YES_OPTION)
            {
                return;
            }
        }

        // reset current state of the Crop3D plugin
        crop3d.initializeCrop3dNodes();
        
        // create file dialog using last open path
        String lastPath = ".";
        openWindow = new JFileChooser(lastPath);
        openWindow.setDialogTitle("Read list of input polygons");
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
            crop3d.readPolygonsFromJson(file);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        updatePolygonListView();
        
        // need to call this to update items to display 
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    /**
     * Callback for the "Save Analysis" menu item. Saves a ".crop3d" file in
     * JSON format containing name of reference image, and polygon data.
     */
    public void onSaveAnalysis()
    {
        System.out.println("Save Analysis");
        
        // create file dialog using last save path
        String imageName = imageFrame.getImage().getName();
        System.out.println("Current Image name: " + imageName);
        
        saveWindow = new JFileChooser();
        saveWindow.setDialogTitle("Save Crop3D Analysis");
        saveWindow.addChoosableFileFilter(crop3dFileFilter);
        saveWindow.addChoosableFileFilter(jsonFileFilter);
        saveWindow.setFileFilter(crop3dFileFilter);
        saveWindow.setCurrentDirectory(new File(lastOpenPath));
        saveWindow.setSelectedFile(new File(imageName + ".crop3d"));
        
        // Open dialog to choose the file
        int ret = saveWindow.showSaveDialog(imageFrame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION) 
        {
            return;
        }

        // Check the chosen file is valid
        File file = saveWindow.getSelectedFile();
        if (!file.getName().endsWith(".crop3d"))
        {
            File parent = file.getParentFile();
            file = new File(parent, file.getName() + ".crop3d");
        }
        
        try 
        {
            Crop3DDataWriter writer = new Crop3DDataWriter(file);
            writer.writeCrop3D(this.crop3d);
            writer.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        System.out.println("Saving Crop3D terminated.");
    }

    
    /**
     * Callback for the "Save Polygons" menu item.
     */
    public void onSavePolygons()
    {
        System.out.println("Save polygons");
        
        ImageSerialSectionsNode polyNode = crop3d.getPolygonsNode();
        if (polyNode == null)
        {
            System.err.println("Current image does not contain Crop3D polygon information");
            return;
        }
        
        // create file dialog using last save path
        String imageName = imageFrame.getImage().getName();
        saveWindow = new JFileChooser(new File(imageName + ".json"));
        saveWindow.setDialogTitle("Save list of input polygons");
        saveWindow.setCurrentDirectory(new File(lastOpenPath));
        saveWindow.setFileFilter(jsonFileFilter);

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
            Crop3DDataWriter writer = new Crop3DDataWriter(file);
            writer.writePolygons(polyNode);
            writer.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        System.out.println("Saving polygon terminated.");
    }
    
    /**
     * Callback for the "Open Image..." button.
     */
    public void onOpenImageButton()
    {
        // Ask for the filename of the image to open
        File file = chooseInputImageFile(parentFrame);
        if (file == null)
        {
            return;
        }
        
        // clear current image data
        this.imageFrame = null;
        
        // create a viewer and a Crop3D object for the new image
        openImage(file);
    }
    
    private File chooseInputImageFile(ImagoFrame parentFrame)
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
            return null;
        }

        // Check the chosen file is valid
        File file = openWindow.getSelectedFile();
        
        // eventually keep path for future opening
        String path = file.getPath();
        lastPath = parentFrame.getLastOpenPath();
        if (lastPath == null || lastPath.isEmpty())
        {
            System.out.println("update frame path");
            parentFrame.setLastOpenPath(path);
        }
        
        return file;
    }
    
    /**
     * Reads a 3D virtual image from the specified file, and creates a new
     * ImageViewer and a new Crop3D object associated to the current frame.
     * 
     * @param file
     *            the image file to open.
     */
    public void openImage(File file)
    {
        // open a virtual image from the file
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
        
        // create viewer for the image,
        // add the image document to GUI
        this.imageFrame = parentFrame.createImageFrame(image);

        // create the associated Crop3D 
        this.crop3d = new Crop3D(imageFrame.getImageHandle());
        this.crop3d.addAlgoListener(imageFrame);

        // and updates the current frame
        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
        viewer.setCurrentTool(new SelectPolygonTool(imageFrame, "selectPolygon"));
        
        imageNameLabel.setText("Current Image: " + image.getName());

        // update last open path
        this.imagePath = file.getAbsolutePath();
        this.lastOpenPath = file.getPath();
        this.imageFrame.setLastOpenPath(this.lastOpenPath);
        
        // enable widget for next processing steps 
        addPolygonButton.setEnabled(true);
        removePolygonButton.setEnabled(true);
        interpolateButton.setEnabled(true);
    }

    
    /**
     * Callback for the "Add Polygon" button.
     */
    public void onAddPolygonButton()
    {
        ImageViewer viewer = imageFrame.getImageView();

        StackSliceViewer piv = (StackSliceViewer) viewer;
        int sliceIndex = piv.getSliceIndex();
        
        Geometry selection = piv.getSelection();
        if (!(selection instanceof Polygon2D))
        {
            System.out.println("requires selection to be a simple polygon");
            return;
        }
        
        // enforce counter-clockwise polygon
        Polygon2D poly = (Polygon2D) selection;
        if (poly.signedArea() < 0)
        {
            poly = poly.complement();
        }
        
        crop3d.addPolygon(sliceIndex, poly);
        
        updatePolygonListView();
        
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
    public void onRemovePolygonButton()
    {
        // retrieve index of selected slice polygon
        int index = roiList.getSelectedIndex();
        if (index == -1)
        {
            return;
        }
        
        String name = roiList.getSelectedValue();
        int sliceIndex = Integer.parseInt(name.substring(6).trim());

        
        ImageSerialSectionsNode group = crop3d.getPolygonsNode();
        group.removeSliceNode(sliceIndex);
        
        updatePolygonListView();
        
        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    public void updatePolygonListView()
    {
        ImageSerialSectionsNode group = crop3d.getPolygonsNode();
        
        
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
    public void onInterpolatePolygonsButton()
    {
        // check current image state
        ImageSerialSectionsNode polyNode = crop3d.getPolygonsNode();
        if (polyNode.isLeaf())
        {
            JOptionPane.showMessageDialog(imageFrame.getWidget(),
                    "Requires the frame to contains valid Crop3D Polygons",
                    "Crop3D Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Configure a progress monitor to display progress
        AlgoProgressMonitor progress = new AlgoProgressMonitor(this.parentFrame, "Interpolate Polygons"); 
        this.crop3d.addAlgoListener(progress);
        
        // Run the process in a new Thread to avoid locking widget updates
        Thread t = new Thread()
        {
            public void run()
            {
                try 
                {
                    crop3d.smoothAndInterpolatePolygons();
                    progress.setProgressRatio(1.0);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace(System.err);
                    ImagoGui.showExceptionDialog(imageFrame, ex, "Interpolation Error");
                }
                finally
                {
                    crop3d.removeAlgoListener(progress);
                }
            }
        };
        t.start();
        
        // validates the next operations
        this.previewCropImageButton.setEnabled(true);
        this.cropImageButton.setEnabled(true);

        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    /**
     * Callback for the "Preview Crop Image" button.
     */
    public void onPreviewCropImageButton()
    {
        ImageSerialSectionsNode polyNode = crop3d.getPolygonsNode();
        if (polyNode == null)
        {
            System.err.println("Current image does not contain Crop3D polygon information");
            return;
        }
        
        // Create new cropped image using virtual crop array
        UInt8Array3D array = (UInt8Array3D) imageFrame.getImage().getData();
        ImageSerialSectionsNode cropNode = crop3d.getInterpolatedPolygonsNode();
        UInt8Array3D cropArray = new CroppedUInt8Array3D(array, cropNode);
        Image refImage = imageFrame.getImage();
        Image cropImage = new Image(cropArray, refImage);
        cropImage.setName(refImage.getName() + "-crop");
        imageFrame.createImageFrame(cropImage);
    }
    
    /**
     * Callback for the "Crop Image" button. This opens a dialog to choose a
     * file in MetaImage format ("*.mhd"). The cropped image is stored in a pair
     * of files: the mhd file contains the header, while the raw file contains
     * the binary data.
     */
    public void onCropImageButton()
    {
        ImageSerialSectionsNode polyNode = crop3d.getPolygonsNode();
        if (polyNode == null)
        {
            System.err.println("Current image does not contain Crop3D polygon information");
            return;
        }
        
        // create dialog to save file
        String imageName = imageFrame.getImage().getName();
        JFileChooser saveDlg = new JFileChooser(new File(imageName + "_crop.mhd"));
        saveDlg.setDialogTitle("Select File for crop result");
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
        this.crop3d.addAlgoListener(progress);
        
        // Run the process in a new Thread to avoid locking widget updates
        Thread t = new Thread()
        {
            public void run()
            {
                try 
                {
                    crop3d.computeCroppedImage(finalFile);
                    progress.setProgressRatio(1.0);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace(System.err);
                    ImagoGui.showExceptionDialog(imageFrame, ex, "Crop 3D Error");
                }
                finally
                {
                    crop3d.removeAlgoListener(progress);
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
        
        Crop3DPlugin plugin = new Crop3DPlugin();
        
        plugin.run(parentFrame, null);
    }
}
