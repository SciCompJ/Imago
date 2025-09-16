/**
 * 
 */
package imago.gui.shape;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import imago.app.GeometryHandle;
import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.app.shape.Shape;
import imago.app.shape.io.JsonGeometryReader;
import imago.app.shape.io.JsonGeometryWriter;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.util.imagej.ImagejRoi;
import imago.util.imagej.ImagejRoiDecoder;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.LineString2D;

/**
 * A frame, unique within the GUI, that allows to display and edit all the
 * geometries managed within the workspace.
 * 
 * @see imago.app.GeometryHandle
 * @see imago.app.Workspace
 * 
 * @author dlegland
 *
 */
public class ShapeManager extends ImagoFrame
{
    // ===================================================================
    // Static factories
    
    /**
     * Locally stores the mapping between GUI instances and shape managers.
     */
    private static final HashMap<ImagoGui, ShapeManager> instances = new HashMap<ImagoGui, ShapeManager>();
    
    /**
     * Returns the instance of ShapeManager associated to this GUI. The instance
     * is created at first request (lazy initialization).
     * 
     * @param gui
     *            the ImagoGui instance of the running app.
     * @return the ShapeManager instance associated to the GUI.
     */
    public static final ShapeManager getInstance(ImagoGui gui)
    {
        ShapeManager manager = instances.get(gui);
        if (manager == null)
        {
            manager = new ShapeManager(gui);
            instances.put(gui, manager);
        }
        return manager;
    }
    
    
    // ===================================================================
    // Static members
    
    private static FileFilter roisFileFilter = new FileNameExtensionFilter("Imago ROI list files (*.rois)", "rois");
    private static FileFilter jsonFileFilter = new FileNameExtensionFilter("All JSON files (*.json)", "json");
    private static FileFilter geomFileFilter = new FileNameExtensionFilter("JSON Single Geometry files (*.geom)", "geom");
    private static FileFilter ijroiFileFilter = new FileNameExtensionFilter("ImageJ ROI files (*.roi)", "roi");
    

    // ===================================================================
    // Class members 
    
    /**
     * The model for data in the JTable
     */
    DefaultTableModel tableModel = new DefaultTableModel();

    /** The widget that displays the list of geometries in app. */
    JTable infoTable;
    
    
    // ===================================================================
    // Constructors

    protected ShapeManager(ImagoGui gui)
    {
        super(gui, "Shape Manager");
        
        // layout the frame
        setupMenu();
        setupWidgets();
        setupLayout();
         
        jFrame.doLayout();
        
        this.jFrame.pack();
        this.jFrame.setSize(new Dimension(400, 300));
        this.setVisible(true);
        
        // setup window listener
        this.jFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        putFrameMiddleScreen();
    }
    
    private void setupMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        createMenuItem(fileMenu, "Import Roi List...", this::onImportRoiListFromJson);
        createMenuItem(fileMenu, "Import ImageJ ROI...", this::onImportImagejRoi);
        fileMenu.addSeparator();
        createMenuItem(fileMenu, "Save Roi List...", this::onSaveRoiListAsJson);
        createMenuItem(fileMenu, "Save Single Geometry...", this::onSaveGeometryAsJson);
        fileMenu.addSeparator();
        createMenuItem(fileMenu, "Close", this::onClose);
        menuBar.add(fileMenu);
        
        JMenu editMenu = new JMenu("Edit");
        createMenuItem(editMenu, "Rename...", this::onRename);
        createMenuItem(editMenu, "Compute Bounds", this::onComputeBounds);
        createMenuItem(editMenu, "Copy to Image Shapes", this::onCopyToImageShapes);
        editMenu.addSeparator();
        createMenuItem(editMenu, "Remove", this::onRemove);
        menuBar.add(editMenu);
        
