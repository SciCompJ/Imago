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

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import imago.gui.FrameMenuBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.view3d.ZBufferRenderer;
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
    
    ZBufferPanel renderPanel;
    JSlider headingSlider;
    JSlider pitchSlider;
    JSlider zoomSlider;
    
    
    public Mesh3DViewer(ImagoGui gui, TriMesh3D mesh)
    {
        super(gui, "Mesh3D Viewer");
        
        setupMenuBar();
        initializeWidgets(mesh);
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
        
        this.jFrame.setSize(400, 400);
    }
    
    private void initializeWidgets(TriMesh3D mesh)
    {
        renderPanel = new ZBufferPanel(mesh);
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
    }

    private void setupMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        FrameMenuBuilder fmb = new FrameMenuBuilder(this);
        
        JMenu fileMenu = new JMenu("File");
        fmb.addPlugin(fileMenu, imago.mesh3d.plugins.file.ImportMeshFromOffFile.class, "Read OFF Mesh...");
        fileMenu.addSeparator();
        fmb.addPlugin(fileMenu, imago.gui.plugins.file.CloseCurrentFrame.class, "Close");
        menuBar.add(fileMenu);
        
        fmb.addSharedMenus(menuBar);
        
        this.jFrame.setJMenuBar(menuBar);
    }
    
    private void setupLayout()
    {
        Container pane = this.jFrame.getContentPane();
        pane.setLayout(new BorderLayout());

        pane.add(renderPanel, BorderLayout.CENTER);
        
        pane.add(headingSlider, BorderLayout.SOUTH);
        pane.add(pitchSlider, BorderLayout.EAST);
        pane.add(zoomSlider, BorderLayout.WEST);
    }
    
    public ZBufferRenderer getRenderer()
    {
        return this.renderPanel.renderer;
    }
    
    public void setMesh(TriMesh3D mesh)
    {
        System.out.println("update mesh");
        renderPanel.mesh = mesh;
    }
    
    private static class ZBufferPanel extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        ZBufferRenderer renderer;
        
        TriMesh3D mesh = null;
        
        public ZBufferPanel(TriMesh3D mesh)
        {
            this.mesh = mesh;
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
