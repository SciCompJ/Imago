/**
 * 
 */
package imago.mesh3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import imago.gui.FrameMenuBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.panels.StatusBar;
import imago.view3d.ZBufferRenderer;
import net.sci.algo.AlgoEvent;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.TriMesh3D;

/**
 * A frame the renders a single mesh, and provides a number of operators for
 * transforming this mesh.
 */
public class Mesh3DViewer extends ImagoFrame 
{
    public static final Mesh3DViewer create(TriMesh3D mesh, ImagoFrame parentFrame)
    {
        // retrieve gui, or create one if necessary
        ImagoGui gui = ImagoGui.of(parentFrame);
//        ImagoGui gui = parentFrame != null ? parentFrame.getGui() : new ImagoGui(new ImagoApp());
//        ImagoApp app = gui.getAppli();
        
//        // create a handle for the image
//        ImageHandle parentHandle = null;
//        if (parentFrame != null && parentFrame instanceof ImageFrame)
//        {
//            parentHandle = ((ImageFrame) parentFrame).getImageHandle();
//        }
//        ImageHandle handle = ImageHandle.create(app, image, parentHandle);

        // Create the frame
        Mesh3DViewer frame = new Mesh3DViewer(gui, mesh);
        gui.updateFrameLocation(frame, parentFrame);
        
//        // if parent image has same type, duplicate the renderer of parent frame 
//        if (parentFrame != null && parentFrame instanceof ImageFrame)
//        {
//            ImageFrame parentImageFrame = ((ImageFrame) parentFrame);
//            if (image.getType() == parentImageFrame.getImageHandle().getImage().getType())
//            {
//                frame.getImageViewer().setRenderer(parentImageFrame.getImageViewer().getRenderer().duplicate());
//            }
//        }
        
        // link the frames
        gui.addFrame(frame);
        if (parentFrame != null)
        {
            parentFrame.addChild(frame);
        }
        
        frame.setVisible(true);
        return frame;
    }
    
    MeshRenderPanel renderPanel;
    JSlider headingSlider;
    JSlider pitchSlider;
    JSlider zoomSlider;
    
    /** Used to display information about mesh, cursor, current process... */
    StatusBar statusBar;

