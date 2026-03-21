/**
 * 
 */
package imago.transform;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

import imago.app.ImagoApp;
import imago.gui.FrameMenuBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.geom.Transform;
import net.sci.geom.geom2d.AffineTransform2D;
import net.sci.geom.geom3d.AffineTransform3D;

/**
 * A frame, unique within the GUI, that allows to display and edit all the
 * geometric transforms managed within the workspace.
 * 
 * 
 * @see imago.shape.ShapeManager
 */
public class TransformManager extends ImagoFrame
{
    // ===================================================================
    // Static factories
    
    /**
     * Locally stores the mapping between GUI instances and shape managers.
     */
    private static final HashMap<ImagoGui, TransformManager> instances = new HashMap<ImagoGui, TransformManager>();
    
    /**
     * Returns the instance of TransformManager associated to this GUI. The instance
     * is created at first request (lazy initialization).
     * 
     * @param gui
     *            the ImagoGui instance of the running app.
     * @return the ShapeManager instance associated to the GUI.
     */
    public static final TransformManager getInstance(ImagoGui gui)
    {
        TransformManager manager = instances.get(gui);
        if (manager == null)
        {
            manager = new TransformManager(gui);
            instances.put(gui, manager);
        }
        return manager;
    }
    
    public static final boolean hasInstance(ImagoGui gui)
    {
        return instances.get(gui) != null;
    }
    
    
    // ===================================================================
    // Class members 
    
    /**
     * The model for data in the JTable
     */
    DefaultTableModel tableModel = new DefaultTableModel();

    /** The widget that displays the list of transforms within gui. */
    JTable infoTable;
    

    // ===================================================================
    // Constructors
    
    TransformManager(ImagoGui gui)
    {
        super(gui, "Transform Manager");
        
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
        
        putFrameMiddleScreen();
    }
    
    private void setupWidgets()
    {
        // initialize data model for the JTable
        Collection<TransformHandle> handles = TransformHandle.getAll(gui.getAppli());
        int nHandles = handles.size();
        String[] colNames = new String[] {"Id/tag", "Type", "Name"};
        this.tableModel = new DefaultTableModel(colNames, nHandles);
        refreshModel();
        
        // create JTable widget
        infoTable = new JTable(this.tableModel);
        infoTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        infoTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        infoTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        infoTable.getSelectionModel().addListSelectionListener(this::onTransformItemListChanged);
    }
    
    private void setupLayout() 
    {
        // put table into a scrollPanel
        JScrollPane scrollPane = new JScrollPane(infoTable);
        scrollPane.setPreferredSize(new Dimension(120, 120));
        infoTable.setFillsViewportHeight(true);
        
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
    // widgets callbacks
    
    /**
     * Called when a transform is selected within the view of transform items.
     * 
     * @param evt
     *            the event sent by the list widget
     */
    private void onTransformItemListChanged(ListSelectionEvent evt)
    {
    }
    
    public TransformHandle getSelectedHandle()
    {
        ListSelectionModel selection = infoTable.getSelectionModel();
        int[] inds = selection.getSelectedIndices();
        if (inds.length != 1)
        {
            ImagoGui.showErrorDialog(this, "Requires a single selection");
            return null;
        }
        
        String id = (String) this.tableModel.getValueAt(inds[0], 0);
        return (TransformHandle) this.gui.getAppli().getWorkspace().getHandle(id);
    }
    
    public Collection<TransformHandle> getSelectedHandles()
    {
        ListSelectionModel selection = infoTable.getSelectionModel();
        int[] indices = selection.getSelectedIndices();
        ArrayList<TransformHandle> handles = new ArrayList<TransformHandle>(indices.length);
        for (int index : indices)
        {
            String id = (String) this.tableModel.getValueAt(index, 0);
            handles.add((TransformHandle) this.gui.getAppli().getWorkspace().getHandle(id));
        }
        return handles;
    }
    
    
    // ===================================================================
    // general methods
    
    private void refreshModel()
    {
        Collection<TransformHandle> handles = TransformHandle.getAll(gui.getAppli());
        int nHandles = handles.size();
        this.tableModel.setRowCount(nHandles);
        
        int i = 0;
        for(TransformHandle handle : handles)
        {
            
            this.tableModel.setValueAt(handle.getTag(), i, 0);
            this.tableModel.setValueAt(getTransformType(handle.getTransform()), i, 1);
            this.tableModel.setValueAt(handle.getName(), i, 2);
            i++;
        }
    }
    
    /**
     * Chooses a generic name for the specified transform.
     * 
     * @param getransfoom
     *            an instance of Transform
     * @return a short name describing the Transform, or by default its class
     *         name.
     */
    private static String getTransformType(Transform transfo)
    {
        return switch (transfo)
        {
            case net.sci.geom.geom2d.AffineTransform2D t -> "Affine_2D";
            case net.sci.geom.geom3d.AffineTransform3D t -> "Affine_3D";
            default -> transfo.getClass().getSimpleName();
        };
    }
    
    
    // ===================================================================
    // Implementation of inner menu builder
    
    private class MenuBuilder extends FrameMenuBuilder
    {
        public MenuBuilder(ImagoFrame frame)
        {
            super(frame);
        }
        
        public void setupMenuBar()
        {
            JMenuBar menuBar = new JMenuBar();
            TransformManager tm = (TransformManager) frame;

            JMenu fileMenu = new JMenu("File");
            addPlugin(fileMenu, imago.transform.plugins.file.CreateTransform2D.class, "Create Transform...");
            fileMenu.addSeparator();
            addPlugin(fileMenu, imago.transform.plugins.file.ImportTransformFromJsonFile.class, "Import from JSON...");
            addPlugin(fileMenu, imago.transform.plugins.file.ImportAffineTransformFromCoefficientsFile.class, "Import Affine from Coeffs...");
            fileMenu.addSeparator();
            createMenuItem(fileMenu, "Close", evt -> tm.setVisible(false));
            menuBar.add(fileMenu);
            
            JMenu editMenu = new JMenu("Edit");
            addPlugin(editMenu, imago.transform.plugins.edit.RenameTransform.class, "Rename...");
            editMenu.addSeparator();
            addPlugin(editMenu, imago.transform.plugins.edit.RemoveTransform.class, "Remove");
            editMenu.addSeparator();
            addPlugin(editMenu, imago.transform.plugins.edit.DisplayCoefficients.class, "Display Coefficients");
            menuBar.add(editMenu);
            
            JMenu processMenu = new JMenu("Process");
            addPlugin(processMenu, imago.transform.plugins.process.DisplayTransformJacobian.class, "Display Jacobian determinant...");
            menuBar.add(processMenu);

            tm.getWidget().setJMenuBar(menuBar);
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
        System.out.println("Start TransformManager demo");
        
        ImagoApp app = new ImagoApp();
        ImagoGui gui = new ImagoGui(app);
        gui.showEmptyFrame(true);
        
        // populate app with some transforms
        AffineTransform2D aff01 = AffineTransform2D.createRotation(0.2);
        TransformHandle.create(app, aff01, "register_1_to_2");
        AffineTransform2D tr01 = AffineTransform2D.createTranslation(10, 20);
        TransformHandle.create(app, tr01, "init_transform");
        AffineTransform3D tr02 = AffineTransform3D.createTranslation(10, 20, 30);
        TransformHandle.create(app, tr02, "init3d");
        
        
        TransformManager tm = TransformManager.getInstance(gui);
        tm.setVisible(true);
    }
}
