/**
 * 
 */
package imago.transform;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import imago.app.ImagoApp;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImagoTextFrame;
import imago.transform.io.DelimitedFileAffineTransformReader;
import imago.transform.io.JsonTransformReader;
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
    // Static members
    
    private static FileFilter textFileFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
    private static FileFilter jsonFileFilter = new FileNameExtensionFilter("All JSON files (*.json)", "json");

    
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
        createMenuItem(fileMenu, "Import from JSON...", this::onImportTransformFromJsonFile);
        createMenuItem(fileMenu, "Import Affine from Coeffs...", this::onImportAffineTransformFromCoefficientsFile);
        fileMenu.addSeparator();
        createMenuItem(fileMenu, "Close", this::onClose);
        menuBar.add(fileMenu);
        
        JMenu editMenu = new JMenu("Edit");
        createMenuItem(editMenu, "Rename...", this::onRename);
        editMenu.addSeparator();
        createMenuItem(editMenu, "Remove", this::onRemove);
        editMenu.addSeparator();
        createMenuItem(editMenu, "Display Coefficients", this::onDisplayCoefficients);
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
    // Menu item callbacks
    
    private void onImportTransformFromJsonFile(ActionEvent evt)
    {
        // open a dialog to read a .json file
        File file = this.gui.chooseFileToOpen(this,
                "Import Transform coefficients file", jsonFileFilter);
        if (file == null)
        {
            return;
        }
        // Check the chosen file exists
        if (!file.exists())
        {
            return;
        }

        Transform transfo;
        try (JsonTransformReader reader = new JsonTransformReader(new FileReader(file));)
        {
            transfo = reader.readTransform();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        
        ImagoApp app = this.gui.getAppli();
        TransformHandle.create(app, transfo, file.getName());
        
        updateInfoTable();

    }
    
    private void onImportAffineTransformFromCoefficientsFile(ActionEvent evt)
    {
        // open a dialog to read a .json file
        File file = this.gui.chooseFileToOpen(this,
                "Import Transform coefficients file", textFileFilter);
        if (file == null)
        {
            return;
        }
        // Check the chosen file exists
        if (!file.exists())
        {
            return;
        }
        
        Transform transfo;
        try (DelimitedFileAffineTransformReader reader = new DelimitedFileAffineTransformReader(file))
        {
            transfo = reader.readTransform();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        
        ImagoApp app = this.gui.getAppli();
        TransformHandle.create(app, transfo, file.getName());
        
        updateInfoTable();
    }
    
    private void onClose(ActionEvent evt)
    {
        this.jFrame.setVisible(false);
    }
    
    private void onRename(ActionEvent evt)
    {
        TransformHandle handle = getSelectedHandle();
        if (handle == null) return;
        
        String newName = ImagoGui.showInputDialog(this, "Rename geometry", "Enter new name:", handle.getName());
        handle.setName(newName);
        
        updateInfoTable();
    }
    
    private void onRemove(ActionEvent evt)
    {
        ImagoApp appli = this.gui.getAppli();
        for (TransformHandle handle : getSelectedHandles())
        {
            appli.removeHandle(handle);
        }
        updateInfoTable();
    }
    
    /**
     * Displays the coefficient of the current affine transform, if selected.
     * 
     * @param evt
     *            the event object
     */
    private void onDisplayCoefficients(ActionEvent evt)
    {
        TransformHandle handle = getSelectedHandle();
        if (handle == null) return;
        
        ArrayList<String> textLines = new ArrayList<String>();
        textLines.add("Coefficients of transform: " + handle.getName() + " (id=" + handle.getTag() + ")");
        
        Transform transfo = handle.getTransform();
        String numberFormat = "%10.5f";
        if (transfo instanceof AffineTransform2D aff)
        {
            double[][] mat = aff.affineMatrix();
            String pattern = String.format("[%s, %s, %s]", numberFormat, numberFormat, numberFormat);
            for (int i = 0; i < 2; i++)
            {
                textLines.add(String.format(Locale.ENGLISH, pattern, mat[i][0], mat[i][1], mat[i][2]));
            }
            textLines.add(String.format(Locale.ENGLISH, pattern, 0.0, 0.0, 1.0));
        }
        else if (transfo instanceof AffineTransform3D aff)
        {
            double[][] mat = aff.affineMatrix();
            String pattern = String.format("[%s, %s, %s, %s]", numberFormat, numberFormat, numberFormat, numberFormat);
            for (int i = 0; i < 3; i++)
            {
                textLines.add(String.format(Locale.ENGLISH, pattern, mat[i][0], mat[i][1], mat[i][2], mat[i][3]));
            }
            textLines.add(String.format(Locale.ENGLISH, pattern, 0.0, 0.0, 0.0, 1.0));
        }
        else
        {
            ImagoGui.showErrorDialog(this, "Requires an affine transform as input", "Wrong Transform Type");
        }
        
        String title = String.format("%s - Transform Info", handle.getName());
        ImagoTextFrame newFrame = new ImagoTextFrame(this, title, textLines);
        newFrame.getWidget().pack();
        newFrame.getWidget().setSize(new Dimension(600, 400));
        newFrame.setVisible(true);
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
    
    private TransformHandle getSelectedHandle()
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
    
    private Collection<TransformHandle> getSelectedHandles()
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
