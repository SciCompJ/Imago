/**
 * 
 */
package imago.gui.frames;


import java.awt.Dimension;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import imago.gui.FrameMenuBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;

/**
 * @author David Legland
 *
 */
public class ImagoEmptyFrame extends ImagoFrame
{
    // ===================================================================
    // Constructor

    public ImagoEmptyFrame(ImagoGui gui)
    {
        super(gui, "Imago");

        setupMenuBar();

        initializePosition();
    }

    private void setupMenuBar()
    {
        FrameMenuBuilder menuBuilder = new FrameMenuBuilder(this);
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        menuBuilder.addPlugin(fileMenu, imago.image.plugin.file.CreateNewImage.class, "New Image...");
        menuBuilder.addPlugin(fileMenu, imago.image.plugin.file.OpenImage.class, "Open...");
        // Import demo images
        JMenu demoMenu = new JMenu("Demo Images");
        menuBuilder.addPlugin(demoMenu, imago.image.plugin.file.OpenImage.class, "fileName=images/grains.png", "Rice grains");
        menuBuilder.addPlugin(demoMenu, imago.image.plugin.file.OpenImage.class, "fileName=images/peppers.png", "Peppers");
        menuBuilder.addPlugin(demoMenu, imago.image.plugin.file.OpenImage.class, "fileName=files/lena_gray_512.tif", "Lena");
        menuBuilder.addPlugin(demoMenu, imago.image.plugin.file.OpenImage.class, "fileName=images/sunflower.png", "Sunflower");
        menuBuilder.addPlugin(demoMenu, imago.image.plugin.file.OpenDemoStack.class, "Demo Stack");
        menuBuilder.addPlugin(demoMenu, imago.image.plugin.edit.CreateDistanceToOctahedronImage3D.class, "Octahedron Distance Map");
        menuBuilder.addPlugin(demoMenu, imago.image.plugin.edit.CreateColorCubeImage3D.class, "3D Color Cube");
        fileMenu.add(demoMenu);
        
        // Import less common file formats
        JMenu tiffFileMenu = new JMenu("Tiff Files");
        menuBuilder.addPlugin(tiffFileMenu, imago.image.plugin.file.ReadImageTiff.class, "Read TIFF...");
        menuBuilder.addPlugin(tiffFileMenu, imago.image.plugin.file.ReadTiffVirtualImage3D.class, "Read TIFF Virtual Image 3D...");
        menuBuilder.addPlugin(tiffFileMenu, imago.image.plugin.file.ReadTiffStackSlice.class, "Read TIFF Slice...");
        tiffFileMenu.addSeparator();
        menuBuilder.addPlugin(tiffFileMenu, imago.image.plugin.file.PrintImageFileTiffTags.class, "Print Tiff File Tags...");
        fileMenu.add(tiffFileMenu);
        
        // Import less common file formats
        JMenu fileImportMenu = new JMenu("Import");
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugin.file.ImportImageRawData.class, "Raw Data...");
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugin.file.ImportImageSeries.class, "Import Image Series...");
        fileImportMenu.addSeparator();
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugin.file.ImportImageMetaImage.class, "MetaImage Data...");
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugin.file.ShowMetaImageFileInfo.class, "Show MetaImage FileInfo...");
        menuBuilder.addPlugin(fileImportMenu, imago.image.plugin.file.ImportImageVgi.class, "VGI Image...");
        fileMenu.add(fileImportMenu);
        
        fileMenu.addSeparator();
        menuBuilder.addPlugin(fileMenu, imago.table.plugin.OpenTable.class, "Open Table...");
        JMenu demoTables = new JMenu("Demo Tables");
        menuBuilder.addPlugin(demoTables, imago.table.plugin.OpenTable.class, "fileName=tables/fisherIris.csv", "Fisher's Iris");
        menuBuilder.addPlugin(demoTables, imago.table.plugin.OpenTable.class, "fileName=tables/penguins_clean.csv", "Penguins (without NA)");
        fileMenu.add(demoTables);
        
        fileMenu.addSeparator();
        menuBuilder.addPlugin(fileMenu, imago.gui.plugin.file.QuitApplication.class, "Quit");
        menuBar.add(fileMenu);
        
        menuBuilder.addSharedMenus(menuBar);
        
        this.jFrame.setJMenuBar(menuBar);
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
}
