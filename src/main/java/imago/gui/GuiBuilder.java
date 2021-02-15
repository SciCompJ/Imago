/**
 * 
 */
package imago.gui;

import java.awt.image.BufferedImage;

import javax.swing.*;

import imago.app.ImageHandle;
import imago.gui.action.RunPluginAction;
import imago.gui.tool.*;
import imago.plugin.BoxFilter3x3FloatPlugin;
import imago.plugin.CloseCurrentFrame;
import imago.plugin.CloseWithChildren;
import imago.plugin.QuitApplication;
import imago.plugin.developer.DisplayExceptionDialog;
import imago.plugin.edit.*;
import imago.plugin.image.ImageArrayOperatorPlugin;
import imago.plugin.image.ImageOperatorPlugin;
import imago.plugin.image.analyze.*;
import imago.plugin.image.convert.*;
import imago.plugin.image.edit.*;
import imago.plugin.image.file.*;
import imago.plugin.image.process.*;
import imago.plugin.image.shape.*;
import imago.plugin.image.vectorize.BinaryImageBoundaryGraph;
import imago.plugin.image.vectorize.ImageFindNonZeroPixels;
import imago.plugin.image.vectorize.ImageIsocontour;
import imago.plugin.plugin.crop.*;
import imago.plugin.table.OpenTable;
import imago.plugin.table.SaveTable;
import imago.plugin.table.ShowDemoTable;
import imago.plugin.table.edit.FoldTableToImage;
import imago.plugin.table.edit.MergeTablesByColumns;
import imago.plugin.table.edit.PrintTableToConsole;
import imago.plugin.table.edit.TableKeepNumericColumns;
import imago.plugin.table.edit.TableSelectColumns;
import imago.plugin.table.edit.TransposeTable;
import imago.plugin.table.plot.TableLinePlot;
import imago.plugin.table.plot.TableScatterPlot;
import imago.plugin.table.process.TableKMeans;
import imago.plugin.table.process.TablePca;
import net.sci.array.Array;
import net.sci.array.ArrayOperator;
import net.sci.array.color.RGB8Array;
import net.sci.array.process.math.PowerOfTwo;
import net.sci.array.process.math.Sqrt;
import net.sci.array.process.shape.Rotate90;
import net.sci.image.ColorMaps;
import net.sci.image.Image;
import net.sci.image.ImageOperator;
import net.sci.image.process.*;
import net.sci.image.process.filter.GaussianFilter5x5;

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
	 * @param frame the frame to build.
	 */
	public GuiBuilder(ImagoFrame frame)
	{
	    this.frame = frame;
		createEmptyIcon();
	}

	public void createMenuBar()
	{
	    computeFlags();
	    
		JMenuBar menuBar = new JMenuBar();
		if (frame instanceof ImageFrame)
		{
	        menuBar.add(createImageFileMenu());
    		menuBar.add(createImageEditMenu());
    		menuBar.add(createImageMenu());
    		menuBar.add(createImageToolsMenu());
    		menuBar.add(createImageProcessMenu());
    		menuBar.add(createImageAnalyzeMenu());
		}
		else if (frame instanceof TableFrame)
		{
            menuBar.add(createTableFileMenu());
            menuBar.add(createTableEditMenu());
            menuBar.add(createTableProcessMenu());
		}
		else if (frame instanceof ImagoEmptyFrame)
		{
	        menuBar.add(createImageFileMenu());
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
	 * Creates the sub-menu for the "File" item in the main menu bar.
	 */
	private JMenu createImageFileMenu()
	{
		JMenu fileMenu = new JMenu("File");
		addPlugin(fileMenu, new CreateNewImage(), "New Image...");
		addPlugin(fileMenu, new OpenImage(), "Open...");
        addPlugin(fileMenu, new ReadImageTiff(), "Read TIFF...");
        addPlugin(fileMenu, new ReadTiffVirtualImage3D(), "Read TIFF Virtual Image 3D...");
        addPlugin(fileMenu, new ReadTiffStackSlice(), "Read TIFF Slice");
        addPlugin(fileMenu, new ImportImageSeries(), "Import Image Series...");

		// Import demo images
		JMenu demoMenu = new JMenu("Open Demo Image");
        addPlugin(demoMenu, new OpenDemoImage("files/grains.png"), "Rice grains");
        addPlugin(demoMenu, new OpenDemoImage("files/lena_gray_512.tif"), "Lena");
		addPlugin(demoMenu, new OpenDemoImage("files/sunflower.png"), "Sunflower");
		addPlugin(demoMenu, new OpenDemoStack(), "Demo Stack");
        addPlugin(demoMenu, new CreateDistanceToOctahedronImage3D(), "Octahedron Distance Map");
		addPlugin(demoMenu, new CreateColorCubeImage3D(), "3D Color Cube");
        fileMenu.add(demoMenu);

		// Import less common file formats
		JMenu fileImportMenu = new JMenu("Import");
		addPlugin(fileImportMenu, new ImportImageRawData(), "Raw Data...");
		addPlugin(fileImportMenu, new ImportImageMetaImage(), "MetaImage Data...");
		addPlugin(fileImportMenu, new ImportImageVgi(), "VGI Image...");
		fileMenu.add(fileImportMenu);

        fileMenu.addSeparator();
        addPlugin(fileMenu, new SaveImageMetaImage(), "Save As MetaImage");

//		addMenuItem(demoMenu, new CreateWhiteNoiseImageAction(frame,
//				"createWhiteNoiseImage"), "White Noise Array<?>");
        fileMenu.addSeparator();
        addPlugin(fileMenu, new OpenTable(), "Open Table...");
        addPlugin(fileMenu, new ShowDemoTable(), "Show Demo Table");
        
		fileMenu.addSeparator();
        addPlugin(fileMenu, new CloseCurrentFrame(), "Close", !(frame instanceof ImagoEmptyFrame));
        addPlugin(fileMenu, new CloseWithChildren(), "Close With Children", !(frame instanceof ImagoEmptyFrame));
		addPlugin(fileMenu, new QuitApplication(), "Quit");
		return fileMenu;
	}

	/**
	 * Creates the sub-menu for the "Edit" item in the main menu bar.
	 */
	private JMenu createImageEditMenu()
	{
		JMenu editMenu = new JMenu("Edit");

		// zoom handles
		addPlugin(editMenu, new ZoomIn(), "Zoom In", hasImage);
		addPlugin(editMenu, new ZoomOut(), "Zoom Out", hasImage);
		addPlugin(editMenu, new ZoomOne(), "Zoom One", hasImage);
        addPlugin(editMenu, new RefreshDisplay(), "Refresh Display", hasImage);
		
        // crop tools
        addPlugin(editMenu, new ImageCropSelection(), "Crop Selection", hasImage2D);
        addPlugin(editMenu, new ImageCropDialog(), "Crop...", hasImage);
        
        // selection sub-menu
        JMenu selectionMenu = new JMenu("Selection");
        addPlugin(selectionMenu, new ImageCopySelectionToWorkspace(), "Copy To Workspace");
        addPlugin(selectionMenu, new ImportSelectionFromWorkspace(), "Import From Workspace");
        editMenu.add(selectionMenu);

        // add utility
		editMenu.addSeparator();
		JMenu sceneGraphMenu = new JMenu("Scene Graph");
		addPlugin(sceneGraphMenu, new ToggleSceneGraphDisplay(), "Toggle Scene Graph Display");
		addPlugin(sceneGraphMenu, new ImageSelectionToSceneGraph(), "Add Selection to scene graph");
		addPlugin(sceneGraphMenu, new PrintImageSceneGraph(), "Print SceneGraph Tree");
		editMenu.add(sceneGraphMenu);
		
		JMenu editMoreMenu = new JMenu("More");
		addPlugin(editMenu, new PrintFrameList(), "Print Frame List");
		addPlugin(editMoreMenu, new PrintDocumentList(), "Print Document List");
        addPlugin(editMoreMenu, new PrintWorkspaceContent(), "Print Workspace Content");
		addPlugin(editMoreMenu, new DocClearShapes(), "Clear Shapes");
		editMenu.add(editMoreMenu);
		
        editMenu.addSeparator();
        JMenu settingsMenu = new JMenu("Settings");
        addPlugin(settingsMenu, new ChooseBrushValue(), "Choose Brush Value...");
        addPlugin(settingsMenu, new ChooseBrushRadius(), "Choose Brush Radius...");
        
        editMenu.add(settingsMenu);
		return editMenu;
	}

	/**
	 * Creates the sub-menu for the "Image" item in the main Menu bar.
	 */
	private JMenu createImageMenu()
	{
		JMenu menu = new JMenu("Image");
		
        addPlugin(menu, new PrintImageInfos(), "Print Image Info", hasImage);

        JMenu imageTypeMenu = new JMenu("Image Type");
        addPlugin(imageTypeMenu, new SetImageTypeToLabel(), "Set to Label Image", hasScalarImage);
        menu.add(imageTypeMenu);
        
	      // Type conversion handles
        addPlugin(menu, new ConvertScalarImageToUInt8(), "Convert to Gray8", hasScalarImage);
        JMenu convertDataTypeMenu = new JMenu("Convert Data-Type");
        convertDataTypeMenu.setEnabled(hasImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToBinary(), "Binary", hasScalarImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToUInt8(), "UInt8", hasScalarImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToUInt16(), "UInt16", hasScalarImage);
        convertDataTypeMenu.addSeparator();
        addPlugin(convertDataTypeMenu, new CreateScaledUInt8View(), "UInt8 View", hasScalarImage);
        convertDataTypeMenu.addSeparator();
        addPlugin(convertDataTypeMenu, new ConvertImageToInt16(), "Int16", hasScalarImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToInt32(), "Int32", hasScalarImage);
        convertDataTypeMenu.addSeparator();
        addPlugin(convertDataTypeMenu, new ConvertImageToFloat32(), "Float32", hasImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToFloat64(), "Float64", hasImage);
        menu.add(convertDataTypeMenu);
        
        // image type conversion handles
        addPlugin(menu, new ConvertStackToMovie(), "Convert stack to movie", hasImage3D);

        menu.addSeparator();
		JMenu displayRangeMenu = new JMenu("Display Range");
		addPlugin(displayRangeMenu, new SetImageDisplayRangeToDataType(), "Set Data Type Display Range", hasScalarImage);
		addPlugin(displayRangeMenu, new SetImageDisplayRangeToData(), "Set Image Display Range", hasScalarImage | hasVectorImage);
		addPlugin(displayRangeMenu, new SetImageDisplayRange(), "Set Manual Display Range", hasScalarImage | hasVectorImage);
		// addMenuItem(editMenu, new SetDisplayRangeUnitIntervalAction(frame,
		// "setDisplayRangeUnitInterval"),
		// "Set Display Range [0 ; 1]", hasScalarImage || hasVectorImage);
		menu.add(displayRangeMenu);

		addArrayOperatorPlugin(menu, new DynamicAdjustment(.01), "Adjust Grayscale Dynamic", hasScalarImage);

        
        // Color conversion handles
        menu.addSeparator();
        JMenu colorMenu = new JMenu("Color");
        // editMenu.add(convertTypeMenu);
        addPlugin(colorMenu, new ConvertRGB8ImageToUInt8(), "Convert to UInt8", hasColorImage);
        addPlugin(colorMenu, new ImageSplitChannels(), "Split Channels", hasVectorImage || hasColorImage);
        addPlugin(colorMenu, new MergeChannelImages(), "Merge Channels");
        addPlugin(colorMenu, new ColorImageExtractChannel(), "Extract Channel...", hasColorImage);
        addPlugin(colorMenu, new ConvertUInt8ImageToRGB(), "UInt8 to RGB8", hasScalarImage);
        addPlugin(colorMenu, new ConvertRGB8ImageToRGB16(), "RGB8 to RGB16", hasRGB8Image);
        addPlugin(colorMenu, new ScalarImagesColorDifference(), "Color difference between two scalar images");
        
        // addMenuItem(editMenu, new MetaImageOperatorAction(frame,
        // "colorToGray",
        // new Gray8Converter()), "RGB -> Gray8", hasColorImage);
        menu.add(colorMenu);

        JMenu vectorMenu = new JMenu("Vector");
        addPlugin(vectorMenu, new VectorImageChannelView(), "Channel View", hasVectorImage);
        addPlugin(vectorMenu, new CreateVectorImageNorm(), "Vector Image Norm", hasVectorImage);
        addPlugin(vectorMenu, new VectorImageConvertToRGB(), "Convert to RGB", hasVectorImage);
        addPlugin(vectorMenu, new CreateVectorImageRGB8View(), "Create RGB View", hasVectorImage);
        addPlugin(vectorMenu, new ConvertChannelsToDimension(), "Convert Channels to Dimension", hasVectorImage);
        addPlugin(vectorMenu, new ConvertDimensionToChannels(), "Convert Dimension to Channels", hasScalarImage);
        addPlugin(vectorMenu, new VectorImageToTable(), "Convert To Table", hasVectorImage);
        menu.add(vectorMenu);

        // add Colormap utils
        JMenu colormapMenu = new JMenu("Color Maps");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.GRAY), "Gray");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.JET), "Jet");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.BLUE_GRAY_RED), "Blue-Gray-Red");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.RED), "Red");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.GREEN), "Green");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.BLUE), "Blue");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.FIRE), "Fire");
        addPlugin(colormapMenu, new ImageSetColorMapFactory(ColorMaps.GLASBEY), "Glasbey");
        menu.add(colormapMenu);
        
        addPlugin(menu, new ImageSetBackgroundColor(), "Set Background Color...", hasLabelImage);
        

        menu.addSeparator();
        addPlugin(menu, new ImageSetScale(), "Image Scale...", hasImage);
        
        menu.addSeparator();
		JMenu geometryMenu = new JMenu("Geometry");
		geometryMenu.setEnabled(hasImage);
		addPlugin(geometryMenu, new ImageFlip(0), "Horizontal Flip ");
		addPlugin(geometryMenu, new ImageFlip(1), "Vertical Flip");
		addPlugin(geometryMenu, new ImageFlip(2), "Z-Flip");
		geometryMenu.addSeparator();
        addArrayOperatorPlugin(geometryMenu, new Rotate90(-1), "Rotate Left", hasImage2D);
        addArrayOperatorPlugin(geometryMenu, new Rotate90(+1), "Rotate Right", hasImage2D);
		addPlugin(geometryMenu, new ImageRotateAroundCenter(), "Rotate...", hasImage2D);
        addPlugin(geometryMenu, new Image3DRotate90(), "Rotate 3D by 90 degrees...", hasImage3D);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, new ImageReshape(), "Reshape Image...", hasImage);
        addPlugin(geometryMenu, new ImagePermuteDims(), "Permute Image Dimensions...", hasImage);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, new ImageConcatenate(), "Concatenate...");
        addPlugin(geometryMenu, new ImageDownSampleBy2(), "Down Sample by 2", hasImage);
        addPlugin(geometryMenu, new ImageSubsample(), "Subsample...", hasImage);
        
		menu.add(geometryMenu);

		// Create the menu for 3D images
		JMenu stackMenu = new JMenu("Stacks");
		stackMenu.setEnabled(hasImage3D);