        getWidget().setJMenuBar(menuBar);
    }

    private void createMenuItem(JMenu menu, String label, ActionListener lst)
    {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(lst);
        menu.add(item);
    }
    
    private void setupWidgets()
    {
        // initialize data model for the JTable
        Collection<GeometryHandle> handles = GeometryHandle.getAll(gui.getAppli());
        int nHandles = handles.size();
        String[] colNames = new String[] {"Id/tag", "Type", "Name"};
        this.tableModel = new DefaultTableModel(colNames, nHandles);
        refreshModel();
        
        // create JTable widget
        infoTable = new JTable(this.tableModel);
        infoTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        infoTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        infoTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        infoTable.getSelectionModel().addListSelectionListener(this::onGeomItemListChanged);
    }
    
    private void setupLayout() 
    {
        // put table into a scrollPanel
        JScrollPane scrollPane = new JScrollPane(infoTable);
        scrollPane.setPreferredSize(new Dimension(120, 120));
        infoTable.setFillsViewportHeight(true);
        
        // The panel for displaying info on selected geometry
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Geometry Info"));
        infoPanel.add(scrollPane, BorderLayout.CENTER);

        // put into global layout
        this.jFrame.setContentPane(scrollPane);
    }
        
    private void putFrameMiddleScreen()
    {
        // set up frame size depending on screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = this.jFrame.getWidth();
        int height = this.jFrame.getHeight();

        // set up frame position depending on frame size
        int posX = (screenSize.width - width) / 4;
        int posY = (screenSize.height - height) / 4;
        this.jFrame.setLocation(posX, posY);
    }
    
    
    // ===================================================================
    // Methods
    
    public void repaint()
    {
        updateInfoTable();
        super.repaint();
    }
    
    public void updateInfoTable()
    {
        refreshModel();
    }
    
    
    // ===================================================================
    // Menu item callbacks
    
    private void onImportImagejRoi(ActionEvent evt) 
    {
        // open a dialog to read a .json file
        File file = this.gui.chooseFileToOpen(this,
                "Import ImageJ Roi", ijroiFileFilter);
        if (file == null)
        {
            return;
        }
        // Check the chosen file exists
        if (!file.exists())
        {
            return;
        }
        
        // import ROI Geometry from file
        Geometry geom;
        try 
        {
            byte[] array = Files.readAllBytes(file.toPath());
            ImagejRoi roi = ImagejRoiDecoder.decode(array);
            geom = roi.asShape().getGeometry();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        // create handle
        ImagoApp app = this.gui.getAppli();
        GeometryHandle handle = GeometryHandle.create(app, geom);
        
        // setup metadata
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        handle.setName(file.getName());
        
        // update display
        updateInfoTable();
    }
    
    private void onImportRoiListFromJson(ActionEvent evt)
    {
        // open a dialog to read a .json file
        File file = this.gui.chooseFileToOpen(this,
                "Import Roi List", roisFileFilter, jsonFileFilter);
        if (file == null)
        {
            return;
        }
        // Check the chosen file exists
        if (!file.exists())
        {
            return;
        }
        
        ImagoApp app = this.gui.getAppli();
        
        try (FileReader fr = new FileReader(file.getAbsoluteFile());
                JsonGeometryReader reader = new JsonGeometryReader(new BufferedReader(fr)))
        {
            reader.beginObject();
            
            @SuppressWarnings("unused")
            String typeKey = reader.nextName();
            @SuppressWarnings("unused")
            String type = reader.nextString();
            
            @SuppressWarnings("unused")
            String itemListKey = reader.nextName();
            reader.beginArray();
            while(reader.hasNext())
            {
                reader.beginObject();
                @SuppressWarnings("unused")
                String nameKey = reader.nextName();
                String name = reader.nextString();
                
                reader.nextName();
                Geometry geom = reader.readGeometry();
                
                GeometryHandle handle = GeometryHandle.create(app, geom);
                handle.setName(name);
                
                reader.endObject();
            }
            reader.endArray();
            
            reader.endObject();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        updateInfoTable();
    }
    
    private void onSaveRoiListAsJson(ActionEvent evt)
    {
        // open a dialog to read a .json file
        String defaultFileName = "data.rois";
        File file = this.gui.chooseFileToSave(this,
                "Save Roi List", defaultFileName, roisFileFilter, jsonFileFilter);
        if (file == null)
        {
            return;
        }
    
        try 
        {
            // create a json Geometry writer from the file
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            JsonGeometryWriter writer = new JsonGeometryWriter(new PrintWriter(fileWriter));
            // configure JSON
            writer.setIndent("  ");
            
            // write ROI List top-lvel node
            writer.beginObject();
            writer.name("type");
            writer.value("RoiList");
            writer.name("items");
            writer.beginArray();

            // choose selected geometries, or all geometries
            boolean hasSelection = infoTable.getSelectionModel().getSelectedItemsCount() > 0;
            Collection<GeometryHandle> handles = hasSelection ? getSelectedHandles() : getAllHandles();
            
            // iterate over geometries
            for (GeometryHandle handle : handles)
            {
                writer.beginObject();
                writer.name("name");
                writer.value(handle.getName());
                writer.name("geometry");
                writer.writeGeometry(handle.getGeometry());
                writer.endObject();
            }
            
            writer.endArray();
            writer.endObject();
            writer.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        updateInfoTable();
    }
    
    private void onSaveGeometryAsJson(ActionEvent evt)
    {
        // open a dialog to read a .json file
        String defaultFileName = "data.geom";
        File file = this.gui.chooseFileToSave(this,
                "Save to JSON file", defaultFileName, geomFileFilter, jsonFileFilter);
        if (file == null)
        {
            return;
        }
    
        GeometryHandle handle = getSelectedHandle();
        
        try 
        {
            // create a json Geometry writer from the file
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            JsonGeometryWriter writer = new JsonGeometryWriter(new PrintWriter(fileWriter));
            // configure JSON
            writer.setIndent("  ");
            
            writer.writeGeometry(handle.getGeometry());
            writer.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        updateInfoTable();
    }
    

    private void onClose(ActionEvent evt)
    {
        this.jFrame.setVisible(false);
    }
    
    private void refreshModel()
    {
        Collection<GeometryHandle> handles = GeometryHandle.getAll(gui.getAppli());
        int nHandles = handles.size();
        this.tableModel.setRowCount(nHandles);
        
        int i = 0;
        for(GeometryHandle handle : handles)
        {
            
            this.tableModel.setValueAt(handle.getTag(), i, 0);
            this.tableModel.setValueAt(handle.getGeometry().getClass().getSimpleName(), i, 1);
            this.tableModel.setValueAt(handle.getName(), i, 2);
            i++;
        }
    }
    
    private void onRename(ActionEvent evt)
    {
        GeometryHandle handle = getSelectedHandle();
        if (handle == null) return;
        
        String newName = ImagoGui.showInputDialog(this, "Rename geometry", "Enter new name:", handle.getName());
        handle.setName(newName);
        
        updateInfoTable();
    }
    
    private void onCopyToImageShapes(ActionEvent evt)
    {
        ImagoApp appli = this.gui.getAppli();
        
        GenericDialog dlg = new GenericDialog(this, "Move To Image");
        String[] imageNames = ImageHandle.getAllNames(appli).toArray(new String[]{});
        dlg.addChoice("Image to Update", imageNames, imageNames[0]);
        dlg.showDialog();
        
        if (dlg.wasCanceled()) 
        {
            return;
        }
        
        // Parse dialog options
        String imageName = dlg.getNextChoice();
        ImageHandle imageHandle = ImageHandle.findFromName(appli, imageName);
        
        for (GeometryHandle handle : getSelectedHandles())
        {
            Shape shape = new Shape(handle.getGeometry());
            imageHandle.addShape(shape);
        }
        
        imageHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
    }
    
    private void onRemove(ActionEvent evt)
    {
        ImagoApp appli = this.gui.getAppli();
        for (GeometryHandle handle : getSelectedHandles())
        {
            appli.removeHandle(handle);
        }
        updateInfoTable();
    }
    
    private void onComputeBounds(ActionEvent evt)
    {
        GeometryHandle handle = getSelectedHandle();
        if (handle == null) return;
        
        // check type
        if (!(handle.getGeometry() instanceof Geometry2D))
        {
            ImagoGui.showErrorDialog(this, "Requires a 2D geometry");
            return;
        }
        
        // create a new geometry
        Geometry2D geom = (Geometry2D) handle.getGeometry();
        Geometry2D bounds = geom.bounds().getRectangle();
        
        // add new geometry to appli
        GeometryHandle newHandle = GeometryHandle.create(this.gui.getAppli(), bounds, handle);
        newHandle.setName(handle.getName() + "-Bounds");
        
        // refresh display
        updateInfoTable();
    }
    
    /**
     * Called when a geometry is selected within the view of geometry items.
     * 
     * @param evt
     *            the event sent by the list widget
     */
    private void onGeomItemListChanged(ListSelectionEvent evt)
    {
    }
    
    private Collection<GeometryHandle> getAllHandles()
    {
        int nGeom = infoTable.getModel().getRowCount();
        ArrayList<GeometryHandle> handles = new ArrayList<GeometryHandle>(nGeom);
        for (int index = 0; index < nGeom; index++)
        {
            String id = (String) this.tableModel.getValueAt(index, 0);
            handles.add((GeometryHandle) this.gui.getAppli().getWorkspace().getHandle(id));
        }
        return handles;
    }
    
    private GeometryHandle getSelectedHandle()
    {
        ListSelectionModel selection = infoTable.getSelectionModel();
        int[] inds = selection.getSelectedIndices();
        if (inds.length != 1)
        {
            ImagoGui.showErrorDialog(this, "Requires a single selection");
            return null;
        }
        
        String id = (String) this.tableModel.getValueAt(inds[0], 0);
        return (GeometryHandle) this.gui.getAppli().getWorkspace().getHandle(id);
    }
    
    private Collection<GeometryHandle> getSelectedHandles()
    {
        ListSelectionModel selection = infoTable.getSelectionModel();
        int[] indices = selection.getSelectedIndices();
        ArrayList<GeometryHandle> handles = new ArrayList<GeometryHandle>(indices.length);
        for (int index : indices)
        {
            String id = (String) this.tableModel.getValueAt(index, 0);
            handles.add((GeometryHandle) this.gui.getAppli().getWorkspace().getHandle(id));
        }
        return handles;
    }
    
    
    // ===================================================================
    // main
    
    /**
     * Static method used for debugging purpose.
     * 
     * @param args
     *            optional arguments not used in practice.
     */
    public static final void main(String... args)
    {
        System.out.println("Start ShapeManager demo");
        
        ImagoApp app = new ImagoApp();
        ImagoGui gui = new ImagoGui(app);
        gui.showEmptyFrame(true);
        
        // populate app with some geometries
        Point2D p1 = new Point2D(3, 2);
        LineSegment2D seg1 = new LineSegment2D(new Point2D(10,30), new Point2D(50,40));
        LineString2D poly1 = LineString2D.create(2);
        poly1.addVertex(new Point2D(10, 10));
        poly1.addVertex(new Point2D(20, 20));
        poly1.addVertex(new Point2D(30, 10));
        GeometryHandle h1 = GeometryHandle.create(app, p1);
        h1.setName("Point 1");
        GeometryHandle h2 = GeometryHandle.create(app, seg1);
        h2.setName("line segment");
        GeometryHandle h3 = GeometryHandle.create(app, poly1);
        h3.setName("outline");
        
        ShapeManager sm = ShapeManager.getInstance(gui);
        sm.setVisible(true);
    }
}
