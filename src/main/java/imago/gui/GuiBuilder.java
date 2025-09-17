/**
 * 
 */
package imago.gui;

import java.awt.Component;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import imago.app.ImageHandle;
import imago.gui.action.RunPluginAction;
import imago.gui.chart.ChartFrame;
import imago.gui.frames.ImagoEmptyFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.tools.DrawBrushValueTool;
import imago.gui.image.tools.DrawValueTool;
import imago.gui.image.tools.FloodFillTool;
import imago.gui.image.tools.SelectEllipseTool;
import imago.gui.image.tools.SelectLineSegmentTool;
import imago.gui.image.tools.SelectPolygonTool;
import imago.gui.image.tools.SelectPolylineTool;
import imago.gui.image.tools.SelectRectangleTool;
import imago.gui.image.tools.SelectionTool;
import imago.gui.table.TableFrame;
import imago.plugin.edit.ChangeCurrentTool;
import imago.plugin.image.ImageArrayOperatorPlugin;
import imago.plugin.image.edit.ImageSetColorMapFactory;
import imago.plugin.image.edit.ImageSetScaleFromLineSelection;
import imago.plugin.image.shape.ImageFlip;
import imago.plugin.table.OpenDemoTable;
import net.sci.array.Array;
import net.sci.array.ArrayOperator;
import net.sci.array.color.ColorMaps;
import net.sci.array.color.RGB8Array;
import net.sci.array.shape.Rotate90;
import net.sci.image.Image;
import net.sci.image.ImageOperator;
import net.sci.image.ImageType;
import net.sci.image.contrast.DynamicAdjustment;
import net.sci.image.contrast.ImageInverter;
/**
 * Setup the menu for a given frame.
 * 
 * @author David Legland
 *
 */
public class GuiBuilder
{
    /** 
     * The frame to setup.
     */
    ImagoFrame frame;
    
    boolean hasDoc = false;
    boolean hasImage = false;
    boolean hasImage2D = false;
    boolean hasImage3D = false;
    boolean hasScalarImage = false;
    boolean hasLabelImage = false;
    boolean hasBinaryImage = false;
    boolean hasVectorImage = false;
    boolean hasColorImage = false;
    boolean hasRGB8Image = false;
    
    Icon emptyIcon;
    
    /**
     * Creates a builder for the specified frame.
     * 
     * @param frame
     *            the frame to build.
     */
    public GuiBuilder(ImagoFrame frame)
    {
        this.frame = frame;
        this.emptyIcon = createEmptyIcon();
    }
    
    public void createMenuBar()
    {
        computeFlags();
        
        JMenuBar menuBar = new JMenuBar();
        if (frame instanceof ImageFrame)
        {
            menuBar.add(createFileMenu());
            menuBar.add(createImageEditMenu());
            menuBar.add(createImageMenu());
            menuBar.add(createImageProcessMenu());
            menuBar.add(createImageAnalyzeMenu());
            menuBar.add(createImageToolsMenu());
        }
        else if (frame instanceof TableFrame)
        {
            menuBar.add(createTableFileMenu());
            menuBar.add(createTableEditMenu());
            menuBar.add(createTablePlotMenu());
            menuBar.add(createTableProcessMenu());
        }
        else if (frame instanceof ChartFrame)
        {
            menuBar.add(createChartFileMenu());
        }
        else if (frame instanceof ImagoEmptyFrame)
        {
            menuBar.add(createFileMenu());
        }
        menuBar.add(createPluginsMenu());
        menuBar.add(createHelpMenu());
        
        frame.getWidget().setJMenuBar(menuBar);
    }
    
    private void computeFlags()
    {
        ImageHandle doc = null;
        if (frame instanceof ImageFrame)
        {
            doc = ((ImageFrame) frame).getImageHandle();
            
            this.hasDoc = doc != null;
            if (!hasDoc) 
                return;
            
            Image image = doc.getImage();
            this.hasImage = image != null;
            if (!hasImage) 
                return;

            Array<?> array = doc.getImage().getData();
            this.hasImage2D = array.dimensionality() == 2;
            this.hasImage3D = array.dimensionality() == 3;
            
            this.hasScalarImage = image.isScalarImage();
            this.hasLabelImage = image.isLabelImage();
            this.hasBinaryImage = image.isBinaryImage();
            this.hasVectorImage = image.isVectorImage();
            this.hasColorImage = image.isColorImage();
            this.hasRGB8Image = array instanceof RGB8Array;
        }
    }
    
    /**
     * Creates the sub-menu for the "File" item in the main menu bar. This menu
     * is common to the image frame and the empty frame.
     */
    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu("File");
        addPlugin(fileMenu, imago.plugin.image.file.CreateNewImage.class, "New Image...");
        addPlugin(fileMenu, imago.plugin.image.file.OpenImage.class, "Open...");
        // Import demo images
        JMenu demoMenu = new JMenu("Demo Images");
        addPlugin(demoMenu, imago.plugin.image.file.OpenImage.class, "fileName=images/grains.png", "Rice grains");
        addPlugin(demoMenu, imago.plugin.image.file.OpenImage.class, "fileName=images/peppers.png", "Peppers");
        addPlugin(demoMenu, imago.plugin.image.file.OpenImage.class, "fileName=files/lena_gray_512.tif", "Lena");
        addPlugin(demoMenu, imago.plugin.image.file.OpenImage.class, "fileName=images/sunflower.png", "Sunflower");
        addPlugin(demoMenu, imago.plugin.image.file.OpenDemoStack.class, "Demo Stack");
        addPlugin(demoMenu, imago.plugin.image.edit.CreateDistanceToOctahedronImage3D.class, "Octahedron Distance Map");
        addPlugin(demoMenu, imago.plugin.image.edit.CreateColorCubeImage3D.class, "3D Color Cube");
        fileMenu.add(demoMenu);
        
        // Import less common file formats
        JMenu tiffFileMenu = new JMenu("Tiff Files");
        addPlugin(tiffFileMenu, imago.plugin.image.file.ReadImageTiff.class, "Read TIFF...");
        addPlugin(tiffFileMenu, imago.plugin.image.file.ReadTiffVirtualImage3D.class, "Read TIFF Virtual Image 3D...");
        addPlugin(tiffFileMenu, imago.plugin.image.file.ReadTiffStackSlice.class, "Read TIFF Slice...");
        tiffFileMenu.addSeparator();
        addPlugin(tiffFileMenu, imago.plugin.image.file.PrintImageFileTiffTags.class, "Print Tiff File Tags...");
        fileMenu.add(tiffFileMenu);
        
