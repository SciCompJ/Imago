/**
 * 
 */
package imago.image.plugins.register;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import imago.app.ImagoApp;
import imago.gui.FrameMenuBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import net.sci.image.Image;

/**
 * Displays a pair of images, by providing several options to change display.
 * 
 * @see ImageFrame
 */
public class SingleImageDisplayFrame extends ImagoFrame
{
    // ===================================================================
    // Class members
    
    public static final SingleImageDisplayFrame create(Image image, ImagoFrame parentFrame)
    {
        // retrieve gui, or create one if necessary
        ImagoGui gui = parentFrame != null ? parentFrame.getGui() : new ImagoGui(new ImagoApp());

        // Create the frame
        SingleImageDisplayFrame frame = new SingleImageDisplayFrame(gui, image);
        gui.updateFrameLocation(frame, parentFrame);
            
        // link the frames
        gui.addFrame(frame);
        if (parentFrame != null)
        {
            parentFrame.addChild(frame);
        }
        
        frame.setVisible(true);
        return frame;
    }
    
    
    // ===================================================================
    // Class members
    
    Image refImage;
    
    SingleImageViewer viewer;
    
    /**
     * The widget for displaying side-by-side the options panel and the image
     * display.
     */
    JSplitPane splitPane;
    

    // ===================================================================
    // Constructor

    public SingleImageDisplayFrame(ImagoGui gui, Image image) 
    {
        super(gui, "Image Pair");
        this.refImage = image;
        
        // Create the different panels
        createViewer();

        // layout the frame
        setupMenuBar();
        setupLayout();
        jFrame.doLayout();
        
//        updateTitle();
        
        // setup window listener
        this.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.jFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                gui.removeFrame(SingleImageDisplayFrame.this);
                SingleImageDisplayFrame.this.jFrame.dispose();
            }           
        });
        
        putFrameMiddleScreen();
        viewer.repaint();
    }
    
    private void createViewer()
    {
        // create the image viewer
        if (refImage.getDimension() == 2)
        {
            this.viewer = new SingleImageViewer(refImage);
        }
        else
        {
            throw new RuntimeException("Requires a 2D image");
        }
    }

    private void setupMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu viewMenu = new JMenu("View");
        FrameMenuBuilder.createMenuItem(viewMenu, "Zoom In", evt -> {
            viewer.setZoom(viewer.getZoom() * Math.sqrt(2.0));
            viewer.repaint();
        });
        FrameMenuBuilder.createMenuItem(viewMenu, "Zoom Out", evt -> {
            viewer.setZoom(viewer.getZoom() / Math.sqrt(2.0));
            viewer.repaint();
        });
        FrameMenuBuilder.createMenuItem(viewMenu, "Zoom 1:1", evt -> {
            viewer.setZoom(1.0);
            viewer.repaint();
        });
        menuBar.add(viewMenu);
        
        this.jFrame.setJMenuBar(menuBar);
    }
    
    private void setupLayout() 
    {
        // put into global layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add((JPanel) viewer.getWidget(), BorderLayout.CENTER);

        this.jFrame.setContentPane(mainPanel);
    }
    
    private void putFrameMiddleScreen()
    {
        // set up frame size depending on screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(800, screenSize.width - 100);
        int height = Math.min(600, screenSize.width - 100);
        Dimension frameSize = new Dimension(width, height);
        this.jFrame.setSize(frameSize);

        // set up frame position depending on frame size
        int posX = (screenSize.width - width) / 4;
        int posY = (screenSize.height - height) / 4;
        this.jFrame.setLocation(posX, posY);
    }
    
    
    // ===================================================================
    // Getter / Setter
    
    public SingleImageViewer getViewer()
    {
        return this.viewer;
    }
}
