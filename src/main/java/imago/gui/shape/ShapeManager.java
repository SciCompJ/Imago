/**
 * 
 */
package imago.gui.shape;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
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
import javax.swing.table.DefaultTableModel;

import imago.app.GeometryHandle;
import imago.app.ImagoApp;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.LineString2D;

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
        createMenuItem(fileMenu, "Close", this::onClose);
        menuBar.add(fileMenu);
        
        JMenu editMenu = new JMenu("Edit");
        createMenuItem(editMenu, "Rename...", this::onRename);
        createMenuItem(editMenu, "Compute Bounds", this::onComputeBounds);
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