        // Import less common file formats
        JMenu fileImportMenu = new JMenu("Import");
        addPlugin(fileImportMenu, imago.plugin.image.file.ImportImageRawData.class, "Raw Data...");
        addPlugin(fileImportMenu, imago.plugin.image.file.ImportImageSeries.class, "Import Image Series...");
        fileImportMenu.addSeparator();
        addPlugin(fileImportMenu, imago.plugin.image.file.ImportImageMetaImage.class, "MetaImage Data...");
        addPlugin(fileImportMenu, imago.plugin.image.file.ShowMetaImageFileInfo.class, "Show MetaImage FileInfo...");
        addPlugin(fileImportMenu, imago.plugin.image.file.ImportImageVgi.class, "VGI Image...");
        fileMenu.add(fileImportMenu);
        
        if (frame instanceof ImageFrame)
        {
            fileMenu.addSeparator();
            addPlugin(fileMenu, imago.plugin.image.file.SaveImageIO.class, "Save As...");
            addPlugin(fileMenu, imago.plugin.image.file.SaveImageAsTiff.class, "Save As Tiff...");
            addPlugin(fileMenu, imago.plugin.image.file.SaveImageMetaImage.class, "Save As MetaImage...");
        }
        
        fileMenu.addSeparator();
        addPlugin(fileMenu, imago.plugin.table.OpenTable.class, "Open Table...");
        JMenu demoTables = new JMenu("Demo Tables");
        addPlugin(demoTables, imago.plugin.table.OpenTable.class, "fileName=tables/fisherIris.csv", "Fisher's Iris");
        addPlugin(demoTables, imago.plugin.table.OpenTable.class, "fileName=tables/penguins_clean.csv", "Penguins (without NA)");
        fileMenu.add(demoTables);
        
