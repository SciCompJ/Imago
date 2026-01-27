/**
 * 
 */
package imago.image;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import imago.gui.FrameMenuBuilder;
import imago.gui.FramePlugin;
import imago.image.plugins.ImageArrayOperatorPlugin;
import imago.image.plugins.edit.ChangeCurrentTool;
import imago.image.plugins.edit.ImageSetColorMapFactory;
import imago.image.plugins.edit.ImageSetScaleFromLineSelection;
import imago.image.plugins.shape.ImageFlip;
import imago.image.tools.DrawBrushValueTool;
import imago.image.tools.DrawValueTool;
import imago.image.tools.FloodFillTool;
import imago.image.tools.SelectEllipseTool;
import imago.image.tools.SelectLineSegmentTool;
import imago.image.tools.SelectPolygonTool;
import imago.image.tools.SelectPolylineTool;
import imago.image.tools.SelectRectangleTool;
import imago.image.tools.SelectionTool;
import imago.shape.ShapeManager;
import imago.transform.TransformManager;
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
 * Utility class for building menu bar of ImageFrame instances.
 */
public class ImageFrameMenuBuilder extends FrameMenuBuilder
{
    // ===================================================================
    // class members
    
    boolean hasHandle = false;
    boolean hasImage = false;
    boolean hasImage2D = false;
    boolean hasImage3D = false;
    boolean hasScalarImage = false;
    boolean hasLabelImage = false;
    boolean hasBinaryImage = false;
    boolean hasVectorImage = false;
    boolean hasColorImage = false;
    boolean hasRGB8Image = false;

    
    // ===================================================================
    // Constructor
    
    public ImageFrameMenuBuilder(ImageFrame frame)
    {
        super(frame);
    }
    
    
    // ===================================================================
    // menu creation methods
    
    public void setupMenuBar()
    {
        computeFlags();
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createImageEditMenu());
        menuBar.add(createImageMenu());
        menuBar.add(createImageProcessMenu());
        menuBar.add(createImageAnalyzeMenu());
        menuBar.add(createImageToolsMenu());
        addSharedMenus(menuBar);
        
