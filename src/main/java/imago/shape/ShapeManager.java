/**
 * 
 */
package imago.shape;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import imago.app.ImagoApp;
import imago.gui.FrameMenuBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.LineString2D;

/**
 * A frame, unique within the GUI, that allows to display and edit all the
 * geometries managed within the workspace.
 * 
 * The ShapeManager instance associated to a given ImagoFrame can be retrieved
 * as:
 * {@snippet lang = java :
 * ShapeManager manager = ShapeManager.getInstance(frame.getGui());
 * }
 * 
 * @see imago.shape.GeometryHandle
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
    
    public static final boolean hasInstance(ImagoGui gui)
    {
        return instances.get(gui) != null;
    }
    
    
    // ===================================================================
    // Static members
    
    public static FileFilter roisFileFilter = new FileNameExtensionFilter("Imago ROI list files (*.rois)", "rois");
    public static FileFilter jsonFileFilter = new FileNameExtensionFilter("All JSON files (*.json)", "json");
    public static FileFilter geomFileFilter = new FileNameExtensionFilter("JSON Single Geometry files (*.geom)", "geom");
    public static FileFilter ijroiFileFilter = new FileNameExtensionFilter("ImageJ ROI files (*.roi)", "roi");
    

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
        new MenuBuilder(this).setupMenuBar();
        setupWidgets();
        setupLayout();
         
        jFrame.doLayout();
        
        this.jFrame.pack();
        this.jFrame.setSize(new Dimension(400, 300));
        this.setVisible(true);
        
        // setup window listener
        this.jFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        gui.putFrameMiddleScreen(this);
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
    
    public JTable getJTable()
    {
        return this.infoTable;
    }
    
    
    // ===================================================================
    // Menu item callbacks
    
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
            this.tableModel.setValueAt(getGeometryType(handle.getGeometry()), i, 1);
            this.tableModel.setValueAt(handle.getName(), i, 2);
            i++;
        }
    }
    
    /**
     * Chooses a generic name for the specified geometry.
     * 
     * @param geom
     *            an instance of Geometry
     * @return a short name describing the geometry, or by default its class
     *         name.
     */
    private static String getGeometryType(Geometry geom)
    {
        return switch (geom)
        {
            case net.sci.geom.Point p -> "Point";
            case net.sci.geom.MultiPoint p -> "MultiPoint";
            case net.sci.geom.polygon2d.Polygon2D p -> "Polygon";
            case net.sci.geom.polygon2d.Polyline2D p -> "Polyline";
            case net.sci.geom.geom2d.curve.Circle2D c -> "Circle";
            case net.sci.geom.geom2d.curve.Ellipse2D elli -> "Ellipse";
            case net.sci.geom.geom2d.LineSegment2D seg -> "Segment";
            case net.sci.geom.geom2d.Curve2D c -> "Curve";
            case net.sci.geom.mesh3d.Mesh3D m -> "Mesh3D";
            default -> geom.getClass().getSimpleName();
        };
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
    
    public Collection<GeometryHandle> getAllHandles()
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
    
    public GeometryHandle getSelectedHandle()
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
    
    public Collection<GeometryHandle> getSelectedHandles()
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
    
    static class MenuBuilder extends FrameMenuBuilder
    {

        public MenuBuilder(ImagoFrame frame)
        {
            super(frame);
        }
        
        public void setupMenuBar()
        {
            JMenuBar menuBar = new JMenuBar();
            ShapeManager sm = (ShapeManager) frame;
                
            JMenu fileMenu = new JMenu("File");
            addPlugin(fileMenu, imago.shape.plugins.file.ImportRoiListFromJson.class, "Import Roi List...");
            addPlugin(fileMenu, imago.shape.plugins.file.ImportImagejRoi.class, "Import ImageJ ROI...");
            fileMenu.addSeparator();
            addPlugin(fileMenu, imago.shape.plugins.file.SaveRoiListAsJson.class, "Save Roi List...");
            addPlugin(fileMenu, imago.shape.plugins.file.SaveGeometryAsJson.class, "Save Single Geometry...");
            fileMenu.addSeparator();
            createMenuItem(fileMenu, "Close", sm::onClose);
            menuBar.add(fileMenu);
            
            JMenu editMenu = new JMenu("Edit");
            addPlugin(editMenu, imago.shape.plugins.edit.PrintGeometryInfo.class, "Display Info");
            addPlugin(editMenu, imago.shape.plugins.edit.RenameGeometry.class, "Rename...");
            editMenu.addSeparator();
            addPlugin(editMenu, imago.shape.plugins.edit.RemoveSelectedGeometries.class, "Remove");
            editMenu.addSeparator();
            addPlugin(editMenu, imago.shape.plugins.edit.SetAsImageSelection.class, "Set As Image Selection...");
            addPlugin(editMenu, imago.shape.plugins.edit.CopyToImageShapes.class, "Copy to Image Shapes...");
            addPlugin(editMenu, imago.shape.plugins.edit.CopyToImageShapeNode.class, "Copy to Image Shape Node...");
            menuBar.add(editMenu);

            JMenu processMenu = new JMenu("Process");
            addPlugin(processMenu, imago.shape.plugins.process.ComputeBounds.class, "Compute Bounds");
            processMenu.addSeparator();
            addPlugin(processMenu, imago.shape.plugins.process.SmoothMesh3D.class, "Smooth Mesh 3D");
            menuBar.add(processMenu);

            frame.getWidget().setJMenuBar(menuBar);

        }
        
        private static final void createMenuItem(JMenu menu, String label, ActionListener lst)
        {
            JMenuItem item = new JMenuItem(label);
            item.addActionListener(lst);
            menu.add(item);
        }
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
