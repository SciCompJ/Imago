/**
 * 
 */
package imago.image.viewers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import net.sci.array.Array;
import net.sci.array.color.ColorMap;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.TriMesh3D;
import net.sci.geom.mesh3d.process.Smooth;
import net.sci.image.Image;
import net.sci.image.region.BinaryRegion3D;
import net.sci.image.region.process.LabelMapToRegions3D;
import net.sci.image.region.process.Region3DIsosurface;

/**
 * A frame that renders a collection of meshes, each mesh corresponding to a
 * region within a 3D label map image.
 */
public class LabelMapViewer3D extends ImagoFrame 
{
    public static final LabelMapViewer3D create(Image labelMapImage, ImagoFrame parentFrame)
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
        LabelMapViewer3D frame = new LabelMapViewer3D(gui, labelMapImage);
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
    
    RenderPanel renderPanel;
    JSlider headingSlider;
    JSlider pitchSlider;
    JSlider zoomSlider;
    
    /** Used to display information about mesh, cursor, current process... */
    StatusBar statusBar;

    public LabelMapViewer3D(ImagoGui gui, Image labelMapImage)
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
        
        // convert 3D label map image to a map of binary regions 
        LabelMapToRegions3D regionBuilder = new LabelMapToRegions3D();
        Map<Integer, BinaryRegion3D> regions = regionBuilder.process(labelMapImage.getData());
        
        // convert each binary regions into a 3D mesh 
        Map<Integer, TriMesh3D> meshes = new HashMap<Integer, TriMesh3D>(); 
        Region3DIsosurface mesher = new Region3DIsosurface();
        Smooth smooth = new Smooth();
        for (int label : regions.keySet())
        {
            TriMesh3D mesh = mesher.process(regions.get(label));
            Mesh3D smoothed = smooth.process(smooth.process(mesh));
            meshes.put(label, TriMesh3D.convert(smoothed));
        }
        this.renderPanel.meshes = meshes;
        
        // compute the color associated to each mesh
        ColorMap colorMap = labelMapImage.getDisplaySettings().getColorMap();
        Map<Integer, Color> meshColors = new HashMap<Integer, Color>();
        for (int label : regions.keySet())
        {
            Color color = convertColor(colorMap.getColor(label - 1 % 255));
            meshColors.put(label, color);
        }
        this.renderPanel.meshColors = meshColors;
        
        // look at the center of the image
        Array<?> array = labelMapImage.getData();
        Point3D imageCenter = Point3D.of(array.size(0) * 0.5, array.size(1) * 0.5, array.size(2) * 0.5);
        getRenderer().setLookAt(imageCenter);
        
        this.jFrame.setSize(400, 400);
    }
    
    private static final Color convertColor(net.sci.array.color.Color color)
    {
        return new Color((float) color.red(), (float) color.green(), (float) color.blue());
    }
    
    private void initializeWidgets()
    {
        renderPanel = new RenderPanel();
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
        fmb.addPlugin(fileMenu, imago.gui.plugins.file.CloseCurrentFrame.class, "Close");
        menuBar.add(fileMenu);
        
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

    private static class RenderPanel extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        ZBufferRenderer renderer;
        
        Map<Integer, TriMesh3D> meshes = null;
        Map<Integer, Color> meshColors = null;
        
        public RenderPanel()
        {
            this.meshes = new HashMap<Integer, TriMesh3D>();
            this.meshColors = new HashMap<Integer, Color>();
            renderer = new ZBufferRenderer(100, 100);
        }
        
        public void paintComponent(Graphics g)
        {
            renderer.reset();
            if (!meshes.isEmpty())
            {
                for (int label : meshes.keySet())
                {
                    TriMesh3D mesh = meshes.get(label);
                    Color color = meshColors.getOrDefault(label, Color.WHITE);
                    renderer.renderMesh(mesh, color);    
                }
            }
            
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(renderer.getImage(), 0, 0, null);
        }
    }   
}
