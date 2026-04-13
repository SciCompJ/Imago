/**
 * 
 */
package imago.gui.frames;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Locale;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import imago.gui.FrameMenuBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.panels.AlgoMonitoringPanel;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;

/**
 * @author David Legland
 *
 */
public class ImagoEmptyFrame extends ImagoFrame implements AlgoListener
{
    AlgoMonitoringPanel algoMonitorPanel;
    
    // ===================================================================
    // Constructor

    public ImagoEmptyFrame(ImagoGui gui)
    {
        super(gui, "Imago");

        setupMenuBar();
        setupLayout();
        
        initializePosition();
    }

    private void setupMenuBar()
    {
        FrameMenuBuilder menuBuilder = new FrameMenuBuilder(this);
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        menuBuilder.addPlugin(fileMenu, imago.image.plugins.file.CreateNewImage.class, "New Image...");
        menuBuilder.addPlugin(fileMenu, imago.image.plugins.file.OpenImage.class, "Open...");
        // Import demo images
        JMenu demoMenu = new JMenu("Demo Images");
        menuBuilder.addPlugin(demoMenu, imago.image.plugins.file.OpenImage.class, "fileName=images/grains.png", "Rice grains");
        menuBuilder.addPlugin(demoMenu, imago.image.plugins.file.OpenImage.class, "fileName=images/peppers.png", "Peppers");
        menuBuilder.addPlugin(demoMenu, imago.image.plugins.file.OpenImage.class, "fileName=files/lena_gray_512.tif", "Lena");
        menuBuilder.addPlugin(demoMenu, imago.image.plugins.file.OpenImage.class, "fileName=images/sunflower.png", "Sunflower");
        menuBuilder.addPlugin(demoMenu, imago.image.plugins.file.OpenDemoStack.class, "Demo Stack");
        menuBuilder.addPlugin(demoMenu, imago.image.plugins.edit.CreateDistanceToOctahedronImage3D.class, "Octahedron Distance Map");
        menuBuilder.addPlugin(demoMenu, imago.image.plugins.edit.CreateColorCubeImage3D.class, "3D Color Cube");
        fileMenu.add(demoMenu);
        
        // Import less common file formats
        JMenu tiffFileMenu = new JMenu("Tiff Files");
        menuBuilder.addPlugin(tiffFileMenu, imago.image.plugins.file.ReadImageTiff.class, "Read TIFF...");
        menuBuilder.addPlugin(tiffFileMenu, imago.image.plugins.file.ReadTiffVirtualImage3D.class, "Read TIFF Virtual Image 3D...");
        menuBuilder.addPlugin(tiffFileMenu, imago.image.plugins.file.ReadTiffStackSlice.class, "Read TIFF Slice...");
        tiffFileMenu.addSeparator();
        menuBuilder.addPlugin(tiffFileMenu, imago.image.plugins.file.PrintImageFileTiffTags.class, "Print Tiff File Tags...");
        fileMenu.add(tiffFileMenu);
        
        // Import less common file formats
        JMenu fileImportMenu = new JMenu("Import");
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugins.file.ImportImageRawData.class, "Raw Data...");
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugins.file.ImportImageSeries.class, "Import Image Series...");
        fileImportMenu.addSeparator();
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugins.file.ImportImageMetaImage.class, "MetaImage Data...");
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugins.file.ShowMetaImageFileInfo.class, "Show MetaImage FileInfo...");
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugins.file.ImportImageVgi.class, "VGI Image...");
        fileMenu.add(fileImportMenu);
        
        fileMenu.addSeparator();
        menuBuilder.addPlugin(fileMenu, imago.table.plugins.file.OpenTable.class, "Open Table...");
        JMenu demoTables = new JMenu("Demo Tables");
        menuBuilder.addPlugin(demoTables, imago.table.plugins.file.OpenTable.class, "fileName=tables/fisherIris.csv", "Fisher's Iris");
        menuBuilder.addPlugin(demoTables, imago.table.plugins.file.OpenTable.class, "fileName=tables/penguins_clean.csv", "Penguins (without NA)");
        fileMenu.add(demoTables);
        
        fileMenu.addSeparator();
        menuBuilder.addPlugin(fileMenu, imago.gui.plugins.file.QuitApplication.class, "Quit");
        menuBar.add(fileMenu);
        
        menuBuilder.addSharedMenus(menuBar);
        
        this.jFrame.setJMenuBar(menuBar);
    }
    
    private void setupLayout()
    {
        // creates a pane for monitoring algorithms
        this.algoMonitorPanel = new AlgoMonitoringPanel();

        // put into global layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(this.algoMonitorPanel, BorderLayout.SOUTH);

        this.jFrame.setContentPane(mainPanel);
    }

    /**
     * Set up frame position on top-left of screen, with a position depending on
     * screen size.
     */
    private void initializePosition()
    {
        this.jFrame.setMinimumSize(new Dimension(300, 0));
        this.jFrame.pack();

        // set up frame position in the upper left corner
        int posX = 300;
        int posY = 50;
        this.jFrame.setLocation(posX, posY);
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
        this.algoMonitorPanel.setProgressPercent(progress);
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
        this.algoMonitorPanel.setStatusMessage(evt.getStatus());
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
        this.algoMonitorPanel.setStatusMessage(status);
        // also reset progress bar
        this.algoMonitorPanel.setProgressPercent(0);
    }
}