    public Mesh3DViewer(ImagoGui gui, TriMesh3D mesh)
    {
        super(gui, "Mesh3D Viewer");
        
        setupMenuBar();
        initializeWidgets();
        setupLayout();
        
        // setup window listener
        this.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.jFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                close();
            }
        });
        
        setMesh(mesh);
        
        this.jFrame.setSize(400, 400);
    }
    
    private void initializeWidgets()
    {
        renderPanel = new MeshRenderPanel();
        renderPanel.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                renderPanel.renderer.resize(renderPanel.getWidth(), renderPanel.getHeight());
                renderPanel.renderer.updateTransform();
                renderPanel.repaint();
            }
        });

        // slider to control horizontal rotation
        headingSlider = new JSlider(-180, 180, 0);
        headingSlider.addChangeListener(e -> 
        {
            renderPanel.renderer.setHeading(headingSlider.getValue());
            renderPanel.renderer.updateTransform();
            renderPanel.repaint();
        });
        
        // slider to control vertical rotation
        pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pitchSlider.addChangeListener(e -> {
            renderPanel.renderer.setPitch(pitchSlider.getValue());
            renderPanel.renderer.updateTransform();
            renderPanel.repaint();
        });
        
        // slider to control vertical rotation
        zoomSlider = new JSlider(SwingConstants.VERTICAL, -100, 100, 0);
        zoomSlider.addChangeListener(e -> {
            renderPanel.renderer.setZoom(Math.pow(2, -zoomSlider.getValue() * 0.1));
            renderPanel.renderer.updateTransform();
            renderPanel.repaint();
        });
        
        this.statusBar = new StatusBar();
    }

    private void setupMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        FrameMenuBuilder fmb = new FrameMenuBuilder(this);
        
        JMenu fileMenu = new JMenu("File");
        fmb.addPlugin(fileMenu, imago.mesh3d.plugins.file.ImportMeshFromOffFile.class, "Read OFF Mesh...");
        fileMenu.addSeparator();
        fmb.addPlugin(fileMenu, imago.mesh3d.plugins.file.ExportMeshToOffFile.class, "Write OFF Mesh...");
        fileMenu.addSeparator();
        fmb.addPlugin(fileMenu, imago.mesh3d.plugins.file.CreateOctahedronMesh.class, "Create Octahedron");
        fileMenu.addSeparator();
        fmb.addPlugin(fileMenu, imago.gui.plugins.file.CloseCurrentFrame.class, "Close");
        menuBar.add(fileMenu);
        
        JMenu editMenu = new JMenu("Edit");
        fmb.addPlugin(editMenu, imago.mesh3d.plugins.edit.AddMeshToShapeManager.class, "Add Mesh To Shape Manager");
        menuBar.add(editMenu);
        
        JMenu processMenu = new JMenu("Process");
        fmb.addPlugin(processMenu, imago.mesh3d.plugins.process.SmoothMesh.class, "Smooth Mesh");
        menuBar.add(processMenu);
        
        fmb.addSharedMenus(menuBar);
        
        this.jFrame.setJMenuBar(menuBar);
    }
    
    private void setupLayout()
    {
        // put into global layout
        JPanel meshViewPanel = new JPanel(new BorderLayout());
        
        Container pane = this.jFrame.getContentPane();
        pane.setLayout(new BorderLayout());

        meshViewPanel.add(renderPanel, BorderLayout.CENTER);
        
        meshViewPanel.add(headingSlider, BorderLayout.SOUTH);
        meshViewPanel.add(pitchSlider, BorderLayout.EAST);
        meshViewPanel.add(zoomSlider, BorderLayout.WEST);
        
        // put into global layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(meshViewPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        this.jFrame.setContentPane(mainPanel);
    }
    
    public ZBufferRenderer getRenderer()
    {
        return this.renderPanel.renderer;
    }
    
    public StatusBar getStatusBar()
    {
        return this.statusBar;
    }
    
    public Mesh3D getMesh()
    {
        return renderPanel.mesh;
    }
    
    public void setMesh(TriMesh3D mesh)
    {
        renderPanel.mesh = mesh;
        
        String msg = "(no mesh)";
        if (mesh != null)
        {
            msg = String.format("%d vertices, %d faces", mesh.vertexCount(), mesh.faceCount());
        }
        this.statusBar.setToolLabel(msg);
    }
    
    /**
     * Completes the default implementation to display the progression in the
     * status bar.
     * 
     * @param evt
     *            the algorithm event
     */
    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
        super.algoProgressChanged(evt);
        int progress = (int) (evt.getProgressRatio() * 100);
        this.statusBar.setProgressBarPercent(progress);
    }

    /**
     * Completes the default implementation to display the algorithm status in
     * the status bar.
     * 
     * @param evt
     *            the algorithm event
     */
    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
        super.algoProgressChanged(evt);
        System.out.println("status: " + evt.getStatus());
        this.statusBar.setCurrentStepLabel(evt.getStatus());
    }
    
    @Override
    public void algoTerminated(String opName, double timeInMillis)
    {
        // compute number of processed elements per unit time
        double timeInSecs = timeInMillis / 1000.0;
       
        // format display
        String pattern = "%s: %.3f seconds";
        String status = String.format(Locale.ENGLISH, pattern, opName, timeInSecs);
        
        // display message
        System.out.println(status);
        this.statusBar.setCurrentStepLabel(status);
        // also reset progress bar
        this.statusBar.setProgressBarPercent(0);
    }

    private static class MeshRenderPanel extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        ZBufferRenderer renderer;
        
        TriMesh3D mesh = null;
        
        public MeshRenderPanel()
        {
            renderer = new ZBufferRenderer(100, 100);
        }
        
        public void paintComponent(Graphics g)
        {
            renderer.reset();
            if (mesh != null)
            {
                renderer.renderMesh(mesh, Color.WHITE);
            }
            
            Graphics2D g2 = (Graphics2D) g;

            g2.drawImage(renderer.getImage(), 0, 0, null);
        }
    }   
}