//		addMenuItem(stackMenu, 
//				new MiddleSliceImageAction(frame, "middleSlice"), "Middle Slice", hasImage3D);
        addPlugin(stackMenu, new Image3DGetCurrentSlice(), "Extract Current Slice", hasImage3D);
        addPlugin(stackMenu, new Image3DGetSlice(), "Extract Slice...", hasImage3D);
        addPlugin(stackMenu, new Image3DOrthoslicesImage(), "Create OrthoSlices Image...", hasImage3D);
        stackMenu.addSeparator();
        addPlugin(stackMenu, new Image3DSetOrthoSlicesDisplay(), "Set Orthoslices Display", hasImage3D);
        stackMenu.addSeparator();
		addPlugin(stackMenu, new ConvertImage3DToVectorImage(), "Stack To Vector", hasImage3D);
		menu.add(stackMenu);

        menu.addSeparator();
		addPlugin(menu, new ImageDuplicate(), "Duplicate", hasImage);
		addArrayOperatorPlugin(menu, new ImageInverter(), "Invert", hasScalarImage || hasColorImage);
        
        // submenu for creation of phantoms
        JMenu phantomMenu = new JMenu("Phantoms");
        addPlugin(phantomMenu, new ImageFillDisk(), "Fill Disk...");
        addPlugin(phantomMenu, new ImageFillEllipse(), "Fill Ellipse...");
        addPlugin(phantomMenu, new ImageSelectionToMask(), "Selection To Mask");
        addPlugin(phantomMenu, new ImageSelectionToDistanceMap(), "Selection To Distance Map");
        menu.add(phantomMenu);
        
		menu.addSeparator();
        addPlugin(menu, new PrintImageTiffTags(), "Print TIFF Tags", hasImage);
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
                    new ChangeCurrentTool(new SelectLineSegmentTool(viewer, "selectLineSegment")),
                    "Select Line", hasImage);
            addPlugin(toolsMenu, 
                    new ChangeCurrentTool(new SelectRectangleTool(viewer, "selectRectangle")),
                    "Select Rectangle", hasImage);
            addPlugin(toolsMenu, 
                    new ChangeCurrentTool(new SelectPolygonTool(viewer, "selectPolygon")),
                    "Select Polygon", hasImage);

            toolsMenu.addSeparator();
            addPlugin(toolsMenu, 
                    new ChangeCurrentTool(new DrawValueTool(viewer, "drawValue")),
                    "Draw (Dot)", hasScalarImage);
            addPlugin(toolsMenu, 
                    new ChangeCurrentTool(new DrawBrushValueTool(viewer, "drawBrushValue")),
                    "Draw (Brush)", hasScalarImage);
