/**
 * 
 */
package imago.gui.imagepair;

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
import imago.gui.image.ImageFrame;
import net.sci.image.Image;

/**
 * Displays a pair of images, by providing several options to change display.
 * 
 * @see ImageFrame
 */
public class ImagePairFrame extends ImagoFrame
{
    // ===================================================================
    // Class members
    
    public static final ImagePairFrame create(Image refImage, Image otherImage, ImagoFrame parentFrame)
    {
        // retrieve gui, or create one if necessary
        ImagoGui gui = parentFrame != null ? parentFrame.getGui() : new ImagoGui(new ImagoApp());

        // Create the frame
        ImagePairFrame frame = new ImagePairFrame(gui, refImage, otherImage);
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
    
    Image otherImage;
    
    
    /** The viewer for the pair of images. */
    CompositeImagePairViewer imagePairViewer;
    
    /** The panel containing display options.*/ 
    JPanel displayOptionsPanel;
    
//    /** Used to display information about image, cursor, current process... */
//    StatusBar statusBar;
    
    /**
     * The widget for displaying side-by-side the options panel and the image
     * display.
     */
    JSplitPane splitPane;
    

    // ===================================================================
    // Constructor

    public ImagePairFrame(ImagoGui gui, Image refImage, Image img2) 
    {
        super(gui, "Image Pair");
        this.refImage = refImage;
        this.otherImage = img2;
        
//        // create menu
//        GuiBuilder builder = new GuiBuilder(this);
//        builder.createMenuBar();
        
        // Create the different panels
        createViewer();
        this.displayOptionsPanel = imagePairViewer.createOptionsPanel();
        this.displayOptionsPanel.setSize(this.displayOptionsPanel.getPreferredSize());
        
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
                gui.removeFrame(ImagePairFrame.this);
                ImagePairFrame.this.jFrame.dispose();
            }           
        });
        
        putFrameMiddleScreen();
    }
    
    private void createViewer()
    {
        // create the image viewer
        if (refImage.getDimension() == 2)
        {
            CompositeImagePairViewer viewer = new CompositeImagePairViewer(refImage, otherImage);

//            ImagoTool cursorDisplay = new DisplayCurrentValueTool(this, "showValue");
//            viewer.getImageDisplay().addMouseListener(cursorDisplay);
//            viewer.getImageDisplay().addMouseMotionListener(cursorDisplay);

            this.imagePairViewer = viewer;
        }
        else
        {
            throw new RuntimeException("Requires a 2D image");
        }
    }

    private void setupLayout() 
    {
        this.displayOptionsPanel.setPreferredSize(new Dimension(0, 0));
        this.displayOptionsPanel.setMinimumSize(new Dimension(0, 0));
        
        // put into global layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add((JPanel) imagePairViewer.getWidget(), BorderLayout.CENTER);
//        mainPanel.add(this.statusBar, BorderLayout.SOUTH);
        
        // setup the layout for the option panel:
        // uses JSplitPanel, initial visibility depends on image dimensionality
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, displayOptionsPanel, mainPanel);
        splitPane.setResizeWeight(0.4);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        this.jFrame.setContentPane(splitPane);
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
}