        fileMenu.addSeparator();
        if (!(frame instanceof ImagoEmptyFrame))
        {
            addPlugin(fileMenu, imago.plugin.CloseCurrentFrame.class, "Close");
            addPlugin(fileMenu, imago.plugin.CloseWithChildren.class, "Close With Children");
        }
        addPlugin(fileMenu, imago.plugin.QuitApplication.class, "Quit");
        return fileMenu;
    }
    
    /**
     * Creates the sub-menu for the "Edit" item in the main menu bar.
     */
    private JMenu createImageEditMenu()
    {
        JMenu editMenu = new JMenu("Edit");
        
        // selection sub-menu
        JMenu selectionMenu = new JMenu("Selection");
        addPlugin(selectionMenu, imago.plugin.image.edit.ImageCopySelectionToWorkspace.class, "Copy To Workspace");
        addPlugin(selectionMenu, imago.plugin.image.edit.ImportSelectionFromWorkspace.class, "Import From Workspace");
        addPlugin(selectionMenu, imago.plugin.image.edit.ImageSelectionToSceneGraph.class, "Add Selection to Scene Graph");
        addPlugin(selectionMenu, imago.plugin.image.edit.ShowShapeManagerFrame.class, "Display Shape Manager");
        selectionMenu.addSeparator();
        addPlugin(selectionMenu, imago.plugin.image.edit.ImageClearSelection.class, "Clear Selection");
        editMenu.add(selectionMenu);

        // crop tools
        addPlugin(editMenu, imago.plugin.image.shape.ImageCropSelection.class, "Crop Selection", hasImage2D);
        addPlugin(editMenu, imago.plugin.image.shape.ImageCropDialog.class, "Crop...", hasImage);
        
        // zoom management
        editMenu.addSeparator();
        addPlugin(editMenu, imago.plugin.edit.ZoomIn.class, "Zoom In", hasImage);
        addPlugin(editMenu, imago.plugin.edit.ZoomOut.class, "Zoom Out", hasImage);
        addPlugin(editMenu, imago.plugin.edit.ZoomOne.class, "Zoom One", hasImage);
        addPlugin(editMenu, imago.plugin.image.edit.RefreshDisplay.class, "Refresh Display", hasImage);
        
        // add utility
        editMenu.addSeparator();
        JMenu sceneGraphMenu = new JMenu("Scene Graph");
        addPlugin(sceneGraphMenu, imago.plugin.image.edit.ShowSceneGraphTree.class, "Display Scene Graph Tree");
        addPlugin(sceneGraphMenu, imago.plugin.image.edit.PrintImageSceneGraph.class, "Print Scene Graph Tree");
        addPlugin(sceneGraphMenu, imago.plugin.image.edit.ToggleSceneGraphDisplay.class, "Toggle Scene Graph Display");
        editMenu.add(sceneGraphMenu);
        
        addPlugin(editMenu, imago.plugin.edit.DocClearShapes.class, "Clear Shapes");
        addPlugin(editMenu, imago.plugin.image.edit.AddPointShapeFromTable.class, "Add Point Shapes...");
        
        editMenu.addSeparator();
        JMenu settingsMenu = new JMenu("Settings");
        addPlugin(settingsMenu, imago.plugin.edit.ChooseFileDialogWidgetToolkit.class, "Choose File Dialog Widget Toolkits...");
        settingsMenu.addSeparator();
        addPlugin(settingsMenu, imago.plugin.edit.ChooseBrushValue.class, "Choose Brush Value...");
        addPlugin(settingsMenu, imago.plugin.edit.ChooseBrushRadius.class, "Choose Brush Radius...");
        
        editMenu.add(settingsMenu);
        return editMenu;
    }
    
    /**
     * Creates the sub-menu for the "Image" item in the main Menu bar.
     */
    private JMenu createImageMenu()
    {
        JMenu menu = new JMenu("Image");
        
        // First general info and calibration about images
        addPlugin(menu, imago.plugin.image.edit.RenameImage.class, "Rename...", hasImage);
        addPlugin(menu, imago.plugin.image.edit.ImageSetScale.class, "Image Scale...", hasImage);
        addPlugin(menu, imago.plugin.image.edit.PrintImageInfos.class, "Print Image Info", hasImage);
        addPlugin(menu, imago.plugin.image.edit.PrintImageTiffTags.class, "Show TIFF Tags", hasImage);

        // Management of image representation
        menu.addSeparator();
        JMenu displayRangeMenu = new JMenu("Display Range");
        addPlugin(displayRangeMenu, imago.plugin.image.edit.SetImageDisplayRangeToDataType.class, "Set Data Type Display Range", hasScalarImage);
        addPlugin(displayRangeMenu, imago.plugin.image.edit.SetImageDisplayRangeToData.class, "Set Image Display Range", hasScalarImage | hasVectorImage);
        addPlugin(displayRangeMenu, imago.plugin.image.edit.SetImageDisplayRange.class, "Set Manual Display Range", hasScalarImage | hasVectorImage);
        menu.add(displayRangeMenu);

        addArrayOperatorPlugin(menu, new DynamicAdjustment(.01), "Adjust Grayscale Dynamic", hasScalarImage);

        // add Colormap utils
        JMenu colormapMenu = new JMenu("Set Color Map");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.GRAY), "Gray");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.JET), "Jet");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.BLUE_GRAY_RED), "Blue-Gray-Red");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.RED), "Red");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.GREEN), "Green");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.BLUE), "Blue");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.FIRE), "Fire");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.GLASBEY), "Glasbey");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.GLASBEY_DARK), "Glasbey (Dark)");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.GLASBEY_BRIGHT), "Glasbey (Bright)");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.HSV), "HSV");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.BLUE_WHITE_RED), "Blue-White-Red");
        colormapMenu.addSeparator();
        addPlugin(colormapMenu, imago.plugin.image.edit.ImageColorMapDisplay.class, "Show Color Map in Table", hasImage && !hasRGB8Image);
        menu.add(colormapMenu);
        
        boolean hasDistanceImage = ((ImageFrame) frame).getImageHandle().getImage().getType() == ImageType.DISTANCE;
        addPlugin(menu, imago.plugin.image.edit.ImageSetBackgroundColor.class, "Set Background Color...", hasLabelImage || hasDistanceImage);
        addPlugin(menu, imago.plugin.image.edit.DisplayImagePair.class, "Display Image Pair", hasImage);

        // Several options for converting images
        menu.addSeparator();
        JMenu convertImageTypeMenu = new JMenu("Change Image Type");
        convertImageTypeMenu.setEnabled(hasImage);
        addPlugin(convertImageTypeMenu, imago.plugin.image.edit.SetImageType.class, "Set Image Display Type...", hasImage);
        addPlugin(convertImageTypeMenu, imago.plugin.image.edit.SetImageTypeToLabel.class, "Set Image Type to Label Image", hasScalarImage);
        convertImageTypeMenu.addSeparator();
        addPlugin(convertImageTypeMenu, imago.plugin.image.convert.ConvertImageToBinary.class, "Convert to Binary");
        addPlugin(convertImageTypeMenu, imago.plugin.image.convert.ConvertImageToLabel.class, "Convert to Label");
        menu.add(convertImageTypeMenu);
        
        JMenu convertDataTypeMenu = new JMenu("Convert Data Type");
        convertDataTypeMenu.setEnabled(hasImage);
        addPlugin(convertDataTypeMenu, imago.plugin.image.convert.ConvertImageToUInt8.class, "UInt8");
        addPlugin(convertDataTypeMenu, imago.plugin.image.convert.CreateScaledUInt8View.class, "UInt8 (adjust dynamic)", hasScalarImage);
        addPlugin(convertDataTypeMenu, imago.plugin.image.convert.ConvertImageToUInt16.class, "UInt16", hasScalarImage);
        convertDataTypeMenu.addSeparator();
        addPlugin(convertDataTypeMenu, imago.plugin.image.convert.ConvertImageToInt16.class, "Int16", hasScalarImage);
        addPlugin(convertDataTypeMenu, imago.plugin.image.convert.ConvertImageToInt32.class, "Int32", hasScalarImage);
        convertDataTypeMenu.addSeparator();
        addPlugin(convertDataTypeMenu, imago.plugin.image.convert.ConvertImageToFloat32.class, "Float32", hasImage);
        addPlugin(convertDataTypeMenu, imago.plugin.image.convert.ConvertImageToFloat64.class, "Float64", hasImage);
        menu.add(convertDataTypeMenu);
        
        // image dimensionality conversion plugins
        JMenu convertDimensionMenu = new JMenu("Convert Dimension(s)");
        convertDimensionMenu.setEnabled(hasImage);
        addPlugin(convertDimensionMenu, imago.plugin.image.convert.ConvertStackToMovie.class, "Convert Image 3D to movie", hasImage3D);
        addPlugin(convertDimensionMenu, imago.plugin.image.convert.ConvertScalarImageToVector.class, "Convert Scalar to Vector", hasImage3D);
        addPlugin(convertDimensionMenu, imago.plugin.image.convert.ConvertVectorImageToScalar.class, "Convert Vector To Scalar", hasVectorImage);
        menu.add(convertDimensionMenu);

        // Color images operators
        menu.addSeparator();
        JMenu colorMenu = new JMenu("Color");
        // editMenu.add(convertTypeMenu);
        addPlugin(colorMenu, imago.plugin.image.process.ImageSplitChannels.class, "Split Channels", hasVectorImage || hasColorImage);
        addPlugin(colorMenu, imago.plugin.image.process.MergeChannelImages.class, "Merge Channels");
        addPlugin(colorMenu, imago.plugin.image.convert.ConvertColorImageToGrayscale.class, "Convert To Grayscale", hasColorImage);
        addPlugin(colorMenu, imago.plugin.image.process.ColorImageExtractChannel.class, "Select Channel...", hasColorImage);
        addPlugin(colorMenu, imago.plugin.image.convert.ConvertUInt8ImageToRGB.class, "UInt8 to RGB8", hasScalarImage);
        addPlugin(colorMenu, imago.plugin.image.convert.ConvertLabelMapToRGB8.class, "Label Map to RGB", hasLabelImage);
        addPlugin(colorMenu, imago.plugin.image.convert.ConvertRGB8ImageToRGB16.class, "RGB8 to RGB16", hasRGB8Image);
        addPlugin(colorMenu, imago.plugin.image.convert.ScalarImagesColorDifference.class, "Color difference between two scalar images");
        menu.add(colorMenu);
        
        JMenu vectorMenu = new JMenu("Vector");
        addPlugin(vectorMenu, imago.plugin.image.process.VectorImageChannelView.class, "Channel View", hasVectorImage);
        addPlugin(vectorMenu, imago.plugin.image.convert.CreateVectorImageNorm.class, "Vector Image Norm", hasVectorImage);
        addPlugin(vectorMenu, imago.plugin.image.process.VectorImageAngle.class, "Vector Image Angle");
        addPlugin(vectorMenu, imago.plugin.image.convert.VectorImageConvertToRGB.class, "Convert to RGB", hasVectorImage);
        addPlugin(vectorMenu, imago.plugin.image.convert.CreateVectorImageRGB8View.class, "Create RGB View", hasVectorImage);
        addPlugin(vectorMenu, imago.plugin.image.edit.SetImageChannelNames.class, "Set Channel Names...", hasVectorImage || hasColorImage);
        addPlugin(vectorMenu, imago.plugin.image.convert.VectorImageToTable.class, "Convert To Table", hasVectorImage);
        menu.add(vectorMenu);

        // Change the geometry of image, and and extract slices
        menu.addSeparator();
        JMenu geometryMenu = new JMenu("Geometry");
        geometryMenu.setEnabled(hasImage);
        addPlugin(geometryMenu, new ImageFlip(0), "Horizontal Flip ");
        addPlugin(geometryMenu, new ImageFlip(1), "Vertical Flip");
        addPlugin(geometryMenu, new ImageFlip(2), "Z-Flip");
        geometryMenu.addSeparator();
        addArrayOperatorPlugin(geometryMenu, new Rotate90(-1), "Rotate Counter-Clockwise", hasImage2D);
        addArrayOperatorPlugin(geometryMenu, new Rotate90(+1), "Rotate Clockwise", hasImage2D);
        addPlugin(geometryMenu, imago.plugin.image.shape.ImageRotateAroundCenter.class, "Rotate...", hasImage2D);
        addPlugin(geometryMenu, imago.plugin.image.shape.Image3DRotate90.class, "Rotate 3D by 90 degrees...", hasImage3D);
        addPlugin(geometryMenu, imago.plugin.image.shape.ImageRotatedCrop.class, "Rotated Crop...", hasImage2D || hasImage3D);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, imago.plugin.image.shape.ImageReshape.class, "Reshape Image...", hasImage);
        addPlugin(geometryMenu, imago.plugin.image.shape.ImagePermuteDims.class, "Permute Image Dimensions...", hasImage);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, imago.plugin.image.shape.ImageConcatenate.class, "Concatenate..."); 
        addPlugin(geometryMenu, imago.plugin.image.shape.ImageAddBorders.class, "Add Borders...", hasScalarImage); 
        addPlugin(geometryMenu, imago.plugin.image.shape.ImageSubsample.class, "Subsample...", hasImage);
        addPlugin(geometryMenu, imago.plugin.image.shape.ImageDownsample.class, "Downsample...", hasImage);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, imago.plugin.image.process.ImageCropThumbnailList.class, "Crop Thumbnails from Positions...", hasImage);
        
        menu.add(geometryMenu);
        
        // Create the menu for 3D images
        JMenu stackMenu = new JMenu("3D Images");
        stackMenu.setEnabled(hasImage3D);
        addPlugin(stackMenu, imago.plugin.image.shape.Image3DGetCurrentSlice.class, "Extract Current Slice", hasImage3D);
        addPlugin(stackMenu, imago.plugin.image.shape.Image3DGetSlice.class, "Extract Slice...", hasImage3D);
        addPlugin(stackMenu, imago.plugin.image.shape.Image3DOrthoslicesMontage.class, "OrthoSlices Montage...", hasImage3D);
        addPlugin(stackMenu, imago.plugin.image.shape.Image3DSliceMontage.class, "Image 3D Montage...", hasImage3D);
        addPlugin(stackMenu, imago.plugin.image.shape.ImageOrthogonalProjection.class, "Orthogonal Projection...", hasImage3D && hasScalarImage);
        stackMenu.addSeparator();
        addPlugin(stackMenu, imago.plugin.image.process.Image3DSetOrthoSlicesDisplay.class, "Set Orthoslices Display", hasImage3D);
		menu.add(stackMenu);

        menu.addSeparator();
		addPlugin(menu, imago.plugin.image.process.ImageDuplicate.class, "Duplicate", hasImage);
		addArrayOperatorPlugin(menu, new ImageInverter(), "Invert", "%s-inv");
        
        // submenu for creation of phantoms
        JMenu phantomMenu = new JMenu("Phantoms");
        addPlugin(phantomMenu, imago.plugin.image.edit.ImageFillDisk.class, "Fill Disk...");
        addPlugin(phantomMenu, imago.plugin.image.edit.ImageFillEllipse.class, "Fill Ellipse...");
        addPlugin(phantomMenu, imago.plugin.image.edit.ImageFillBox.class, "Fill Box...");
        addPlugin(phantomMenu, imago.plugin.image.edit.ImageFillEllipsoid.class, "Fill Ellipsoid...");
        phantomMenu.addSeparator();
        addPlugin(phantomMenu, imago.plugin.image.edit.ImageSelectionToMask.class, "Selection To Mask");
        addPlugin(phantomMenu, imago.plugin.image.edit.ImageSelectionToDistanceMap.class, "Selection To Distance Map");
        menu.add(phantomMenu);
        
		return menu;
	}

	/**
	 * Creates the sub-menu for the "process" item in the main Menu bar.
	 */
	private JMenu createImageProcessMenu()
	{
		JMenu menu = new JMenu("Process");

		JMenu mathsMenu = new JMenu("Math");
        addPlugin(mathsMenu, imago.plugin.image.process.ImageApplyMathFunction.class, "Apply Function...", hasScalarImage);
        addPlugin(mathsMenu, imago.plugin.image.process.ImageApplySingleValueOperator.class, "Math operator (image+value)...");
        addPlugin(mathsMenu, imago.plugin.image.process.ImageApplyMathBinaryOperator.class, "Math operator (Image pair)...");
        mathsMenu.addSeparator();
        addPlugin(mathsMenu, imago.plugin.image.process.ImageApplyLogicalBinaryOperator.class, "Logical operator (Image pair)...");
		menu.add(mathsMenu);
		menu.addSeparator();

		// Noise reduction filters
        JMenu filtersMenu = new JMenu("Filters");
        addPlugin(filtersMenu, imago.plugin.image.process.ImageBoxFilter.class, "Box Filter...");
        addPlugin(filtersMenu, imago.plugin.image.process.BoxFilter3x3FloatPlugin.class, "Box Filter 2D 3x3 (float)", hasScalarImage);
        addArrayOperatorPlugin(filtersMenu, net.sci.image.filtering.GaussianFilter5x5.class, "Gaussian Filter 5x5", hasScalarImage && hasImage2D);
        addPlugin(filtersMenu, imago.plugin.image.process.ImageMedianFilterBox.class, "Median Filter...");
        addPlugin(filtersMenu, imago.plugin.image.binary.BinaryImageBoxMedianFilter.class, "Binary Median Filter...");
        addPlugin(filtersMenu, imago.plugin.image.process.ImageMinMaxFilterBox.class, "Min/Max Filter...");
        filtersMenu.addSeparator();
        addPlugin(filtersMenu, imago.plugin.image.process.ImageVarianceFilterBox.class, "Variance Filter...");
		menu.add(filtersMenu);
        
		// Gradient filters
        JMenu gradientFiltersMenu = new JMenu("Gradient Filters");
        addImageOperatorPlugin(gradientFiltersMenu, net.sci.image.filtering.SobelGradient.class, "Sobel Gradient", hasScalarImage);
        addImageOperatorPlugin(gradientFiltersMenu, net.sci.image.filtering.SobelGradientNorm.class, "Sobel Gradient Norm", hasScalarImage);
        addImageOperatorPlugin(gradientFiltersMenu, net.sci.image.contrast.VectorArrayNorm.class, "Vector Image Norm", hasVectorImage);
        menu.add(gradientFiltersMenu);
        
		JMenu morphologyMenu = new JMenu("Mathematical Morphology");
        addPlugin(morphologyMenu, imago.plugin.image.process.ImageMorphologicalFilter.class, "Morphological Filters...");
        addPlugin(morphologyMenu, imago.plugin.image.binary.BinaryImageMorphologicalFilter.class, "Binary Morphological Filters...");
        addPlugin(morphologyMenu, imago.plugin.image.binary.BinaryImageMorphologicalFilterBall.class, "Ball Binary Morphological Filters...");

		morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, imago.plugin.image.process.ImageRegionalExtrema.class, "Regional Min./Max...", hasScalarImage);
        addPlugin(morphologyMenu, imago.plugin.image.process.ImageExtendedExtrema.class, "Extended Min./Max...", hasScalarImage);
        addPlugin(morphologyMenu, imago.plugin.image.process.ImageImposeExtrema.class, "Impose Min./Max...", hasScalarImage);
        addPlugin(morphologyMenu, imago.plugin.image.process.ImageMorphologicalReconstruction.class, "Morphological Reconstruction...");
        addPlugin(morphologyMenu, imago.plugin.image.binary.BinaryImageMorphologicalReconstruction.class, "Binary Morphological Reconstruction...");
        addPlugin(morphologyMenu, imago.plugin.image.process.ImageIteratedGeodesicDilations.class, "Geodesic Dilation...");
		morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, imago.plugin.image.process.ImageFillHoles.class, "Fill Holes");
        addPlugin(morphologyMenu, imago.plugin.image.binary.BinaryImageFillHoles.class, "Binary Fill Holes");
        addPlugin(morphologyMenu, imago.plugin.image.process.ImageKillBorders.class, "Kill Borders");
        addPlugin(morphologyMenu, imago.plugin.image.binary.BinaryImageKillBorders.class, "Binary Kill Borders");
        menu.add(morphologyMenu);
		
        menu.addSeparator();
        addPlugin(menu, imago.plugin.image.process.ImagePair2DRegister.class, "Simple Image Registration", hasScalarImage);
        
        menu.addSeparator();
        addPlugin(menu, imago.plugin.image.vectorize.ImageFindNonZeroPixels.class, "Find Non-Zeros Elements", hasImage2D && hasScalarImage);
        addPlugin(menu, imago.plugin.image.vectorize.ImageIsocontour.class, "Isocontour...");
        addPlugin(menu, imago.plugin.image.vectorize.ExportIsosurface.class, "Export Isosurface...");
        addPlugin(menu, imago.plugin.image.vectorize.Image3DIsosurface.class, "Compute Isosurface...");
        addPlugin(menu, imago.plugin.image.process.Image3DKymograph.class, "Kymograph", hasImage3D && hasScalarImage);

        // operators specific to binary images
        menu.addSeparator();
        JMenu segmentationMenu = new JMenu("Segmentation");
        addPlugin(segmentationMenu, imago.plugin.image.process.ImageOtsuThreshold.class, "Otsu Auto Threshold", hasScalarImage);
        addPlugin(segmentationMenu, imago.plugin.image.process.ImageIsodataThreshold.class, "Isodata Auto Threshold", hasScalarImage);
        addPlugin(segmentationMenu, imago.plugin.image.process.ImageManualThreshold.class, "Manual Threshold", hasScalarImage);
        addPlugin(segmentationMenu, imago.plugin.image.process.ImageHysteresisThreshold.class, "Hysteresis Threshold", hasScalarImage);
        segmentationMenu.addSeparator();
        addPlugin(segmentationMenu, imago.plugin.image.process.ImageKMeansSegmentation.class, "K-Means Segmentation", hasImage);
        segmentationMenu.addSeparator();
        addPlugin(segmentationMenu, imago.plugin.image.process.ImageWatershed.class, "Watershed", hasScalarImage);
        addPlugin(segmentationMenu, imago.plugin.image.process.ImageMarkerControlledWatershed.class, "Marker-Based Watershed", hasScalarImage);
        menu.add(segmentationMenu);

        // operators specific to binary images
        menu.addSeparator();
		JMenu binaryMenu = new JMenu("Binary Images");
        addPlugin(binaryMenu, imago.plugin.image.binary.BinaryImageConnectedComponentsLabeling.class, "Connected Components Labeling");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, imago.plugin.image.binary.BinaryImageEuclideanDistanceMap.class, "Distance Map");
        addPlugin(binaryMenu, imago.plugin.image.binary.BinaryImageChamferDistanceMap.class, "Chamfer Distance Map");
        addPlugin(binaryMenu, imago.plugin.image.binary.BinaryImage3DDepthMap.class, "3D Binary Image Depth Map");
        addPlugin(binaryMenu, imago.plugin.image.binary.ImageGeodesicDistanceMap.class, "Geodesic Distance Map...");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, imago.plugin.image.binary.BinaryImageSkeleton.class, "IJ Skeleton");
        addPlugin(binaryMenu, imago.plugin.image.binary.BinaryImageSplitCoalescentParticles.class, "Split Particles...");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, imago.plugin.image.binary.BinaryImageOverlay.class, "Binary Overlay...");
        addPlugin(binaryMenu, imago.plugin.image.binary.ApplyBinaryMask.class, "Apply Binary Mask...");
        menu.add(binaryMenu);
        
        // operators specific to binary images
        JMenu labelMenu = new JMenu("Binary / Label Images");
        addPlugin(labelMenu, imago.plugin.image.process.LabelMapCropLabel.class, "Crop Label...", hasLabelImage);
        addPlugin(labelMenu, imago.plugin.image.process.LabelMapSizeOpening.class, "Size Opening...", hasLabelImage);
        labelMenu.addSeparator();
        addPlugin(labelMenu, imago.plugin.image.process.LabelMapSkeleton.class, "Skeleton (2D)");
        labelMenu.addSeparator();
        addPlugin(labelMenu, imago.plugin.image.vectorize.BinaryImageBoundaryGraph.class, "Boundary Graph", hasImage2D && hasBinaryImage);
        addPlugin(labelMenu, imago.plugin.image.vectorize.LabelMapToBoundaryPolygons.class, "Region Boundaries to Polygons", hasImage2D && hasLabelImage);
        menu.add(labelMenu);
		
		return menu;
	}

	/**
	 * Creates the sub-menu for the "process" item in the main Menu bar.
	 */
	private JMenu createImageAnalyzeMenu()
	{
		JMenu menu = new JMenu("Analyze");

        addPlugin(menu, imago.plugin.image.analyze.ImageHistogram.class, "Histogram", hasImage);
        addPlugin(menu, imago.plugin.image.analyze.ImageRoiHistogram.class, "ROI Histogram", hasImage && hasImage2D);
        addPlugin(menu, imago.plugin.image.analyze.ImageMeanValue.class, "Mean Value", hasImage);
        addPlugin(menu, imago.plugin.image.analyze.ColorImageBivariateHistograms.class, "Bivariate Color Histograms", hasColorImage);
        addPlugin(menu, imago.plugin.image.process.ImageBivariateHistogram.class, "Bivariate Histogram");
		menu.addSeparator();
        addPlugin(menu, imago.plugin.image.analyze.ImageLineProfile.class, "Line Profile", hasImage);
        addPlugin(menu, imago.plugin.image.analyze.ImageAnalyzeWithinROI.class, "Intensity within ROI", hasImage);
        addPlugin(menu, imago.plugin.image.analyze.ImagePlotChannels.class, "Channel Profile", hasImage);

        menu.addSeparator();
        JMenu regFeatMenu = new JMenu("Region Features");
        addPlugin(regFeatMenu, imago.plugin.image.analyze.RegionMorphology2D.class, "Regions Morphology", hasImage2D && hasLabelImage);
        menu.add(regFeatMenu);
        
        JMenu regions2dMenu = new JMenu("Regions (2D)");
        regions2dMenu.setEnabled(hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.plugin.image.analyze.LabelImageIntrinsicVolumes.class, "Regions Intrinsic Volumes", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.plugin.image.analyze.LabelImageEquivalentDisks.class, "Regions Equivalent Disks", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.plugin.image.analyze.LabelImageEquivalentEllipses.class, "Regions Equivalent Ellipses", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.plugin.image.analyze.LabelImageOrientedBoxes.class, "Regions Oriented Boxes", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.plugin.image.analyze.LabelImageConvexHulls.class, "Regions Conxex Hulls", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.plugin.image.analyze.LabelImageMaxFeretDiameters.class, "Regions Max. Feret Diameters", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.plugin.image.analyze.LabelImageGeodesicDiameters.class, "Regions Geodesic Diameters", hasImage2D && hasLabelImage);
        regions2dMenu.addSeparator();
        addPlugin(regions2dMenu, imago.plugin.image.analyze.LabelImageAdjacencies.class, "Regions Adjacencies", hasImage2D && hasLabelImage);
        menu.add(regions2dMenu);

        JMenu regions3dMenu = new JMenu("Regions (3D)");
        regions3dMenu.setEnabled(hasImage3D && hasLabelImage);
        addPlugin(regions3dMenu, imago.plugin.image.analyze.LabelImageEquivalentEllipsoids.class, "Regions Equivalent Ellipsoids", hasImage3D && hasLabelImage);
        menu.add(regions3dMenu);

        addPlugin(menu, imago.plugin.image.analyze.LabelImageBoundingBoxes.class, "Bounding Boxes", (hasImage2D || hasImage3D) && hasLabelImage);
        addPlugin(menu, imago.plugin.image.analyze.LabelImageCentroids.class, "Regions Centroids", (hasImage2D || hasImage3D) && hasLabelImage);

        menu.addSeparator();
        JMenu textureMenu = new JMenu("Texture Analysis");
        addPlugin(textureMenu, imago.plugin.image.analyze.GrayLevelImageCooccurenceMatrix.class, "Gray Level Co-Occurence Matrix", hasImage2D && hasScalarImage);
        addPlugin(textureMenu, imago.plugin.image.analyze.ImageGrayscaleGranulometry.class, "Grayscale granulometry", hasImage2D && hasScalarImage);
        menu.add(textureMenu);
        
        return menu;
    }
    
    /**
     * Creates the sub-menu for the "Tools" item in the main menu bar.
     */
    private JMenu createImageToolsMenu()
    {
        JMenu toolsMenu = new JMenu("Tools");
        
        // tool selection handles
        if (frame instanceof ImageFrame)
        {
            ImageFrame viewer = (ImageFrame) frame;
            
            addPlugin(toolsMenu, new ChangeCurrentTool(new SelectionTool(viewer, "select")), "Select", hasImage);
            addPlugin(toolsMenu,
                    new ChangeCurrentTool(new SelectLineSegmentTool(viewer, "selectLineSegment")), "Select Line",
                    hasImage);
            addPlugin(toolsMenu, new ChangeCurrentTool(new SelectRectangleTool(viewer, "selectRectangle")),
                    "Select Rectangle", hasImage);
            addPlugin(toolsMenu, new ChangeCurrentTool(new SelectEllipseTool(viewer, "selectEllipse")),
                    "Select Ellipse", hasImage);
            addPlugin(toolsMenu, new ChangeCurrentTool(new SelectPolygonTool(viewer, "selectPolygon")),
                    "Select Polygon", hasImage);
            addPlugin(toolsMenu, new ChangeCurrentTool(new SelectPolylineTool(viewer, "selectPolyline")),
                    "Select Polyline", hasImage);
            
            toolsMenu.addSeparator();
            addPlugin(toolsMenu, new ChangeCurrentTool(new DrawValueTool(viewer, "drawValue")), "Draw (Dot)",
                    hasScalarImage);
            addPlugin(toolsMenu, new ChangeCurrentTool(new DrawBrushValueTool(viewer, "drawBrushValue")),
                    "Draw (Brush)", hasScalarImage);
            addPlugin(toolsMenu, new ChangeCurrentTool(new FloodFillTool(viewer, "floodFillValue")), "Flood-Fill",
                    hasScalarImage);
            
            toolsMenu.addSeparator();
            addPlugin(toolsMenu, new ImageSetScaleFromLineSelection(), "Set Scale from Selection...", hasImage);
        }
        
        return toolsMenu;
    }
    
    /**
     * Creates the sub-menu for the "File" item in the main menu bar of Table frames.
     */
    private JMenu createTableFileMenu()
    {
        JMenu fileMenu = new JMenu("File");
        
        addPlugin(fileMenu, imago.plugin.table.OpenTable.class, "Open Table...");
        JMenu demoTables = new JMenu("Demo Tables");
        addPlugin(demoTables, new OpenDemoTable("tables/fisherIris.csv"), "Fisher's Iris");
        addPlugin(demoTables, new OpenDemoTable("tables/penguins_clean.csv"), "Penguins");
        fileMenu.add(demoTables);

        fileMenu.addSeparator();
        addPlugin(fileMenu, imago.plugin.table.SaveTable.class, "Save Table...");

        fileMenu.addSeparator();
        addPlugin(fileMenu, imago.plugin.CloseCurrentFrame.class, "Close", !(frame instanceof ImagoEmptyFrame));
        addPlugin(fileMenu, imago.plugin.QuitApplication.class, "Quit");

        return fileMenu;
    }
    
    /**
     * Creates the sub-menu for the "Edit" item in the main menu bar of Table
     * frames.
     */
    private JMenu createTableEditMenu()
    {
        JMenu editMenu = new JMenu("Edit");
        
        addPlugin(editMenu, imago.plugin.table.edit.RenameTable.class, "Rename...");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.plugin.table.edit.TableSelectColumns.class, "Select Columns...");
        addPlugin(editMenu, imago.plugin.table.edit.TableKeepNumericColumns.class, "Keep Numeric Columns");
        addPlugin(editMenu, imago.plugin.table.edit.ConcatenateTableColumns.class, "Concatenate Columns...");
        addPlugin(editMenu, imago.plugin.table.edit.TableParseGroupFromRowNames.class, "Parse Group From Row Names...");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.plugin.table.edit.TableFilterRows.class, "Filter/Select Rows...");
        addPlugin(editMenu, imago.plugin.table.edit.TableSortRows.class, "Sort Rows...");
        addPlugin(editMenu, imago.plugin.table.edit.ConcatenateTableRows.class, "Concatenate Rows...");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.plugin.table.edit.TransposeTable.class, "Transpose");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.plugin.table.edit.FoldTableToImage.class, "Fold Table to Image");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.plugin.table.edit.PrintTableInfo.class, "Print Table Info");
        addPlugin(editMenu, imago.plugin.table.edit.NumericTableSummary.class, "Table Summary");
        addPlugin(editMenu, imago.plugin.table.edit.PrintTableToConsole.class, "Print to Console");
        
        return editMenu;
    }

    /**
     * Creates the sub-menu for the "plot" item in the main menu bar of Table
     * frames.
     */
    private JMenu createTablePlotMenu()
    {
        JMenu plotMenu = new JMenu("Plot");
        addPlugin(plotMenu, imago.plugin.table.plot.TableScatterPlot.class, "Scatter Plot...");
        addPlugin(plotMenu, imago.plugin.table.plot.TableGroupScatterPlot.class, "Scatter Plot By Group...");
        addPlugin(plotMenu, imago.plugin.table.plot.TablePairPlot.class, "Pair Plot");
        plotMenu.addSeparator();
        addPlugin(plotMenu, imago.plugin.table.plot.TableLinePlot.class, "Line Plot...");
        plotMenu.addSeparator();
        addPlugin(plotMenu, imago.plugin.table.plot.PlotTableColumnHistogram.class, "Histogram...");
        return plotMenu;
    }
    
    /**
     * Creates the sub-menu for the "Process" item in the main menu bar of Table
     * frames.
     */
    private JMenu createTableProcessMenu()
    {
        JMenu processMenu = new JMenu("Process");
        
        addPlugin(processMenu, imago.plugin.table.process.AggregateTableWithColumn.class, "Aggregate by group...");
        processMenu.addSeparator();
        addPlugin(processMenu, imago.plugin.table.process.TablePca.class, "Principal Components Analysis");
        processMenu.addSeparator();
        addPlugin(processMenu, imago.plugin.table.process.TableKMeans.class, "K-Means...");
        processMenu.addSeparator();
        addPlugin(processMenu, imago.plugin.table.process.TableConfusionMatrix.class, "Confusion Matrix...");
        
        return processMenu;
    }

    /**
     * Creates the sub-menu for the "File" item in the main menu bar of Chart frames.
     */
    private JMenu createChartFileMenu()
    {
        JMenu fileMenu = new JMenu("File");
        
        addPlugin(fileMenu, imago.plugin.chart.SaveChart.class, "Save Chart...");

        fileMenu.addSeparator();
        addPlugin(fileMenu, imago.plugin.CloseCurrentFrame.class, "Close", !(frame instanceof ImagoEmptyFrame));
        return fileMenu;
    }
    
    /**
     * Creates the sub-menu for the "Plugins" item in the main menu bar, shared
     * by several frame types.
     */
    private JMenu createPluginsMenu()
    {
        JMenu menu = new JMenu("Plugins");
        
        JMenu devMenu = new JMenu("Developer");
        menu.add(devMenu);
        addPlugin(devMenu, imago.plugin.developer.DisplayExceptionDialog.class, "Show Demo Exception");
        // The two following plugins are used for debugging
//        addPlugin(devMenu, imago.plugin.developer.FailingConstructorPlugin.class, "(X) Can not Instantiate");
//        addPlugin(devMenu, imago.plugin.developer.RunThrowExceptionPlugin.class, "(X) Can not Run");
        devMenu.addSeparator();
        addPlugin(devMenu, imago.plugin.edit.PrintFrameList.class, "Print Frame List");
        addPlugin(devMenu, imago.plugin.edit.PrintDocumentList.class, "Print Document List");
        addPlugin(devMenu, imago.plugin.edit.PrintWorkspaceContent.class, "Print Workspace Content");
        
        // Add some domain-specific plugins, to be transformed into user plugins in the future
        menu.addSeparator();
        JMenu perigrainMenu = new JMenu("Perigrain");
        addPlugin(perigrainMenu, imago.plugin.plugin.crop.Crop3DPlugin.class, "Crop 3D");
        addPlugin(perigrainMenu, imago.plugin.plugin.crop.CreateSurface3DPlugin.class, "Surface 3D");
        addPlugin(perigrainMenu, imago.plugin.plugin.ImportImage3DPolylineSeries.class, "Import Polyline Series");
        menu.add(perigrainMenu);
        
        // Add the user plugins
        menu.addSeparator();
        for (PluginHandler handler : frame.gui.getPluginManager().pluginHandlers)
        {
            addPlugin(menu, handler);
        }
        
        return menu;
    }
    
    private JMenu createHelpMenu()
    {
        JMenu menu = new JMenu("Help");
        addMenuItem(menu, null, "About...", true);
        return menu;
    }
    
    private JMenuItem addMenuItem(JMenu menu, ImagoAction action, String label, boolean enabled)
    {
        JMenuItem item = new JMenuItem(action);
        item.setText(label);
        item.setIcon(this.emptyIcon);
        item.setEnabled(enabled);
        menu.add(item);
        return item;
    }
    
    private JMenuItem addImageOperatorPlugin(JMenu menu, Class<? extends ImageOperator> opClass, String label, boolean enabled)
    {
        FramePlugin plugin = frame.gui.getPluginManager().retrievePlugin(opClass);
        return addPlugin(menu, plugin, label, enabled);
    }

    private JMenuItem addArrayOperatorPlugin(JMenu menu, Class<? extends ArrayOperator> opClass, String label, boolean enabled)
    {
        FramePlugin plugin = frame.gui.getPluginManager().retrievePlugin(opClass);
        return addPlugin(menu, plugin, label, enabled);
    }

    private JMenuItem addArrayOperatorPlugin(JMenu menu, ArrayOperator operator, String label, boolean enabled)
    {
        FramePlugin plugin = new ImageArrayOperatorPlugin(operator);
        return addPlugin(menu, plugin, label, enabled);
    }

    private JMenuItem addArrayOperatorPlugin(JMenu menu, ArrayOperator operator, String label, String newNamePattern)
    {
        FramePlugin plugin = new ImageArrayOperatorPlugin(operator, label, newNamePattern);
        return addPlugin(menu, plugin, label, plugin.isEnabled(frame));
    }

    private void addPlugin(JMenu menu, PluginHandler handler)
    {
        // If menu path is specified, retrieve or create the hierarchy of menus
        String menuPath = handler.getMenuPath();
        if (!menuPath.isEmpty())
        {
            // determine menu text hierarchy
            String[] tokens = menuPath.split(">");
            
            // remove the first item ("Plugins")
            int ntokens = tokens.length;
            String[] tokens2 = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, tokens2, 0, ntokens - 1);
            
            // retrieve correct menu
            for (String name : tokens2)
            {
                menu = getSubMenu(menu, name);
            }
        }
        
        addPlugin(menu, handler.getPlugin(), handler.getName());
    }
    
    private JMenu getSubMenu(JMenu baseMenu, String subMenuName)
    {
        for (Component sub : baseMenu.getMenuComponents())
        {
            if (sub instanceof JMenu)
            {
                if (((JMenu) sub).getText().equals(subMenuName))
                {
                    return (JMenu) sub;
                }
            }
        }
        
        // create a new sub-menu
        JMenu subMenu = new JMenu(subMenuName);
        baseMenu.add(subMenu);
        return subMenu;
    }

    private JMenuItem addPlugin(JMenu menu, Class<? extends FramePlugin> pluginClass, String label)
    {
        FramePlugin plugin = frame.gui.getPluginManager().retrievePlugin(pluginClass);
        return addPlugin(menu, plugin, label, plugin.isEnabled(frame));
    }
    
    private JMenuItem addPlugin(JMenu menu, Class<? extends FramePlugin> pluginClass, String label, boolean isEnabled)
    {
        FramePlugin plugin = frame.gui.getPluginManager().retrievePlugin(pluginClass);
        return addPlugin(menu, plugin, label, isEnabled);
    }

    private JMenuItem addPlugin(JMenu menu, FramePlugin plugin, String label)
    {
        return addPlugin(menu, plugin, label, plugin.isEnabled(frame));
    }

    private JMenuItem addPlugin(JMenu menu, Class<? extends FramePlugin> itemClass, String optionsString, String label)
    {
        // retrieve plugin
        FramePlugin plugin = frame.gui.getPluginManager().retrievePlugin(itemClass);
        if (plugin == null) return null;
        
        // create action that will catch action events
        RunPluginAction action = new RunPluginAction(frame, plugin, optionsString);
        
        // setup menu item
        JMenuItem item = new JMenuItem(action);
        item.setText(label);
        item.setIcon(this.emptyIcon);
        item.setMargin(new Insets(0, 0, 0, 0));
        item.setEnabled(plugin.isEnabled(frame));
        menu.add(item);
        return item;
    }
    
    private JMenuItem addPlugin(JMenu menu, FramePlugin plugin, String label, boolean enabled)
    {
        if (plugin == null) return null;
        JMenuItem item = createPluginMenuItem(plugin, label);
        item.setEnabled(enabled);
        menu.add(item);
        return item;
    }
    
    private JMenuItem createPluginMenuItem(FramePlugin plugin, String label)
    {
        JMenuItem item = new JMenuItem(new RunPluginAction(frame, plugin));
        item.setText(label);
        item.setIcon(this.emptyIcon);
        item.setMargin(new Insets(0, 0, 0, 0));
        return item;
    }
    
    /**
     * Initializes the empty icon image with an image of type ARGB, and filled
     * with the value "0x00FFFFFF": white, and totally transparent.
     */
    private static final ImageIcon createEmptyIcon()
    {
        int width = 16;
        int height = 16;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                image.setRGB(x, y, 0x00FFFFFF);
            }
        }
        return new ImageIcon(image);
    }
}