//            addPlugin(editMenu, 
//                    new ChangeCurrentTool(new DrawValueTool(viewer, "drawBlack", 0.0)),
//                    "Draw Black", hasScalarImage);

            toolsMenu.addSeparator();
		}

		return toolsMenu;
	}

	/**
	 * Creates the sub-menu for the "process" item in the main Menu bar.
	 */
	private JMenu createImageProcessMenu()
	{
		JMenu menu = new JMenu("Process");

		JMenu mathsMenu = new JMenu("Math");
        addPlugin(mathsMenu, new ImageApplyMathFunction(), "Math Function...", hasScalarImage);
        addArrayOperatorPlugin(mathsMenu, new Sqrt(), "Sqrt", hasScalarImage);
        addArrayOperatorPlugin(mathsMenu, new PowerOfTwo(), "Power Of Two", hasScalarImage);
        mathsMenu.addSeparator();
        addPlugin(mathsMenu, new ImageValueOperator(), "Image value operator...");
        addPlugin(mathsMenu, new ImageArrayBinaryMathOperator(), "Image pair operator...");
		menu.add(mathsMenu);
		menu.addSeparator();

		// Noise reduction filters
        JMenu filtersMenu = new JMenu("Filters");
        addPlugin(filtersMenu, new ImageBoxFilter(), "Box Filter...");
		addPlugin(filtersMenu, new BoxFilter3x3FloatPlugin(), "Box Filter 2D 3x3 (float)", hasScalarImage);
		addImageOperatorPlugin(filtersMenu, new GaussianFilter5x5(), "Gaussian Filter 5x5", hasScalarImage && hasImage2D);
        addPlugin(filtersMenu, new ImageBoxMedianFilter(), "Median Filter...");
        addPlugin(filtersMenu, new ImageBoxMinMaxFilter(), "Min/Max Filter...");
        filtersMenu.addSeparator();
		addPlugin(filtersMenu, new ImageBoxVarianceFilter(), "Variance Filter...");
		menu.add(filtersMenu);
        
		// Gradient filters
        JMenu gradientFiltersMenu = new JMenu("Gradient Filters");
        addImageOperatorPlugin(gradientFiltersMenu, new SobelGradient(), "Sobel Gradient", hasScalarImage);
		addImageOperatorPlugin(gradientFiltersMenu, new SobelGradientNorm(), "Sobel Gradient Norm", hasScalarImage);
		addImageOperatorPlugin(gradientFiltersMenu, new VectorArrayNorm(), "Vector Image Norm", hasVectorImage);
		// addMenuItem(menu, new ImageOperatorAction(frame, "vectorAngle",
		// new VectorImageAngle()),
		// "Array<?> Angle", hasImage);
        menu.add(gradientFiltersMenu);
//		menu.addSeparator();
        
		JMenu morphologyMenu = new JMenu("Mathematical Morphology");
		addPlugin(morphologyMenu, new ImageMorphologicalFilter(), "Morphological Filters...");
//		addPlugin(morphologyMenu, new ImageMorphologicalFilter3D(), "Morphological Filters (3D)...");

		morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, new ImageRegionalExtrema(), "Regional Min./Max...", hasScalarImage);
		addPlugin(morphologyMenu, new ImageExtendedExtrema(), "Extended Min./Max...", hasScalarImage);
		addPlugin(morphologyMenu, new ImageMorphologicalReconstruction(), "Morphological Reconstruction...");
        addPlugin(morphologyMenu, new ImageIteratedGeodesicDilations(), "Geodesic Dilation...");
		morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, new ImageFillHoles(), "Fill Holes");
        addPlugin(morphologyMenu, new ImageKillBorders(), "Kill Borders");
		menu.add(morphologyMenu);
		
        menu.addSeparator();
        addPlugin(menu, new ImageFindNonZeroPixels(),
                "Find Non-Zeros Elements", hasImage2D && hasScalarImage);
        addPlugin(menu, new ImageIsocontour(), "Isocontour...");
		addPlugin(menu, new Image3DKymograph(), "Kymograph", hasImage3D && hasScalarImage);

        // operators specific to binary images
        menu.addSeparator();
        JMenu segmentationMenu = new JMenu("Segmentation");
		addPlugin(segmentationMenu, new ImageOtsuThreshold(), "Otsu Threshold", hasScalarImage);
        addPlugin(segmentationMenu, new ImageManualThreshold(), "Manual Threshold", hasScalarImage);
        segmentationMenu.addSeparator();
        addPlugin(segmentationMenu, new ImageWatershed(), "Watershed", hasScalarImage);
        menu.add(segmentationMenu);

        // operators specific to binary images
        menu.addSeparator();
		JMenu binaryMenu = new JMenu("Binary Images");
		addPlugin(binaryMenu, new BinaryImageConnectedComponentsLabeling(), "Connected Components Labeling");
		addPlugin(binaryMenu, new BinaryImageChamferDistanceMap(), "Distance Map", hasBinaryImage);
		addPlugin(binaryMenu, new ImageGeodesicDistanceMap(), "Geodesic Distance Map...");
        addPlugin(binaryMenu, new BinaryImageSkeleton(), "IJ Skeleton");
        addPlugin(binaryMenu, new BinaryImageOverlay(), "Binary Overlay...");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, new BinaryImageBoundaryGraph(),
                "Boundary Graph", hasImage2D && hasBinaryImage);
		menu.add(binaryMenu);
		
        
		return menu;
	}

	/**
	 * Creates the sub-menu for the "process" item in the main Menu bar.
	 */
	private JMenu createImageAnalyzeMenu()
	{
		JMenu menu = new JMenu("Analyze");

		addPlugin(menu, new ImageHistogram(), "Histogram", hasImage);
		addPlugin(menu, new ImageRoiHistogram(), "ROI Histogram", hasImage && hasImage2D);
        addPlugin(menu, new ImageMeanValue(), "Mean Value", hasImage);
        addPlugin(menu, new ColorImageBivariateHistograms(), "Bivariate Color Histograms", hasColorImage);
        addPlugin(menu, new ImageBivariateHistogram(), "Bivariate Histogram");
		menu.addSeparator();
		addPlugin(menu, new ImageLineProfile(), "Line Profile", hasImage);
        menu.addSeparator();
        addPlugin(menu, new ImagePlotChannels(), "Channel Profile", hasImage);

        menu.addSeparator();
        JMenu regions2dMenu = new JMenu("Regions (2D)");
        regions2dMenu.setEnabled(hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageEquivalentDisks(), "Regions Equivalent Disks", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageEquivalentEllipses(), "Regions Equivalent Ellipses", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageConvexHulls(), "Regions Conxex Hulls", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageMaxFeretDiameters(), "Regions Max. Feret Diameters", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageGeodesicDiameters(), "Regions Geodesic Diameters", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageAdjacencies(), "Regions Adjacencies", hasImage2D && hasLabelImage);
        menu.add(regions2dMenu);
        addPlugin(menu, new LabelImageBoundingBoxes(), "Bounding Boxes", (hasImage2D || hasImage3D) && hasLabelImage);
        addPlugin(menu, new LabelImageCentroids(), "Regions Centroids", (hasImage2D || hasImage3D) && hasLabelImage);

        menu.addSeparator();
        addPlugin(menu, new GrayLevelImageCooccurenceMatrix(), "Gray Level Co-Occurence Matrix", hasImage2D && hasScalarImage);
        
		return menu;
	}

    /**
     * Creates the sub-menu for the "File" item in the main menu bar.
     */
    private JMenu createTableFileMenu()
    {
        JMenu fileMenu = new JMenu("File");
        
        addPlugin(fileMenu, new OpenTable(), "Open Table...");
        
        fileMenu.addSeparator();
        addPlugin(fileMenu, new SaveTable(), "Save Table...");

        fileMenu.addSeparator();
        addPlugin(fileMenu, new TableScatterPlot(), "Scatter Plot...");
        addPlugin(fileMenu, new TableLinePlot(), "Line Plot...");

        fileMenu.addSeparator();
        addPlugin(fileMenu, new CloseCurrentFrame(), "Close", !(frame instanceof ImagoEmptyFrame));
        addPlugin(fileMenu, new QuitApplication(), "Quit");

        return fileMenu;
    }

    /**
     * Creates the sub-menu for the "Edit" item in the main menu bar.
     */
    private JMenu createTableEditMenu()
    {
        JMenu editMenu = new JMenu("Edit");
        
        addPlugin(editMenu, new TableSelectColumns(), "Select Columns");
        addPlugin(editMenu, new TableKeepNumericColumns(), "Keep Numeric Columns");
        addPlugin(editMenu, new MergeTablesByColumns(), "Merge Columns");
        editMenu.addSeparator();
        addPlugin(editMenu, new TransposeTable(), "Transpose");
        editMenu.addSeparator();
        addPlugin(editMenu, new FoldTableToImage(), "Fold Table to Image");
        editMenu.addSeparator();
        addPlugin(editMenu, new PrintTableToConsole(), "Print to Console");
        
        return editMenu;
    }

    /**
     * Creates the sub-menu for the "Process" item in the main menu bar.
     */
    private JMenu createTableProcessMenu()
    {
        JMenu processMenu = new JMenu("Process");
        
        addPlugin(processMenu, new TablePca(), "Principal Components Analysis");
        processMenu.addSeparator();
        addPlugin(processMenu, new TableKMeans(), "K-Means");
        
        return processMenu;
    }

    
    private JMenu createPluginsMenu()
    {
        JMenu menu = new JMenu("Plugins");
        
        JMenu devMenu = new JMenu("Developer");
        addPlugin(devMenu, new DisplayExceptionDialog(), "Show Demo Exception");
        menu.add(devMenu);
        menu.addSeparator();
        
        if (hasImage3D)
        {
        	JMenu crop3DMenu = new JMenu("Crop 3D");
        	addPlugin(crop3DMenu, new Crop3D_Initialize(), "Initialize Crop3D");
        	addPlugin(crop3DMenu, new Crop3D_LoadPolygonsFromJson(), "Load polygons from JSON...");
        	crop3DMenu.addSeparator();
        	addPlugin(crop3DMenu, new Crop3D_AddPolygon(), "Add polygon");
        	addPlugin(crop3DMenu, new Crop3D_InterpolatePolygons(), "Interpolate polygons");
        	crop3DMenu.addSeparator();
        	addPlugin(crop3DMenu, new Crop3D_SavePolygonsAsJson(), "Save polygons as JSON...");
        	addPlugin(crop3DMenu, new Crop3D_CropImage(), "Crop Image...");
        	menu.add(crop3DMenu);
        }
        
        return menu;
    }

