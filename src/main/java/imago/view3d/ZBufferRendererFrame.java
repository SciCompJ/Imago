/**
 * 
 */
package imago.view3d;

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

import imago.app.ImagoApp;
import imago.gui.FrameMenuBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.SimpleTriMesh3D;
import net.sci.geom.mesh3d.TriMesh3D;

/**
 * 
 */
public class ZBufferRendererFrame extends ImagoFrame 
{
    ZBufferPanel renderPanel;
    JSlider headingSlider;
    JSlider pitchSlider;
    JSlider zoomSlider;
    
    
    public ZBufferRendererFrame(ImagoGui gui)
    {
        super(gui, "ZBuffer Renderer Frame");
        
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
        
        this.jFrame.setSize(600, 600);
        this.jFrame.setVisible(true);
    }
    
    private void initializeWidgets()
    {
        renderPanel = new ZBufferPanel();
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
        fmb.addPlugin(fileMenu, imago.view3d.plugins.file.ImportMeshFromOffFile.class, "Read OFF Mesh...");
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

    static class ZBufferPanel extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        ZBufferRenderer renderer;
        
        TriMesh3D mesh = null;
        
        public ZBufferPanel()
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
    
    public ZBufferRenderer getRenderer()
    {
        return this.renderPanel.renderer;
    }
    
    public void setMesh(TriMesh3D mesh)
    {
        renderPanel.mesh = mesh;
    }
    
    public static void main(String[] args)
    {
        System.out.println("hi!");
        ImagoGui gui = new ImagoGui(new ImagoApp());
        
        ZBufferRendererFrame frame = new ZBufferRendererFrame(gui);
        frame.renderPanel.mesh = createTetrahedron();
    }
    
    private static TriMesh3D createTetrahedron()
    {
        SimpleTriMesh3D mesh = new SimpleTriMesh3D();
        Mesh3D.Vertex v1 = mesh.addVertex(new Point3D(100, 100, 100));
        Mesh3D.Vertex v2 = mesh.addVertex(new Point3D(-100, -100, 100));
        Mesh3D.Vertex v3 = mesh.addVertex(new Point3D(-100, 100, -100));
        Mesh3D.Vertex v4 = mesh.addVertex(new Point3D(100, -100, -100));
        mesh.addFace(v1, v2, v3);
        mesh.addFace(v1, v2, v4);
        mesh.addFace(v3, v4, v1);
        mesh.addFace(v3, v4, v2);

        return mesh;
    }
}
