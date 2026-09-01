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
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import imago.app.ImagoApp;
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
    
//    /** The panel containing display options.*/ 
//    JPanel displayOptionsPanel;
    
//    /** Used to display information about image, cursor, current process... */
//    StatusBar statusBar;
    
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
//        this.displayOptionsPanel = imagePairViewer.createOptionsPanel();
//        this.displayOptionsPanel.setSize(this.displayOptionsPanel.getPreferredSize());
        
//        this.statusBar = new StatusBar();

        // layout the frame
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
    }
    
    private void createViewer()
    {
        // create the image viewer
        if (refImage.getDimension() == 2)
        {
            this.viewer = new SingleImageViewer(refImage);

//            ImagoTool cursorDisplay = new DisplayCurrentValueTool(this, "showValue");
//            viewer.getImageDisplay().addMouseListener(cursorDisplay);
//            viewer.getImageDisplay().addMouseMotionListener(cursorDisplay);
        }
        else
        {
            throw new RuntimeException("Requires a 2D image");
        }
    }

    private void setupLayout() 
    {
//        this.displayOptionsPanel.setPreferredSize(new Dimension(0, 0));
//        this.displayOptionsPanel.setMinimumSize(new Dimension(0, 0));
        
        // put into global layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add((JPanel) viewer.getWidget(), BorderLayout.CENTER);
//        mainPanel.add(this.statusBar, BorderLayout.SOUTH);
        
//        // setup the layout for the option panel:
//        // uses JSplitPanel, initial visibility depends on image dimensionality
//        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, displayOptionsPanel, mainPanel);
//        splitPane.setResizeWeight(0.4);
//        splitPane.setOneTouchExpandable(true);
//        splitPane.setContinuousLayout(true);

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