        frame.getWidget().setJMenuBar(menuBar);
    }
    
    private void computeFlags()
    {
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
        
        this.hasHandle = handle != null;
        if (!hasHandle) 
            return;
        
        Image image = handle.getImage();
        this.hasImage = image != null;
        if (!hasImage) 
            return;
        
        Array<?> array = handle.getImage().getData();
        this.hasImage2D = array.dimensionality() == 2;
        this.hasImage3D = array.dimensionality() == 3;
        
        this.hasScalarImage = image.isScalarImage();
        this.hasLabelImage = image.isLabelImage();
        this.hasBinaryImage = image.isBinaryImage();
        this.hasVectorImage = image.isVectorImage();
        this.hasColorImage = image.isColorImage();
        this.hasRGB8Image = array instanceof RGB8Array;
    }
    
    /**
     * Creates the sub-menu for the "File" item in the main menu bar. This menu
     * is common to the image frame and the empty frame.
     */
    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu("File");
        addPlugin(fileMenu, imago.image.plugins.file.CreateNewImage.class, "New Image...");
        addPlugin(fileMenu, imago.image.plugins.file.OpenImage.class, "Open...");
        // Import demo images
        JMenu demoMenu = new JMenu("Demo Images");
        addPlugin(demoMenu, imago.image.plugins.file.OpenImage.class, "fileName=images/grains.png", "Rice grains");
        addPlugin(demoMenu, imago.image.plugins.file.OpenImage.class, "fileName=images/peppers.png", "Peppers");
        addPlugin(demoMenu, imago.image.plugins.file.OpenImage.class, "fileName=files/lena_gray_512.tif", "Lena");
        addPlugin(demoMenu, imago.image.plugins.file.OpenImage.class, "fileName=images/sunflower.png", "Sunflower");
        addPlugin(demoMenu, imago.image.plugins.file.OpenDemoStack.class, "Demo Stack");
        addPlugin(demoMenu, imago.image.plugins.edit.CreateDistanceToOctahedronImage3D.class, "Octahedron Distance Map");
        addPlugin(demoMenu, imago.image.plugins.edit.CreateColorCubeImage3D.class, "3D Color Cube");
        fileMenu.add(demoMenu);
        
        // Import less common file formats
        JMenu tiffFileMenu = new JMenu("Tiff Files");
        addPlugin(tiffFileMenu, imago.image.plugins.file.ReadImageTiff.class, "Read TIFF...");
        addPlugin(tiffFileMenu, imago.image.plugins.file.ReadTiffVirtualImage3D.class, "Read TIFF Virtual Image 3D...");
        addPlugin(tiffFileMenu, imago.image.plugins.file.ReadTiffStackSlice.class, "Read TIFF Slice...");
        tiffFileMenu.addSeparator();
        addPlugin(tiffFileMenu, imago.image.plugins.file.PrintImageFileTiffTags.class, "Print Tiff File Tags...");
        fileMenu.add(tiffFileMenu);
        
        // Import less common file formats
        JMenu fileImportMenu = new JMenu("Import");
        addPlugin(fileImportMenu, imago.image.plugins.file.ImportImageRawData.class, "Raw Data...");
        addPlugin(fileImportMenu, imago.image.plugins.file.ImportImageSeries.class, "Import Image Series...");
        fileImportMenu.addSeparator();
        addPlugin(fileImportMenu, imago.image.plugins.file.ImportImageMetaImage.class, "MetaImage Data...");
        addPlugin(fileImportMenu, imago.image.plugins.file.ShowMetaImageFileInfo.class, "Show MetaImage FileInfo...");
        addPlugin(fileImportMenu, imago.image.plugins.file.ImportImageVgi.class, "VGI Image...");
        fileMenu.add(fileImportMenu);
        
        fileMenu.addSeparator();
        addPlugin(fileMenu, imago.image.plugins.file.SaveImageIO.class, "Save As...");
        addPlugin(fileMenu, imago.image.plugins.file.SaveImageAsTiff.class, "Save As Tiff...");
        addPlugin(fileMenu, imago.image.plugins.file.SaveImageMetaImage.class, "Save As MetaImage...");
        
        fileMenu.addSeparator();
        addPlugin(fileMenu, imago.table.plugins.OpenTable.class, "Open Table...");
        JMenu demoTables = new JMenu("Demo Tables");
        addPlugin(demoTables, imago.table.plugins.OpenTable.class, "fileName=tables/fisherIris.csv", "Fisher's Iris");
        addPlugin(demoTables, imago.table.plugins.OpenTable.class, "fileName=tables/penguins_clean.csv", "Penguins (without NA)");
        fileMenu.add(demoTables);
        
        fileMenu.addSeparator();
        addPlugin(fileMenu, imago.gui.plugins.file.CloseCurrentFrame.class, "Close");
        addPlugin(fileMenu, imago.gui.plugins.file.CloseWithChildren.class, "Close With Children");
        addPlugin(fileMenu, imago.gui.plugins.file.QuitApplication.class, "Quit");
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
        addPlugin(selectionMenu, imago.image.plugins.edit.ImageCopySelectionToWorkspace.class, "Copy To Workspace");
        addPlugin(selectionMenu, imago.image.plugins.edit.ImportSelectionFromWorkspace.class, "Import From Workspace");
        addPlugin(selectionMenu, imago.image.plugins.edit.ImageSelectionToSceneGraph.class, "Add Selection to Scene Graph");
        addPlugin(selectionMenu, imago.image.plugins.edit.ShowShapeManagerFrame.class, "Show Shape Manager");
        addPlugin(selectionMenu, 
                (frame, options) -> {ShapeManager.getInstance(frame.getGui()).setVisible(true);},
                "Display Shape Manager");
        selectionMenu.addSeparator();
        addPlugin(selectionMenu, imago.image.plugins.edit.ImageClearSelection.class, "Clear Selection");
        editMenu.add(selectionMenu);

        // crop tools
        addPlugin(editMenu, imago.image.plugins.shape.ImageCropSelection.class, "Crop Selection", hasImage2D);
        addPlugin(editMenu, imago.image.plugins.shape.ImageCropDialog.class, "Crop...", hasImage);
        
        // zoom management
        editMenu.addSeparator();
        addPlugin(editMenu, imago.image.plugins.edit.ZoomIn.class, "Zoom In", hasImage);
        addPlugin(editMenu, imago.image.plugins.edit.ZoomOut.class, "Zoom Out", hasImage);
        addPlugin(editMenu, imago.image.plugins.edit.ZoomOne.class, "Zoom One", hasImage);
        addPlugin(editMenu, imago.image.plugins.edit.RefreshDisplay.class, "Refresh Display", hasImage);
        
        // add utility
        editMenu.addSeparator();
        JMenu sceneGraphMenu = new JMenu("Scene Graph");
        addPlugin(sceneGraphMenu, imago.image.plugins.edit.ShowSceneGraphTree.class, "Display Scene Graph Tree");
        addPlugin(sceneGraphMenu, imago.image.plugins.edit.PrintImageSceneGraph.class, "Print Scene Graph Tree");
        addPlugin(sceneGraphMenu, imago.image.plugins.edit.ToggleSceneGraphDisplay.class, "Toggle Scene Graph Display");
        editMenu.add(sceneGraphMenu);
        
        addPlugin(editMenu, imago.image.plugins.edit.DocClearShapes.class, "Clear Shapes");
        addPlugin(editMenu, imago.image.plugins.edit.AddPointShapeFromTable.class, "Add Point Shapes...");
        
        editMenu.addSeparator();
        JMenu settingsMenu = new JMenu("Settings");
        addPlugin(settingsMenu, imago.image.plugins.edit.ChooseFileDialogWidgetToolkit.class, "Choose File Dialog Widget Toolkits...");
        settingsMenu.addSeparator();
        addPlugin(settingsMenu, imago.image.plugins.edit.ChooseBrushValue.class, "Choose Brush Value...");
        addPlugin(settingsMenu, imago.image.plugins.edit.ChooseBrushRadius.class, "Choose Brush Radius...");
        
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
        addPlugin(menu, imago.image.plugins.edit.RenameImage.class, "Rename...", hasImage);
        addPlugin(menu, imago.image.plugins.edit.ImageSetScale.class, "Image Scale...", hasImage);
        addPlugin(menu, imago.image.plugins.edit.PrintImageInfos.class, "Print Image Info", hasImage);
        addPlugin(menu, imago.image.plugins.edit.PrintImageTiffTags.class, "Show TIFF Tags", hasImage);

        // Management of image representation
        menu.addSeparator();
        JMenu displayRangeMenu = new JMenu("Display Range");
        addPlugin(displayRangeMenu, imago.image.plugins.edit.SetImageDisplayRangeToDataType.class, "Set Data Type Display Range", hasScalarImage);
        addPlugin(displayRangeMenu, imago.image.plugins.edit.SetImageDisplayRangeToData.class, "Set Image Display Range", hasScalarImage | hasVectorImage);
        addPlugin(displayRangeMenu, imago.image.plugins.edit.SetImageDisplayRange.class, "Set Manual Display Range", hasScalarImage | hasVectorImage);
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
        addPlugin(colormapMenu, imago.image.plugins.edit.ImageColorMapDisplay.class, "Show Color Map in Table", hasImage && !hasRGB8Image);
        menu.add(colormapMenu);
        
        boolean hasDistanceImage = ((ImageFrame) frame).getImageHandle().getImage().getType() == ImageType.DISTANCE;
        addPlugin(menu, imago.image.plugins.edit.ImageSetBackgroundColor.class, "Set Background Color...", hasLabelImage || hasDistanceImage);
        addPlugin(menu, imago.image.plugins.edit.DisplayImagePair.class, "Display Image Pair", hasImage);

        // Several options for converting images
        menu.addSeparator();
        JMenu convertImageTypeMenu = new JMenu("Change Image Type");
        convertImageTypeMenu.setEnabled(hasImage);
        addPlugin(convertImageTypeMenu, imago.image.plugins.edit.SetImageType.class, "Set Image Display Type...", hasImage);
        addPlugin(convertImageTypeMenu, imago.image.plugins.edit.SetImageTypeToLabel.class, "Set Image Type to Label Image", hasScalarImage);
        convertImageTypeMenu.addSeparator();
        addPlugin(convertImageTypeMenu, imago.image.plugins.convert.ConvertImageToBinary.class, "Convert to Binary");
        addPlugin(convertImageTypeMenu, imago.image.plugins.convert.ConvertImageToLabel.class, "Convert to Label");
        menu.add(convertImageTypeMenu);
        
        JMenu convertDataTypeMenu = new JMenu("Convert Data Type");
        convertDataTypeMenu.setEnabled(hasImage);
        addPlugin(convertDataTypeMenu, imago.image.plugins.convert.ConvertImageToUInt8.class, "UInt8");
        addPlugin(convertDataTypeMenu, imago.image.plugins.convert.CreateScaledUInt8View.class, "UInt8 (adjust dynamic)", hasScalarImage);
        addPlugin(convertDataTypeMenu, imago.image.plugins.convert.ConvertImageToUInt16.class, "UInt16", hasScalarImage);
        convertDataTypeMenu.addSeparator();
        addPlugin(convertDataTypeMenu, imago.image.plugins.convert.ConvertImageToInt16.class, "Int16", hasScalarImage);
        addPlugin(convertDataTypeMenu, imago.image.plugins.convert.ConvertImageToInt32.class, "Int32", hasScalarImage);
        convertDataTypeMenu.addSeparator();
        addPlugin(convertDataTypeMenu, imago.image.plugins.convert.ConvertImageToFloat32.class, "Float32", hasImage);
        addPlugin(convertDataTypeMenu, imago.image.plugins.convert.ConvertImageToFloat64.class, "Float64", hasImage);
        menu.add(convertDataTypeMenu);
        
        // image dimensionality conversion plugins
        JMenu convertDimensionMenu = new JMenu("Convert Dimension(s)");
        convertDimensionMenu.setEnabled(hasImage);
        addPlugin(convertDimensionMenu, imago.image.plugins.convert.ConvertStackToMovie.class, "Convert Image 3D to movie", hasImage3D);
        addPlugin(convertDimensionMenu, imago.image.plugins.convert.ConvertScalarImageToVector.class, "Convert Scalar to Vector", hasImage3D);
        addPlugin(convertDimensionMenu, imago.image.plugins.convert.ConvertVectorImageToScalar.class, "Convert Vector To Scalar", hasVectorImage);
        menu.add(convertDimensionMenu);

        // Color images operators
        menu.addSeparator();
        JMenu colorMenu = new JMenu("Color");
        // editMenu.add(convertTypeMenu);
        addPlugin(colorMenu, imago.image.plugins.process.ImageSplitChannels.class, "Split Channels", hasVectorImage || hasColorImage);
        addPlugin(colorMenu, imago.image.plugins.process.MergeChannelImages.class, "Merge Channels");
        addPlugin(colorMenu, imago.image.plugins.convert.ConvertColorImageToGrayscale.class, "Convert To Grayscale", hasColorImage);
        addPlugin(colorMenu, imago.image.plugins.process.ColorImageExtractChannel.class, "Select Channel...", hasColorImage);
        addPlugin(colorMenu, imago.image.plugins.convert.ConvertUInt8ImageToRGB.class, "UInt8 to RGB8", hasScalarImage);
        addPlugin(colorMenu, imago.image.plugins.convert.ConvertLabelMapToRGB8.class, "Label Map to RGB", hasLabelImage);
        addPlugin(colorMenu, imago.image.plugins.convert.ConvertRGB8ImageToRGB16.class, "RGB8 to RGB16", hasRGB8Image);
        addPlugin(colorMenu, imago.image.plugins.convert.ScalarImagesColorDifference.class, "Color difference between two scalar images");
        menu.add(colorMenu);
        
        JMenu vectorMenu = new JMenu("Vector");
        addPlugin(vectorMenu, imago.image.plugins.process.VectorImageChannelView.class, "Channel View", hasVectorImage);
        addPlugin(vectorMenu, imago.image.plugins.convert.CreateVectorImageNorm.class, "Vector Image Norm", hasVectorImage);
        addPlugin(vectorMenu, imago.image.plugins.process.VectorImageAngle.class, "Vector Image Angle");
        addPlugin(vectorMenu, imago.image.plugins.convert.VectorImageConvertToRGB.class, "Convert to RGB", hasVectorImage);
        addPlugin(vectorMenu, imago.image.plugins.convert.CreateVectorImageRGB8View.class, "Create RGB View", hasVectorImage);
        addPlugin(vectorMenu, imago.image.plugins.edit.SetImageChannelNames.class, "Set Channel Names...", hasVectorImage || hasColorImage);
        addPlugin(vectorMenu, imago.image.plugins.convert.VectorImageToTable.class, "Convert To Table", hasVectorImage);
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
        addPlugin(geometryMenu, imago.image.plugins.shape.ImageRotateBy90Degrees.class, "Rotate by 90 degrees...");
        addPlugin(geometryMenu, imago.image.plugins.shape.ImageRotateAroundCenter.class, "Rotate...", hasImage2D);
        addPlugin(geometryMenu, imago.image.plugins.shape.ImageRotatedCrop.class, "Rotated Crop...", hasImage2D || hasImage3D);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, imago.image.plugins.shape.ImageReshape.class, "Reshape Image...", hasImage);
        addPlugin(geometryMenu, imago.image.plugins.shape.ImagePermuteDims.class, "Permute Image Dimensions...", hasImage);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, imago.image.plugins.shape.ImageConcatenate.class, "Concatenate..."); 
        addPlugin(geometryMenu, imago.image.plugins.shape.ImageAddBorders.class, "Add Borders..."); 
        addPlugin(geometryMenu, imago.image.plugins.shape.ImageSubsample.class, "Subsample...", hasImage);
        addPlugin(geometryMenu, imago.image.plugins.shape.ImageDownsample.class, "Downsample...", hasImage);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, imago.image.plugins.process.ImageCropThumbnailList.class, "Crop Thumbnails from Positions...", hasImage);
        
        menu.add(geometryMenu);
        
        // Create the menu for 3D images
        JMenu stackMenu = new JMenu("3D Images");
        stackMenu.setEnabled(hasImage3D);
        addPlugin(stackMenu, imago.image.plugins.shape.Image3DGetCurrentSlice.class, "Extract Current Slice", hasImage3D);
        addPlugin(stackMenu, imago.image.plugins.shape.Image3DGetSlice.class, "Extract Slice...", hasImage3D);
        addPlugin(stackMenu, imago.image.plugins.shape.Image3DOrthoslicesMontage.class, "OrthoSlices Montage...", hasImage3D);
        addPlugin(stackMenu, imago.image.plugins.shape.Image3DSliceMontage.class, "Image 3D Montage...", hasImage3D);
        addPlugin(stackMenu, imago.image.plugins.shape.ImageOrthogonalProjection.class, "Orthogonal Projection...", hasImage3D && hasScalarImage);
        stackMenu.addSeparator();
        addPlugin(stackMenu, imago.image.plugins.process.Image3DSetOrthoSlicesDisplay.class, "Set Orthoslices Display", hasImage3D);
        menu.add(stackMenu);

        menu.addSeparator();
        addPlugin(menu, imago.image.plugins.process.ImageDuplicate.class, "Duplicate", hasImage);
        addArrayOperatorPlugin(menu, new ImageInverter(), "Invert", "%s-inv");
        
        // submenu for creation of phantoms
        JMenu phantomMenu = new JMenu("Phantoms");
        addPlugin(phantomMenu, imago.image.plugins.edit.ImageFillDisk.class, "Fill Disk...");
        addPlugin(phantomMenu, imago.image.plugins.edit.ImageFillEllipse.class, "Fill Ellipse...");
        addPlugin(phantomMenu, imago.image.plugins.edit.ImageFillBox.class, "Fill Box...");
        addPlugin(phantomMenu, imago.image.plugins.edit.ImageFillEllipsoid.class, "Fill Ellipsoid...");
        phantomMenu.addSeparator();
        addPlugin(phantomMenu, imago.image.plugins.edit.ImageSelectionToMask.class, "Selection To Mask");
        addPlugin(phantomMenu, imago.image.plugins.edit.ImageSelectionToDistanceMap.class, "Selection To Distance Map");
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
        addPlugin(mathsMenu, imago.image.plugins.process.ImageApplyMathFunction.class, "Apply Function...", hasScalarImage);
        addPlugin(mathsMenu, imago.image.plugins.process.ImageApplySingleValueOperator.class, "Math operator (image+value)...");
        addPlugin(mathsMenu, imago.image.plugins.process.ImageApplyMathBinaryOperator.class, "Math operator (Image pair)...");
        mathsMenu.addSeparator();
        addPlugin(mathsMenu, imago.image.plugins.process.ImageApplyLogicalBinaryOperator.class, "Logical operator (Image pair)...");
        menu.add(mathsMenu);
        menu.addSeparator();

        // Noise reduction filters
        JMenu filtersMenu = new JMenu("Filters");
        addPlugin(filtersMenu, imago.image.plugins.process.ImageBoxFilter.class, "Box Filter...");
        addPlugin(filtersMenu, imago.image.plugins.process.BoxFilter3x3FloatPlugin.class, "Box Filter 2D 3x3 (float)", hasScalarImage);
        addArrayOperatorPlugin(filtersMenu, net.sci.image.filtering.GaussianFilter5x5.class, "Gaussian Filter 5x5", hasScalarImage && hasImage2D);
        addPlugin(filtersMenu, imago.image.plugins.process.ImageMedianFilterBox.class, "Median Filter...");
        addPlugin(filtersMenu, imago.image.plugins.binary.BinaryImageBoxMedianFilter.class, "Binary Median Filter...");
        addPlugin(filtersMenu, imago.image.plugins.process.ImageMinMaxFilterBox.class, "Min/Max Filter...");
        filtersMenu.addSeparator();
        addPlugin(filtersMenu, imago.image.plugins.process.ImageVarianceFilterBox.class, "Variance Filter...");
        menu.add(filtersMenu);
        
        // Gradient filters
        JMenu gradientFiltersMenu = new JMenu("Gradient Filters");
        addImageOperatorPlugin(gradientFiltersMenu, net.sci.image.filtering.SobelGradient.class, "Sobel Gradient", hasScalarImage);
        addImageOperatorPlugin(gradientFiltersMenu, net.sci.image.filtering.SobelGradientNorm.class, "Sobel Gradient Norm", hasScalarImage);
        addImageOperatorPlugin(gradientFiltersMenu, net.sci.image.contrast.VectorArrayNorm.class, "Vector Image Norm", hasVectorImage);
        menu.add(gradientFiltersMenu);
        
        JMenu morphologyMenu = new JMenu("Mathematical Morphology");
        addPlugin(morphologyMenu, imago.image.plugins.process.ImageMorphologicalFilter.class, "Morphological Filters...");
        addPlugin(morphologyMenu, imago.image.plugins.binary.BinaryImageMorphologicalFilter.class, "Binary Morphological Filters...");
        addPlugin(morphologyMenu, imago.image.plugins.binary.BinaryImageMorphologicalFilterBall.class, "Ball Binary Morphological Filters...");

        morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, imago.image.plugins.process.ImageRegionalExtrema.class, "Regional Min./Max...", hasScalarImage);
        addPlugin(morphologyMenu, imago.image.plugins.process.ImageExtendedExtrema.class, "Extended Min./Max...", hasScalarImage);
        addPlugin(morphologyMenu, imago.image.plugins.process.ImageImposeExtrema.class, "Impose Min./Max...", hasScalarImage);
        addPlugin(morphologyMenu, imago.image.plugins.process.ImageMorphologicalReconstruction.class, "Morphological Reconstruction...");
        addPlugin(morphologyMenu, imago.image.plugins.binary.BinaryImageMorphologicalReconstruction.class, "Binary Morphological Reconstruction...");
        addPlugin(morphologyMenu, imago.image.plugins.process.ImageIteratedGeodesicDilations.class, "Geodesic Dilation...");
        morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, imago.image.plugins.process.ImageFillHoles.class, "Fill Holes");
        addPlugin(morphologyMenu, imago.image.plugins.binary.BinaryImageFillHoles.class, "Binary Fill Holes");
        addPlugin(morphologyMenu, imago.image.plugins.process.ImageKillBorders.class, "Kill Borders");
        addPlugin(morphologyMenu, imago.image.plugins.binary.BinaryImageKillBorders.class, "Binary Kill Borders");
        menu.add(morphologyMenu);
        
        menu.addSeparator();
        JMenu transformsMenu = new JMenu("Geometric transforms");
        addPlugin(transformsMenu, imago.image.plugins.register.ImagePair2DRegister.class, "Simple Image Registration", hasScalarImage);
        addPlugin(transformsMenu, imago.image.plugins.register.ApplyExistingTransformToImage.class, "Apply Transform To Image", hasScalarImage);
        transformsMenu.addSeparator();
        addPlugin(transformsMenu, 
                (frame, options) -> {TransformManager.getInstance(frame.getGui()).setVisible(true);},
                "Show Transform Manager");
        menu.add(transformsMenu);
        
        menu.addSeparator();
        addPlugin(menu, imago.image.plugins.vectorize.ImageFindNonZeroPixels.class, "Find Non-Zeros Elements", hasImage2D && hasScalarImage);
        addPlugin(menu, imago.image.plugins.vectorize.ImageIsocontour.class, "Isocontour...");
        addPlugin(menu, imago.image.plugins.vectorize.ExportIsosurface.class, "Export Isosurface...");
        addPlugin(menu, imago.image.plugins.vectorize.Image3DIsosurface.class, "Compute Isosurface...");
        addPlugin(menu, imago.image.plugins.process.Image3DKymograph.class, "Kymograph", hasImage3D && hasScalarImage);

        // operators specific to binary images
        menu.addSeparator();
        JMenu segmentationMenu = new JMenu("Segmentation");
        addPlugin(segmentationMenu, imago.image.plugins.process.ImageOtsuThreshold.class, "Otsu Auto Threshold", hasScalarImage);
        addPlugin(segmentationMenu, imago.image.plugins.process.ImageIsodataThreshold.class, "Isodata Auto Threshold", hasScalarImage);
        addPlugin(segmentationMenu, imago.image.plugins.process.ImageManualThreshold.class, "Manual Threshold", hasScalarImage);
        addPlugin(segmentationMenu, imago.image.plugins.process.ImageHysteresisThreshold.class, "Hysteresis Threshold", hasScalarImage);
        segmentationMenu.addSeparator();
        addPlugin(segmentationMenu, imago.image.plugins.process.ImageKMeansSegmentation.class, "K-Means Segmentation", hasImage);
        segmentationMenu.addSeparator();
        addPlugin(segmentationMenu, imago.image.plugins.process.ImageWatershed.class, "Watershed", hasScalarImage);
        addPlugin(segmentationMenu, imago.image.plugins.process.ImageMarkerControlledWatershed.class, "Marker-Based Watershed", hasScalarImage);
        menu.add(segmentationMenu);

        // operators specific to binary images
        menu.addSeparator();
        JMenu binaryMenu = new JMenu("Binary Images");
        addPlugin(binaryMenu, imago.image.plugins.binary.BinaryImageConnectedComponentsLabeling.class, "Connected Components Labeling");
        addPlugin(binaryMenu, imago.image.plugins.binary.BinaryImageComponentsLabelingAndContouring.class, "Components Labeling and Contouring");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, imago.image.plugins.binary.BinaryImageEuclideanDistanceMap.class, "Distance Map");
        addPlugin(binaryMenu, imago.image.plugins.binary.BinaryImageChamferDistanceMap.class, "Chamfer Distance Map");
        addPlugin(binaryMenu, imago.image.plugins.binary.BinaryImage3DDepthMap.class, "3D Binary Image Depth Map");
        addPlugin(binaryMenu, imago.image.plugins.binary.ImageGeodesicDistanceMap.class, "Geodesic Distance Map...");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, imago.image.plugins.binary.BinaryImageSkeleton.class, "IJ Skeleton");
        addPlugin(binaryMenu, imago.image.plugins.binary.BinaryImageSplitCoalescentParticles.class, "Split Particles...");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, imago.image.plugins.binary.BinaryImageOverlay.class, "Binary Overlay...");
        addPlugin(binaryMenu, imago.image.plugins.binary.ApplyBinaryMask.class, "Apply Binary Mask...");
        menu.add(binaryMenu);
        
        // operators specific to binary images
        JMenu labelMenu = new JMenu("Binary / Label Images");
        addPlugin(labelMenu, imago.image.plugins.process.LabelMapCropLabel.class, "Crop Label...", hasLabelImage);
        addPlugin(labelMenu, imago.image.plugins.process.LabelMapSizeOpening.class, "Size Opening...", hasLabelImage);
        labelMenu.addSeparator();
        addPlugin(labelMenu, imago.image.plugins.process.LabelMapSkeleton.class, "Skeleton (2D)");
        labelMenu.addSeparator();
        addPlugin(labelMenu, imago.image.plugins.vectorize.BinaryImageBoundaryGraph.class, "Boundary Graph", hasImage2D && hasBinaryImage);
        addPlugin(labelMenu, imago.image.plugins.vectorize.LabelMapToBoundaryPolygons.class, "Region Boundaries to Polygons", hasImage2D && hasLabelImage);
        menu.add(labelMenu);
        
        return menu;
    }

    /**
     * Creates the sub-menu for the "process" item in the main Menu bar.
     */
    private JMenu createImageAnalyzeMenu()
    {
        JMenu menu = new JMenu("Analyze");

        addPlugin(menu, imago.image.plugins.analyze.ImageHistogram.class, "Histogram", hasImage);
        addPlugin(menu, imago.image.plugins.analyze.ImageRoiHistogram.class, "ROI Histogram", hasImage && hasImage2D);
        addPlugin(menu, imago.image.plugins.analyze.ImageMeanValue.class, "Mean Value", hasImage);
        addPlugin(menu, imago.image.plugins.analyze.ColorImageBivariateHistograms.class, "Bivariate Color Histograms", hasColorImage);
        addPlugin(menu, imago.image.plugins.process.ImageBivariateHistogram.class, "Bivariate Histogram");
        menu.addSeparator();
        addPlugin(menu, imago.image.plugins.analyze.ImageLineProfile.class, "Line Profile", hasImage);
        addPlugin(menu, imago.image.plugins.analyze.ImageAnalyzeWithinROI.class, "Intensity within ROI", hasImage);
        addPlugin(menu, imago.image.plugins.analyze.ImagePlotChannels.class, "Channel Profile", hasImage);

        menu.addSeparator();
        JMenu regFeatMenu = new JMenu("Region Features");
        addPlugin(regFeatMenu, imago.image.plugins.analyze.RegionMorphology2D.class, "Regions Morphology", hasImage2D && hasLabelImage);
        addPlugin(regFeatMenu, imago.image.plugins.analyze.RegionFeatureOverlay2D.class, "Regions Feature Overlay", hasImage2D && hasLabelImage);
        menu.add(regFeatMenu);
        
        JMenu regions2dMenu = new JMenu("Regions (2D)");
        regions2dMenu.setEnabled(hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.image.plugins.analyze.LabelImageIntrinsicVolumes.class, "Regions Intrinsic Volumes", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.image.plugins.analyze.LabelImageEquivalentDisks.class, "Regions Equivalent Disks", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.image.plugins.analyze.LabelImageEquivalentEllipses.class, "Regions Equivalent Ellipses", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.image.plugins.analyze.LabelImageOrientedBoxes.class, "Regions Oriented Boxes", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.image.plugins.analyze.LabelImageConvexHulls.class, "Regions Conxex Hulls", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.image.plugins.analyze.LabelImageMaxFeretDiameters.class, "Regions Max. Feret Diameters", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, imago.image.plugins.analyze.LabelImageGeodesicDiameters.class, "Regions Geodesic Diameters", hasImage2D && hasLabelImage);
        regions2dMenu.addSeparator();
        addPlugin(regions2dMenu, imago.image.plugins.analyze.LabelImageAdjacencies.class, "Regions Adjacencies", hasImage2D && hasLabelImage);
        menu.add(regions2dMenu);

        JMenu regions3dMenu = new JMenu("Regions (3D)");
        regions3dMenu.setEnabled(hasImage3D && hasLabelImage);
        addPlugin(regions3dMenu, imago.image.plugins.analyze.LabelImageEquivalentEllipsoids.class, "Regions Equivalent Ellipsoids", hasImage3D && hasLabelImage);
        menu.add(regions3dMenu);

        addPlugin(menu, imago.image.plugins.analyze.LabelImageBoundingBoxes.class, "Bounding Boxes", (hasImage2D || hasImage3D) && hasLabelImage);
        addPlugin(menu, imago.image.plugins.analyze.LabelImageCentroids.class, "Regions Centroids", (hasImage2D || hasImage3D) && hasLabelImage);

        menu.addSeparator();
        JMenu textureMenu = new JMenu("Texture Analysis");
        addPlugin(textureMenu, imago.image.plugins.analyze.GrayLevelImageCooccurenceMatrix.class, "Gray Level Co-Occurence Matrix", hasImage2D && hasScalarImage);
        addPlugin(textureMenu, imago.image.plugins.analyze.ImageGrayscaleGranulometry.class, "Grayscale granulometry", hasImage2D && hasScalarImage);
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
    
    private JMenuItem addImageOperatorPlugin(JMenu menu, Class<? extends ImageOperator> opClass, String label, boolean enabled)
    {
        FramePlugin plugin = frame.getGui().getPluginManager().retrievePlugin(opClass);
        return addPlugin(menu, plugin, label, enabled);
    }

    private JMenuItem addArrayOperatorPlugin(JMenu menu, Class<? extends ArrayOperator> opClass, String label, boolean enabled)
    {
        FramePlugin plugin = frame.getGui().getPluginManager().retrievePlugin(opClass);
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

}
