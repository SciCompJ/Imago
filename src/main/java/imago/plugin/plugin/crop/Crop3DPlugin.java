/**
 * 
 */
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
import javax.swing.ProgressMonitor;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.app.ImagoApp;
import imago.app.scene.ImageSerialSectionsNode;
import imago.gui.FramePlugin;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoEmptyFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.viewer.StackSliceViewer;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;

/**
 * Start the Crop3D plugin. 
 * 
 * Opens a dialog to load a 3D image, initializes the associated data structure, 
 * and opens the dialog to setup parameters.
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
    JMenuItem loadPolygonsItem;
    JMenuItem savePolygonsItem;
    
    JLabel imageNameLabel;
    
    // buttons
    JButton openImageButton;
    JButton addPolygonButton;
    JButton removePolygonButton;
    JButton interpolateButton;
    JButton cropImageButton;
    
    // the widget that displays the list of base polygons
    JList<String> roiList;
    
    private JFileChooser openWindow = null;
    private JFileChooser saveWindow = null;

    
    public Crop3DPlugin()
    {
        System.out.println("create the crop3D plugin");
    }
    
    @Override
    public void run(ImagoFrame parentFrame, String args)
    {
        System.out.println("start Crop3D plugin");
        this.parentFrame = parentFrame;
//        
//        this.imageFrame = null;
//        chooseInputImage(parentFrame);
//        if (this.imageFrame == null)
//        {
//            return;
//        }
//        
//        this.crop3d = new Crop3D(imageFrame);
//        this.crop3d.addAlgoListener(imageFrame);
//        
//        this.crop3d.initializeCrop3dNodes();
//
//        // need to call this to update items to display
//        ImageViewer viewer = imageFrame.getImageView();
//        viewer.refreshDisplay(); 
//        viewer.repaint();
//        
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
        // init menu items
        loadPolygonsItem = new JMenuItem("Load Polygons...");
        loadPolygonsItem.addActionListener(evt -> loadPolygons());
        savePolygonsItem = new JMenuItem("Save Polygons...");
        savePolygonsItem.addActionListener(evt -> savePolygons());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(loadPolygonsItem);
        fileMenu.addSeparator();
        fileMenu.add(savePolygonsItem);
        
        
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
        openImageButton.addActionListener(evt -> openImage());
        addPolygonButton = new JButton("Add Polygon");
        addPolygonButton.addActionListener(evt -> addPolygon());
        addPolygonButton.setEnabled(false);
        removePolygonButton = new JButton("Remove Polygon");
        removePolygonButton.addActionListener(evt -> removePolygon());
        removePolygonButton.setEnabled(false);
        //removePolygonButton.setEnabled(false);
        interpolateButton = new JButton("Interpolate");
        interpolateButton.addActionListener(evt -> interpolatePolygons());
        interpolateButton.setEnabled(false);
        cropImageButton = new JButton("Crop Image...");
        cropImageButton.addActionListener(evt -> cropImage());
        cropImageButton.setEnabled(false);
        
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel btnPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        btnPanel.add(new JLabel(" "));
        btnPanel.add(openImageButton);
        btnPanel.add(addPolygonButton);
        btnPanel.add(removePolygonButton);
        btnPanel.add(interpolateButton);        
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
    
    public void loadPolygons()
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
    
    public void savePolygons()
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
            crop3d.savePolygonsAsJson(file);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    
    public void openImage()
    {
        this.imageFrame = null;
        chooseInputImage(parentFrame);
        if (this.imageFrame == null)
        {
            return;
        }
        
        this.crop3d = new Crop3D(imageFrame);
        this.crop3d.addAlgoListener(imageFrame);
        
        this.crop3d.initializeCrop3dNodes();

        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
        
        imageNameLabel.setText("Current Image: " + this.imageFrame.getImageHandle().getImage().getName());
        
        addPolygonButton.setEnabled(true);
        removePolygonButton.setEnabled(true);
        interpolateButton.setEnabled(true);
        cropImageButton.setEnabled(true);

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

    public void addPolygon()
    {
        ImageViewer viewer = imageFrame.getImageView();

        StackSliceViewer piv = (StackSliceViewer) viewer;
        int sliceIndex = piv.getSliceIndex();
        
        Geometry2D selection = piv.getSelection();
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
     * Removes the polygon identified by the line selected in the polygon list.
     */
    public void removePolygon()
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
    
    private void updatePolygonListView()
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
    
    public void interpolatePolygons()
    {
        crop3d.interpolatePolygons();

        // need to call this to update items to display
        ImageViewer viewer = imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
    }
    
    public void cropImage()
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
        ProgressMonitor progressMonitor = new ProgressMonitor(this.jframe, "Compute 3D Crop", "", 0, 100);
        progressMonitor.setMillisToDecideToPopup(10);
        progressMonitor.setMillisToPopup(100);
        progressMonitor.setProgress(0);

        AlgoListener progressDisplay = new AlgoListener()
        {
            @Override
            public void algoProgressChanged(AlgoEvent evt)
            {
                int progress = (int) Math.round(evt.getProgressRatio() * 100);
                progressMonitor.setProgress(progress);
            }

            @Override
            public void algoStatusChanged(AlgoEvent evt)
            {
                progressMonitor.setNote(evt.getStatus());
            }
        };
        this.crop3d.addAlgoListener(progressDisplay);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try 
                {
                    crop3d.computeCroppedImage(finalFile);
                    progressMonitor.setProgress(100);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace(System.err);
                    ImagoGui.showExceptionDialog(imageFrame, ex, "Crop 3D Error");
                }
                finally
                {
                    crop3d.removeAlgoListener(progressDisplay);
                }

            }
        };
        
        t.start();
        
        
    }
    

    
    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting() == false)
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

    public static final void main(String... args)
    {
        System.out.println("run...");
        ImagoFrame parentFrame = new ImagoEmptyFrame(new ImagoGui(new ImagoApp()));
        
        Crop3DPlugin plugin = new Crop3DPlugin();
        
        plugin.run(parentFrame, null);
    }
}