//    private JMenu createDeveloperMenu()
//    {
//        JMenu menu = new JMenu("Developer");
//        addPlugin(menu, new DisplayExceptionDialog(), "Show Demo Exception");
//        return menu;
//    }

    private JMenu createHelpMenu()
	{
		JMenu menu = new JMenu("Help");
		addMenuItem(menu, null, "About...", true);
		return menu;
	}

	private JMenuItem addMenuItem(JMenu menu, ImagoAction action, String label,
			boolean enabled)
	{
		JMenuItem item = new JMenuItem(action);
		item.setText(label);
		item.setIcon(this.emptyIcon);
		item.setEnabled(enabled);
		menu.add(item);
		return item;
	}

    private JMenuItem addImageOperatorPlugin(JMenu menu, ImageOperator operator, String label, boolean enabled)
    {
        FramePlugin plugin = new ImageOperatorPlugin(operator);
        return addPlugin(menu, plugin, label, enabled);
    }

    private JMenuItem addArrayOperatorPlugin(JMenu menu, ArrayOperator operator, String label, boolean enabled)
    {
        FramePlugin plugin = new ImageArrayOperatorPlugin(operator);
        return addPlugin(menu, plugin, label, enabled);
    }

    private JMenuItem addPlugin(JMenu menu, FramePlugin plugin, String label)
    {
       return addPlugin(menu, plugin, label, plugin.isEnabled(frame));
    }

    private JMenuItem addPlugin(JMenu menu, FramePlugin plugin, String label, boolean enabled)
    {
        JMenuItem item = new JMenuItem(new RunPluginAction(frame, plugin));
        item.setText(label);
        item.setIcon(this.emptyIcon);
        item.setEnabled(enabled);
        menu.add(item);
        return item;
    }

	private void createEmptyIcon()
	{
		int width = 16;
		int height = 16;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				image.setRGB(x, y, 0x00FFFFFF);
		this.emptyIcon = new ImageIcon(image);
	}
}
