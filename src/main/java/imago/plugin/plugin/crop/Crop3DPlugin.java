package imago.plugin.plugin.crop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.dialogs.AlgoProgressMonitor;
import imago.gui.frames.ImagoEmptyFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.image.StackSliceViewer;
import imago.gui.image.tools.SelectPolygonTool;
import imago.plugin.image.file.ImageFileFilters;
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
    // ===================================================================
    // Fields
    
    ImagoFrame parentFrame;
    
    ImageFrame imageFrame;
    JFrame jframe;
    
    /** The processing class */
    Crop3D crop3d;
    
    JLabel imageNameLabel = new JLabel("");
    
    // buttons
    JButton openImageButton;
    JButton addRegionButton;
    JButton removeRegionButton;
    JButton addPolygonButton;
    JButton removePolygonButton;
    JButton importPolygonsButton;
    JButton exportPolygonsButton;
    JButton interpolateButton;
    JButton previewCropImageButton;
    JButton cropImageButton;
    
    // the widget that displays the names of the crop regions
    JComboBox<String> regionComboBox;
    
    // the widget that displays the names of base polygons
    JList<String> cropItemList;

    private static FileFilter crop3dFileFilter = new FileNameExtensionFilter("Crop3D files (*.crop3d)", "crop3d");
    private static FileFilter jsonFileFilter = new FileNameExtensionFilter("All JSON files (*.json)", "json");
    private static FileFilter mhdFileFilter = new FileNameExtensionFilter("MetaImage Files (*.mhd)", "mhd");
    
    
    // ===================================================================
    // Constructor
    
    /**
     * Simply creates an empty plugin.
     */
    public Crop3DPlugin()
    {
    }

    
    // ===================================================================
    // Implementation of the Plugin interface
    
    @Override
    public void run(ImagoFrame parentFrame, String args)
    {
        this.parentFrame = parentFrame;
        this.jframe = createFrame(parentFrame.getWidget());
    }
    
    private JFrame createFrame(JFrame parentFrame)
    {
        JFrame frame = new JFrame("Crop 3D");
        
        // initialize widgets and setup layout
        createMenu(frame);
        initializeWidgets();
        setupLayout(frame);
        
        // setup listeners
        frame.addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent evt)
            {
                // close the corresponding image viewer if it exists
                if (imageFrame != null)
                {
                    imageFrame.close();
                }
                frame.setVisible(false);
                frame.dispose();
            }
        });
        
        // setup frame location according to parent frame location
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
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Save Crop3D File...", evt -> onSaveAnalysis());
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Close", evt -> onClose());
        menuBar.add(fileMenu);

        JMenu polygonsMenu = new JMenu("Polygons");
        addMenuItem(polygonsMenu, "Load Polygons...", evt -> onLoadPolygons());
        addMenuItem(polygonsMenu, "Load Image Serial Section Node...", evt -> onLoadImageSerialSectionNode());
        polygonsMenu.addSeparator();
        addMenuItem(polygonsMenu, "Save Polygons...", evt -> onSavePolygons());
        menuBar.add(polygonsMenu);
        
        frame.setJMenuBar(menuBar);
    }
    
    private JMenuItem addMenuItem(JMenu parentMenu, String label, Consumer<ActionEvent> action)
    {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(evt -> action.accept(evt));
        parentMenu.add(item);
        return item;
    }
    
    private void initializeWidgets()
    {
        openImageButton = new JButton("Choose...");
        openImageButton.addActionListener(evt -> onOpenImageButton());
        addRegionButton = new JButton("New...");
        addRegionButton.addActionListener(evt -> onAddRegionButton());
        addRegionButton.setEnabled(false);
        removeRegionButton = new JButton("Remove");
        removeRegionButton.addActionListener(evt -> onRemoveRegionButton());
        removeRegionButton.setEnabled(false);
        addPolygonButton = new JButton("Add");
        addPolygonButton.addActionListener(evt -> onAddPolygonButton());
        addPolygonButton.setEnabled(false);
        removePolygonButton = new JButton("Remove");
        removePolygonButton.addActionListener(evt -> onRemovePolygonButton());
        removePolygonButton.setEnabled(false);
        importPolygonsButton = new JButton("Load...");
        importPolygonsButton.addActionListener(evt -> onLoadPolygons());
        importPolygonsButton.setEnabled(false);
        exportPolygonsButton = new JButton("Save...");
        exportPolygonsButton.addActionListener(evt -> onSavePolygons());
        exportPolygonsButton.setEnabled(false);
        interpolateButton = new JButton("Interpolate");
        interpolateButton.addActionListener(evt -> onInterpolatePolygonsButton());
        interpolateButton.setEnabled(false);
        previewCropImageButton = new JButton("Crop Preview");
        previewCropImageButton.addActionListener(evt -> onPreviewCropImageButton());
        previewCropImageButton.setEnabled(false);
        cropImageButton = new JButton("Crop...");
        cropImageButton.addActionListener(evt -> onCropImageButton());
        cropImageButton.setEnabled(false);
        
        regionComboBox = new JComboBox<String>(new String[] {});
        regionComboBox.setMaximumSize(regionComboBox.getPreferredSize());
        regionComboBox.addActionListener(evt -> onCurrentRegionUpdated());

        cropItemList = new JList<String>(new String[] {});
        cropItemList.setVisibleRowCount(10);
        cropItemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cropItemList.addListSelectionListener(this);
    }

    private void setupLayout(JFrame frame)
    {
        // The main panel is composed of several framed panel
        // 1. Choice of reference image
        // 2. Choosing the name of crop regions
        // 3. Manual selection of crop items (polygons)
        // 4. Processing steps (interpolation, crop)
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel upperPanel = new JPanel(new BorderLayout());

        // 1. The panel for reference image file name
        JPanel referenceImagePanel = new JPanel();
        referenceImagePanel.setLayout(new BoxLayout(referenceImagePanel, BoxLayout.X_AXIS));
        referenceImagePanel.setBorder(BorderFactory.createTitledBorder("Reference Image"));
        referenceImagePanel.add(new JLabel("Image Name: "));
        referenceImagePanel.add(imageNameLabel);
        referenceImagePanel.add(Box.createHorizontalGlue());
        referenceImagePanel.add(openImageButton);
        
        upperPanel.add(referenceImagePanel, BorderLayout.NORTH);
        
        // 2. The panel for the crop regions
        JPanel cropRegionsPanel = new JPanel();
        cropRegionsPanel.setLayout(new GridLayout(1, 2, 5, 5));
        cropRegionsPanel.setBorder(BorderFactory.createTitledBorder("Regions"));
        
        // 2.1 The control panel for the regions
        JPanel regionButtonsPanel = new JPanel();
        regionButtonsPanel.add(addRegionButton);
        regionButtonsPanel.add(removeRegionButton);
        cropRegionsPanel.add(regionButtonsPanel);

        // 2.2 the list of regions
        JPanel regionListPanel = new JPanel();
        regionListPanel.setLayout(new BorderLayout());
        regionListPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        regionListPanel.add(regionComboBox);
        cropRegionsPanel.add(regionListPanel);

        upperPanel.add(cropRegionsPanel, BorderLayout.SOUTH);
        mainPanel.add(upperPanel, BorderLayout.NORTH);
        
        
        // 3. The panel for the crop items
        JPanel cropItemsPanel = new JPanel();
        cropItemsPanel.setLayout(new GridLayout(1, 2, 5, 5));
        cropItemsPanel.setBorder(BorderFactory.createTitledBorder("Crop Items"));
        
        // 3.1 The control panel for the crop items
        JPanel polyButtonsPanel = new JPanel(new GridLayout(8, 1));
        polyButtonsPanel.add(new JLabel(""));
        polyButtonsPanel.add(addPolygonButton);
        polyButtonsPanel.add(removePolygonButton);
        polyButtonsPanel.add(new JLabel(""));
        polyButtonsPanel.add(importPolygonsButton);
        polyButtonsPanel.add(exportPolygonsButton);
        polyButtonsPanel.add(new JLabel(""));
        cropItemsPanel.add(polyButtonsPanel);

        // 3.2 the list of crop items
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane listScroller = new JScrollPane(cropItemList);
        listScroller.setPreferredSize(new Dimension(80, 120));
        listPanel.add(listScroller, BorderLayout.CENTER);
        
        cropItemsPanel.add(listPanel);
        mainPanel.add(cropItemsPanel, BorderLayout.CENTER);
        
        
        // 4. The panel for the processing steps
        JPanel processPanel = new JPanel();
        processPanel.setBorder(BorderFactory.createTitledBorder("Process"));
        
        JPanel processButtonsPanel = new JPanel();
        processButtonsPanel.setLayout(new BoxLayout(processButtonsPanel, BoxLayout.X_AXIS));
        processButtonsPanel.add(interpolateButton);
        processButtonsPanel.add(previewCropImageButton);
        processButtonsPanel.add(cropImageButton);
        processPanel.add(processButtonsPanel);
        mainPanel.add(processPanel, BorderLayout.SOUTH);
        
        
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setSize(340, 420);
    }
    
    
    // ===================================================================
    // Widget callbacks
    
    /**
     * Callback for the "Load Analysis" menu item.
     */
    public void onLoadAnalysis()
    {
        System.out.println("Load analysis");
        
        // open a dialog to read a .crop3d or .json file
        File file = parentFrame.getGui().chooseFileToOpen(parentFrame,
                "Read Crop3D analysis", crop3dFileFilter, jsonFileFilter);
        if (file == null)
        {
            return;
        }

        // Check the chosen file is valid
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
     * Callback for the "Save Analysis" menu item. Saves a ".crop3d" file in
     * JSON format containing name of reference image, and polygon data.
     */
    public void onSaveAnalysis()
    {
        System.out.println("Save Analysis");
        
        // create file dialog using last save path
        String imageName = imageFrame.getImageHandle().getImage().getName();
        System.out.println("Current Image name: " + imageName);
        
        String defaultFileName = imageName + ".crop3d";
        File file = parentFrame.getGui().chooseFileToSave(parentFrame, 
                "Save Crop3D Analysis", defaultFileName, crop3dFileFilter, jsonFileFilter);
        if (file == null)
        {
            return;
        }

        // Check the chosen file is valid
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

    public void onClose()
    {
        this.jframe.setVisible(false);
        
        // clear inner variables
        this.crop3d = null;
        
        // close frames
        if (this.imageFrame != null)
        {
            this.imageFrame.close();
        }
        this.jframe.dispose();
    }
    
    /**
     * Callback for the "Load Polygons" menu item.
     */
    public void onLoadPolygons()
    {
        System.out.println("Load polygons");
        
        // check with user if it is fine to replace existing polygons
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
        
        // open a dialog to read a .json file
        File file = parentFrame.getGui().chooseFileToOpen(parentFrame,
                "Read list of input polygons", jsonFileFilter);
        if (file == null)
        {
            return;
        }
    
        // Check the chosen file is state
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
        ImageViewer viewer = imageFrame.getImageViewer();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }


    /**
     * Callback for the "Load Image Serial Section Node" menu item.
     */
    public void onLoadImageSerialSectionNode()
    {
        System.out.println("Load polygons from image serial sections node");
        
        // check with user if it is fine to replace existing polygons
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
        
        // open a dialog to read a .json file
        File file = parentFrame.getGui().chooseFileToOpen(parentFrame,
                "Read json file containing serial sections node", jsonFileFilter);
        if (file == null)
        {
            return;
        }
    
        // Check the chosen file is state
        if (!file.exists())
        {
            return;
        }
        
        try
        {
            crop3d.readPolygonsFromImageSerialSectionsNode(file);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        updatePolygonListView();
        
        // need to call this to update items to display 
        ImageViewer viewer = imageFrame.getImageViewer();
        viewer.refreshDisplay(); 
        viewer.repaint();
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
        String imageName = imageFrame.getImageHandle().getImage().getName();
        String defaultFileName = imageName + ".json";
        
        File file = parentFrame.getGui().chooseFileToSave(parentFrame, 
                "Save list of input polygons", defaultFileName, jsonFileFilter);
        if (file == null)
        {
            return;
        }

        // Check the chosen file is valid
        if (!file.getName().endsWith(".json"))
        {
            File parent = file.getParentFile();
            file = new File(parent, file.getName() + ".json");
        }
        
        try 
        {
            Crop3DDataWriter writer = new Crop3DDataWriter(file);
            writer.writePolygons(crop3d.currentRegion.polygons);
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
        File file = parentFrame.getGui().chooseFileToOpen(parentFrame, 
                "Choose Input 3D TIFF Image", ImageFileFilters.TIFF);

        // Check the chosen file is valid
        if (file == null)
        {
            return;
        }
        
        // clear current image data
        this.imageFrame = null;
        
        // create a viewer and a Crop3D object for the new image
        initializeFromImageFile(file);
    }
    
    /**
     * Reads a 3D virtual image from the specified file, and creates a new
     * ImageViewer and a new Crop3D object associated to the current frame.
     * 
     * @param imageFileName
     *            the image file to open.
     */
    public void initializeFromImageFile(File imageFileName)
    {
        createImageFrame(imageFileName);
        createDefaultCrop3D();
    }
    
    public void initialize(Crop3DData data)
    {
        File imageFile = new File(data.imageInfo.filePath);
        createImageFrame(imageFile);
        data.image = this.imageFrame.getImageHandle().getImage();
        
        // create the associated Crop3D 
        this.crop3d = new Crop3D(data, imageFrame.getImageHandle());
        this.crop3d.addAlgoListener(imageFrame);
        
        // choose the default region to display:
        // either the first non-empty one, or the first one if they are all empty
        Crop3DRegion refRegion = data.regions.get(0);
        for (Crop3DRegion region : data.regions())
        {
            if (!region.polygons.isEmpty())
            {
                refRegion = region;
                break;
            }
        }
        crop3d.selectCurrentRegion(refRegion.name);
        
        // enable widgets for regions management
        updateRegionWidgets();
        this.regionComboBox.setSelectedItem(refRegion.name);
        updatePolygonListView();
        
        // and updates the current frame
        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageViewer();
        viewer.refreshDisplay(); 
        viewer.repaint();
        viewer.setCurrentTool(new SelectPolygonTool(imageFrame, "selectPolygon"));
    }
    
    /**
     * Reads a 3D virtual image from the specified file, and creates a new
     * ImageViewer and a new Crop3D object associated to the current frame.
     * 
     * @param imageFileName
     *            the image file to open.
     */
    private void createImageFrame(File imageFileName)
    {
        // open a virtual image from the file
        Image image;
        try 
        {
            TiffImageReader reader = new TiffImageReader(imageFileName);
            image = reader.readVirtualImage3D();
        }
        catch (Exception ex) 
        {
            System.err.println(ex);
            ImagoGui.showErrorDialog(parentFrame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
            return;
        }
        
        // create viewer for the image
        this.imageFrame = ImageFrame.create(image, parentFrame);

        // update widgets
        imageNameLabel.setText(image.getName());
    }
    
    private void createDefaultCrop3D()
    {
        // create the associated Crop3D 
        this.crop3d = new Crop3D(imageFrame.getImageHandle());
        this.crop3d.addAlgoListener(imageFrame);
        this.crop3d.initializeDefaultRegions();
        
        // and updates the current frame
        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageViewer();
        viewer.refreshDisplay(); 
        viewer.repaint();
        viewer.setCurrentTool(new SelectPolygonTool(imageFrame, "selectPolygon"));
        
        // enable widgets for regions management
        updateRegionWidgets();
    }
    
    /**
     * Callback for the "Add Region" button.
     */
    public void onAddRegionButton()
    {
        System.out.println("add a new region");
        
        GenericDialog gd = new GenericDialog(jframe, "Choose Region Name");
        gd.addTextField("Name", "Region");
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        
        String regionName = gd.getNextString();
        
        // update the crop3D object
        this.crop3d.addRegion(regionName);
        this.crop3d.selectCurrentRegion(regionName);
        
        updateRegionWidgets();
        
        // use new region as current region
        this.regionComboBox.setSelectedIndex(this.regionComboBox.getItemCount() - 1);
    }
    
    /**
     * Callback for the "Remove Region" button.
     */
    public void onRemoveRegionButton()
    {
        System.out.println("remove current region");

        // retrieve name of the region to remove
        String regionName = (String) this.regionComboBox.getSelectedItem();
        if (regionName == null)
        {
            return;
        }
        
        // remove region from crop3D
        this.crop3d.removeRegion(regionName);
        
        // update widgets
        updateRegionWidgets();
        updatePolygonListView();
    }
    
    /**
     * Updates the different widgets related to region management, depending on
     * a region exists in current Crop3D.
     */
    private void updateRegionWidgets()
    {
        boolean hasRegions = this.crop3d.data.regions.size() > 0;
        
        addRegionButton.setEnabled(true);
        removeRegionButton.setEnabled(hasRegions);
        regionComboBox.setEnabled(hasRegions);
    
        // enable widgets for crop and process
        addPolygonButton.setEnabled(hasRegions);
        removePolygonButton.setEnabled(hasRegions);
        importPolygonsButton.setEnabled(hasRegions);
        exportPolygonsButton.setEnabled(hasRegions);
        interpolateButton.setEnabled(hasRegions);
    
        // update selection of current region
        regionComboBox.removeAllItems();
        for (Crop3DRegion region : this.crop3d.data.regions())
        {
            regionComboBox.addItem(region.name);
        }
    }

    /**
     * Callback for the Current Region ComboBox
     */
    public void onCurrentRegionUpdated()
    {
        System.out.println("update current region to \"" + this.regionComboBox.getSelectedItem() + "\"");
        
        // retrieve name of the region to remove
        String regionName = (String) this.regionComboBox.getSelectedItem();
        if (regionName == null)
        {
            return;
        }

        this.crop3d.selectCurrentRegion(regionName);
        
        updatePolygonListView();
        
        // need to call this to update items to display 
        ImageViewer viewer = imageFrame.getImageViewer();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    /**
     * Callback for the "Add Polygon" button.
     */
    public void onAddPolygonButton()
    {
        StackSliceViewer viewer = (StackSliceViewer) imageFrame.getImageViewer();

        int sliceIndex = viewer.getSlicingPosition(2);
        
        Geometry selection = viewer.getSelection();
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
        viewer.clearSelection();
        
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
        int index = cropItemList.getSelectedIndex();
        if (index == -1)
        {
            return;
        }
        
        String name = cropItemList.getSelectedValue();
        int sliceIndex = Integer.parseInt(name.substring(6).trim());

        
        ImageSerialSectionsNode group = crop3d.getPolygonsNode();
        group.removeSliceNode(sliceIndex);
        
        updatePolygonListView();
        
        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageViewer();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    /**
     * Updates the items shown in the polygon list according to the polygon node
     * of the ImageHandle.
     */
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
        
        this.cropItemList.setListData(strings);
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
                    crop3d.interpolatePolygons();
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
        ImageViewer viewer = imageFrame.getImageViewer();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    /**
     * Callback for the "Preview Crop Image" button.
     */
    public void onPreviewCropImageButton()
    {
        // Create new cropped image using virtual crop array
        Image cropImage = crop3d.createCropImageView();
        ImageFrame.create(cropImage, imageFrame);
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
        String defaultFileName = imageFrame.getImageHandle().getImage().getName() + ".crop3d";
        File file = parentFrame.getGui().chooseFileToSave(parentFrame, 
                "Select File for crop result", defaultFileName, mhdFileFilter);
        if (file == null)
        {
            return;
        }

        // Check the chosen file is valid
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
                    crop3d.saveCropImage(finalFile);
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
            int index = cropItemList.getSelectedIndex();
            if (index == -1)
            {
                return;
            }
            
            String name = cropItemList.getSelectedValue();
            int sliceIndex = Integer.parseInt(name.substring(6).trim());
             
            // need to call this to update items to display
            StackSliceViewer viewer = (StackSliceViewer) imageFrame.getImageViewer();
            viewer.setSlicingPosition(2, sliceIndex);
            
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
