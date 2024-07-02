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
import imago.gui.frames.ImagoEmptyFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.tools.DrawBrushValueTool;
import imago.gui.image.tools.DrawValueTool;
import imago.gui.image.tools.FloodFillTool;
import imago.gui.image.tools.SelectLineSegmentTool;
import imago.gui.image.tools.SelectPolygonTool;
import imago.gui.image.tools.SelectPolylineTool;
import imago.gui.image.tools.SelectRectangleTool;
import imago.gui.image.tools.SelectionTool;
import imago.gui.table.TableFrame;
import imago.plugin.CloseCurrentFrame;
import imago.plugin.CloseWithChildren;
import imago.plugin.QuitApplication;
import imago.plugin.developer.DisplayExceptionDialog;
import imago.plugin.edit.ChangeCurrentTool;
import imago.plugin.edit.ChooseBrushRadius;
import imago.plugin.edit.ChooseBrushValue;
import imago.plugin.edit.DocClearShapes;
import imago.plugin.edit.PrintDocumentList;
import imago.plugin.edit.PrintFrameList;
import imago.plugin.edit.PrintWorkspaceContent;
import imago.plugin.edit.ZoomIn;
import imago.plugin.edit.ZoomOne;
import imago.plugin.edit.ZoomOut;
import imago.plugin.image.ImageArrayOperatorPlugin;
import imago.plugin.image.ImageOperatorPlugin;
import imago.plugin.image.analyze.ColorImageBivariateHistograms;
import imago.plugin.image.analyze.GrayLevelImageCooccurenceMatrix;
import imago.plugin.image.analyze.ImageHistogram;
import imago.plugin.image.analyze.ImageLineProfile;
import imago.plugin.image.analyze.ImageMeanValue;
import imago.plugin.image.analyze.ImagePlotChannels;
import imago.plugin.image.analyze.ImageRoiHistogram;
import imago.plugin.image.analyze.LabelImageAdjacencies;
import imago.plugin.image.analyze.LabelImageBoundingBoxes;
import imago.plugin.image.analyze.LabelImageCentroids;
import imago.plugin.image.analyze.LabelImageConvexHulls;
import imago.plugin.image.analyze.LabelImageEquivalentDisks;
import imago.plugin.image.analyze.LabelImageEquivalentEllipses;
import imago.plugin.image.analyze.LabelImageEquivalentEllipsoids;
import imago.plugin.image.analyze.LabelImageGeodesicDiameters;
import imago.plugin.image.analyze.LabelImageIntrinsicVolumes;
import imago.plugin.image.analyze.LabelImageMaxFeretDiameters;
import imago.plugin.image.analyze.LabelImageOrientedBoxes;
import imago.plugin.image.binary.ApplyBinaryMask;
import imago.plugin.image.binary.BinaryImage3DDepthMap;
import imago.plugin.image.binary.BinaryImageChamferDistanceMap;
import imago.plugin.image.binary.BinaryImageConnectedComponentsLabeling;
import imago.plugin.image.binary.BinaryImageFillHoles;
import imago.plugin.image.binary.BinaryImageKillBorders;
import imago.plugin.image.binary.BinaryImageMorphologicalFilter;
import imago.plugin.image.binary.BinaryImageMorphologicalReconstruction;
import imago.plugin.image.binary.BinaryImageOverlay;
import imago.plugin.image.binary.BinaryImageSkeleton;
import imago.plugin.image.binary.ImageGeodesicDistanceMap;
import imago.plugin.image.convert.ConvertChannelsToDimension;
import imago.plugin.image.convert.ConvertDimensionToChannels;
import imago.plugin.image.convert.ConvertImage3DToVectorImage;
import imago.plugin.image.convert.ConvertImageToBinary;
import imago.plugin.image.convert.ConvertImageToFloat32;
import imago.plugin.image.convert.ConvertImageToFloat64;
import imago.plugin.image.convert.ConvertImageToInt16;
import imago.plugin.image.convert.ConvertImageToInt32;
import imago.plugin.image.convert.ConvertImageToLabel;
import imago.plugin.image.convert.ConvertImageToUInt16;
import imago.plugin.image.convert.ConvertImageToUInt8;
import imago.plugin.image.convert.ConvertRGB8ImageToRGB16;
import imago.plugin.image.convert.ConvertStackToMovie;
import imago.plugin.image.convert.ConvertUInt8ImageToRGB;
import imago.plugin.image.convert.CreateScaledUInt8View;
import imago.plugin.image.convert.CreateVectorImageNorm;
import imago.plugin.image.convert.CreateVectorImageRGB8View;
import imago.plugin.image.convert.ScalarImagesColorDifference;
import imago.plugin.image.convert.VectorImageConvertToRGB;
import imago.plugin.image.convert.VectorImageToTable;
import imago.plugin.image.edit.CreateColorCubeImage3D;
import imago.plugin.image.edit.CreateDistanceToOctahedronImage3D;
import imago.plugin.image.edit.DisplayImagePair;
import imago.plugin.image.edit.ImageColorMapDisplay;
import imago.plugin.image.edit.ImageCopySelectionToWorkspace;
import imago.plugin.image.edit.ImageFillBox;
import imago.plugin.image.edit.ImageFillDisk;
import imago.plugin.image.edit.ImageFillEllipse;
import imago.plugin.image.edit.ImageFillEllipsoid;
import imago.plugin.image.edit.ImageSelectionToDistanceMap;
import imago.plugin.image.edit.ImageSelectionToMask;
import imago.plugin.image.edit.ImageSelectionToSceneGraph;
import imago.plugin.image.edit.ImageSetBackgroundColor;
import imago.plugin.image.edit.ImageSetColorMapFactory;
import imago.plugin.image.edit.ImageSetScale;
import imago.plugin.image.edit.ImageSetScaleFromLineSelection;
import imago.plugin.image.edit.ImportSelectionFromWorkspace;
import imago.plugin.image.edit.PrintImageInfos;
import imago.plugin.image.edit.PrintImageSceneGraph;
import imago.plugin.image.edit.PrintImageTiffTags;
import imago.plugin.image.edit.RefreshDisplay;
import imago.plugin.image.edit.RenameImage;
import imago.plugin.image.edit.SetImageDisplayRange;
import imago.plugin.image.edit.SetImageDisplayRangeToData;
import imago.plugin.image.edit.SetImageDisplayRangeToDataType;
import imago.plugin.image.edit.SetImageTypeToLabel;
import imago.plugin.image.edit.ShowSceneGraphTree;
import imago.plugin.image.edit.ToggleSceneGraphDisplay;
import imago.plugin.image.file.CreateNewImage;
import imago.plugin.image.file.ImportImageMetaImage;
import imago.plugin.image.file.ImportImageRawData;
import imago.plugin.image.file.ImportImageSeries;
import imago.plugin.image.file.ImportImageVgi;
import imago.plugin.image.file.OpenDemoImage;
import imago.plugin.image.file.OpenDemoStack;
import imago.plugin.image.file.OpenImage;
import imago.plugin.image.file.PrintImageFileTiffTags;
import imago.plugin.image.file.ReadImageTiff;
import imago.plugin.image.file.ReadTiffStackSlice;
import imago.plugin.image.file.ReadTiffVirtualImage3D;
import imago.plugin.image.file.SaveImageMetaImage;
import imago.plugin.image.process.BinaryImageBoxMedianFilter;
import imago.plugin.image.process.BinaryImageSplitCoalescentParticles;
import imago.plugin.image.process.BoxFilter3x3FloatPlugin;
import imago.plugin.image.process.ColorImageExtractChannel;
import imago.plugin.image.process.Image3DKymograph;
import imago.plugin.image.process.Image3DSetOrthoSlicesDisplay;
import imago.plugin.image.process.ImageApplyMathFunction;
import imago.plugin.image.process.ImageApplyMathBinaryOperator;
import imago.plugin.image.process.ImageApplyLogicalBinaryOperator;
import imago.plugin.image.process.ImageBivariateHistogram;
import imago.plugin.image.process.ImageBoxFilter;
import imago.plugin.image.process.ImageMedianFilterBox;
import imago.plugin.image.process.ImageMinMaxFilterBox;
import imago.plugin.image.process.ImageVarianceFilterBox;
import imago.plugin.image.process.ImageDuplicate;
import imago.plugin.image.process.ImageExtendedExtrema;
import imago.plugin.image.process.ImageFillHoles;
import imago.plugin.image.process.ImageHysteresisThreshold;
import imago.plugin.image.process.ImageImposeExtrema;
import imago.plugin.image.process.ImageIteratedGeodesicDilations;
import imago.plugin.image.process.ImageKMeansSegmentation;
import imago.plugin.image.process.ImageKillBorders;
import imago.plugin.image.process.ImageManualThreshold;
import imago.plugin.image.process.ImageMarkerControlledWatershed;
import imago.plugin.image.process.ImageMorphologicalFilter;
import imago.plugin.image.process.ImageMorphologicalReconstruction;
import imago.plugin.image.process.ImageOtsuThreshold;
import imago.plugin.image.process.ImagePair2DRegister;
import imago.plugin.image.process.ImageRegionalExtrema;
import imago.plugin.image.process.ImageSplitChannels;
import imago.plugin.image.process.ImageApplySingleValueOperator;
import imago.plugin.image.process.ImageWatershed;
import imago.plugin.image.process.MergeChannelImages;
import imago.plugin.image.process.VectorImageChannelView;
import imago.plugin.image.shape.Image3DGetCurrentSlice;
import imago.plugin.image.shape.Image3DGetSlice;
import imago.plugin.image.shape.Image3DOrthoslicesMontage;
import imago.plugin.image.shape.Image3DRotate90;
import imago.plugin.image.shape.Image3DSliceMontage;
import imago.plugin.image.shape.ImageConcatenate;
import imago.plugin.image.shape.ImageCropDialog;
import imago.plugin.image.shape.ImageCropSelection;
import imago.plugin.image.shape.ImageDownsample;
import imago.plugin.image.shape.ImageAddBorders;
import imago.plugin.image.shape.ImageFlip;
import imago.plugin.image.shape.ImageOrthogonalProjection;
import imago.plugin.image.shape.ImagePermuteDims;
import imago.plugin.image.shape.ImageReshape;
import imago.plugin.image.shape.ImageRotateAroundCenter;
import imago.plugin.image.shape.ImageRotatedCrop;
import imago.plugin.image.shape.ImageSubsample;
import imago.plugin.image.vectorize.BinaryImageBoundaryGraph;
import imago.plugin.image.vectorize.ExportIsosurface;
import imago.plugin.image.vectorize.Image3DIsosurface;
import imago.plugin.image.vectorize.ImageFindNonZeroPixels;
import imago.plugin.image.vectorize.ImageIsocontour;
import imago.plugin.image.vectorize.LabelMapToBoundaryPolygons;
import imago.plugin.plugin.ImportImage3DPolylineSeries;
import imago.plugin.plugin.crop.CreateSurface3DPlugin;
import imago.plugin.plugin.crop.Crop3DPlugin;
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
import net.sci.array.color.ColorMaps;
import net.sci.array.color.RGB8Array;
import net.sci.array.shape.Rotate90;
import net.sci.image.Image;
import net.sci.image.ImageOperator;
import net.sci.image.process.DynamicAdjustment;
import net.sci.image.process.ImageInverter;
import net.sci.image.process.SobelGradient;
import net.sci.image.process.SobelGradientNorm;
import net.sci.image.process.VectorArrayNorm;
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
    		menuBar.add(createImageProcessMenu());
    		menuBar.add(createImageAnalyzeMenu());
            menuBar.add(createImageToolsMenu());
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

		// Import demo images
		JMenu demoMenu = new JMenu("Demo Images");
        addPlugin(demoMenu, new OpenDemoImage("files/grains.png"), "Rice grains");
        addPlugin(demoMenu, new OpenDemoImage("files/lena_gray_512.tif"), "Lena");
		addPlugin(demoMenu, new OpenDemoImage("files/sunflower.png"), "Sunflower");
		addPlugin(demoMenu, new OpenDemoStack(), "Demo Stack");
        addPlugin(demoMenu, new CreateDistanceToOctahedronImage3D(), "Octahedron Distance Map");
		addPlugin(demoMenu, new CreateColorCubeImage3D(), "3D Color Cube");
        fileMenu.add(demoMenu);

        // Import less common file formats
        JMenu tiffFileMenu = new JMenu("Tiff Files");
        addPlugin(tiffFileMenu, new ReadImageTiff(), "Read TIFF...");
        addPlugin(tiffFileMenu, new ReadTiffVirtualImage3D(), "Read TIFF Virtual Image 3D...");
        addPlugin(tiffFileMenu, new ReadTiffStackSlice(), "Read TIFF Slice...");
        tiffFileMenu.addSeparator();
        addPlugin(tiffFileMenu, new PrintImageFileTiffTags(), "Print Tiff File Tags...");
        fileMenu.add(tiffFileMenu);
        
		// Import less common file formats
		JMenu fileImportMenu = new JMenu("Import");
		addPlugin(fileImportMenu, new ImportImageRawData(), "Raw Data...");
        addPlugin(fileImportMenu, new ImportImageSeries(), "Import Image Series...");
        fileImportMenu.addSeparator();
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

        // selection sub-menu
        JMenu selectionMenu = new JMenu("Selection");
        addPlugin(selectionMenu, new ImageCopySelectionToWorkspace(), "Copy To Workspace");
        addPlugin(selectionMenu, new ImportSelectionFromWorkspace(), "Import From Workspace");
        editMenu.add(selectionMenu);

        // crop tools
        addPlugin(editMenu, new ImageCropSelection(), "Crop Selection", hasImage2D);
        addPlugin(editMenu, new ImageCropDialog(), "Crop...", hasImage);
        
        // zoom management
        editMenu.addSeparator();
        addPlugin(editMenu, new ZoomIn(), "Zoom In", hasImage);
        addPlugin(editMenu, new ZoomOut(), "Zoom Out", hasImage);
        addPlugin(editMenu, new ZoomOne(), "Zoom One", hasImage);
        addPlugin(editMenu, new RefreshDisplay(), "Refresh Display", hasImage);
        
        // add utility
		editMenu.addSeparator();
		JMenu sceneGraphMenu = new JMenu("Scene Graph");
        addPlugin(sceneGraphMenu, new ImageSelectionToSceneGraph(), "Add Selection to scene graph");
		addPlugin(sceneGraphMenu, new ShowSceneGraphTree(), "Display Scene Graph Tree");
		addPlugin(sceneGraphMenu, new PrintImageSceneGraph(), "Print SceneGraph Tree");
        addPlugin(sceneGraphMenu, new ToggleSceneGraphDisplay(), "Toggle Scene Graph Display");
		editMenu.add(sceneGraphMenu);
		
		addPlugin(editMenu, new DocClearShapes(), "Clear Shapes");
        
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

		// First general info and calibration about images
        addPlugin(menu, new RenameImage(), "Rename...", hasImage);
        addPlugin(menu, new PrintImageInfos(), "Print Image Info", hasImage);
        addPlugin(menu, new ImageSetScale(), "Image Scale...", hasImage);
        addPlugin(menu, new PrintImageTiffTags(), "Show TIFF Tags", hasImage);
        

        // Management of image representation
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
        colormapMenu.addSeparator();
        addPlugin(colormapMenu, new ImageColorMapDisplay(), "Show Color Map in Table", hasImage && !hasRGB8Image);
        menu.add(colormapMenu);
        
        addPlugin(menu, new ImageSetBackgroundColor(), "Set Background Color...", hasLabelImage);
        addPlugin(menu, new DisplayImagePair(), "Display Image Pair", hasImage);

        // Several options for converting images
        menu.addSeparator();
        
        JMenu convertTypeMenu = new JMenu("Convert Type");
        convertTypeMenu.setEnabled(hasImage);
        addPlugin(convertTypeMenu, new ConvertImageToBinary(), "Binary");
        addPlugin(convertTypeMenu, new ConvertImageToUInt8(), "UInt8");
        addPlugin(convertTypeMenu, new CreateScaledUInt8View(), "UInt8 (change dynamic)", hasScalarImage);
        addPlugin(convertTypeMenu, new ConvertImageToUInt16(), "UInt16", hasScalarImage);
        convertTypeMenu.addSeparator();
        addPlugin(convertTypeMenu, new ConvertImageToLabel(), "Label");
        addPlugin(convertTypeMenu, new SetImageTypeToLabel(), "Set to Label Image", hasScalarImage);
        convertTypeMenu.addSeparator();
        convertTypeMenu.addSeparator();
        addPlugin(convertTypeMenu, new ConvertImageToInt16(), "Int16", hasScalarImage);
        addPlugin(convertTypeMenu, new ConvertImageToInt32(), "Int32", hasScalarImage);
        convertTypeMenu.addSeparator();
        addPlugin(convertTypeMenu, new ConvertImageToFloat32(), "Float32", hasImage);
        addPlugin(convertTypeMenu, new ConvertImageToFloat64(), "Float64", hasImage);
        menu.add(convertTypeMenu);
        
        // image type conversion handles
        addPlugin(menu, new ConvertStackToMovie(), "Convert stack to movie", hasImage3D);

        // Color images operators
        menu.addSeparator();
        JMenu colorMenu = new JMenu("Color");
        // editMenu.add(convertTypeMenu);
        addPlugin(colorMenu, new ImageSplitChannels(), "Split Channels", hasVectorImage || hasColorImage);
        addPlugin(colorMenu, new MergeChannelImages(), "Merge Channels");
        addPlugin(colorMenu, new ColorImageExtractChannel(), "Select Channel...", hasColorImage);
        addPlugin(colorMenu, new ConvertUInt8ImageToRGB(), "UInt8 to RGB8", hasScalarImage);
        addPlugin(colorMenu, new ConvertRGB8ImageToRGB16(), "RGB8 to RGB16", hasRGB8Image);
        addPlugin(colorMenu, new ScalarImagesColorDifference(), "Color difference between two scalar images");
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
        addPlugin(geometryMenu, new ImageRotateAroundCenter(), "Rotate...", hasImage2D);
        addPlugin(geometryMenu, new Image3DRotate90(), "Rotate 3D by 90 degrees...", hasImage3D);
        addPlugin(geometryMenu, new ImageRotatedCrop(), "Rotated Crop...", hasImage2D || hasImage3D);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, new ImageReshape(), "Reshape Image...", hasImage);
        addPlugin(geometryMenu, new ImagePermuteDims(), "Permute Image Dimensions...", hasImage);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, new ImageConcatenate(), "Concatenate..."); 
        addPlugin(geometryMenu, new ImageAddBorders(), "Add Borders...", hasScalarImage); 
        addPlugin(geometryMenu, new ImageSubsample(), "Subsample...", hasImage);
        addPlugin(geometryMenu, new ImageDownsample(), "Downsample...", hasImage);
        
        menu.add(geometryMenu);
        
        // Create the menu for 3D images
        JMenu stackMenu = new JMenu("3D Images");
        stackMenu.setEnabled(hasImage3D);
        addPlugin(stackMenu, new Image3DGetCurrentSlice(), "Extract Current Slice", hasImage3D);
        addPlugin(stackMenu, new Image3DGetSlice(), "Extract Slice...", hasImage3D);
        addPlugin(stackMenu, new Image3DOrthoslicesMontage(), "OrthoSlices Montage...", hasImage3D);
        addPlugin(stackMenu, new Image3DSliceMontage(), "Image 3D Montage...", hasImage3D);
        addPlugin(stackMenu, new ImageOrthogonalProjection(), "Orthogonal Projection...", hasImage3D && hasScalarImage);
        stackMenu.addSeparator();
        addPlugin(stackMenu, new Image3DSetOrthoSlicesDisplay(), "Set Orthoslices Display", hasImage3D);
        stackMenu.addSeparator();
		addPlugin(stackMenu, new ConvertImage3DToVectorImage(), "Convert Z-Dim To Vector", hasImage3D);
		menu.add(stackMenu);

        menu.addSeparator();
		addPlugin(menu, new ImageDuplicate(), "Duplicate", hasImage);
		addArrayOperatorPlugin(menu, new ImageInverter(), "Invert", "%s-inv");
        
        // submenu for creation of phantoms
        JMenu phantomMenu = new JMenu("Phantoms");
        addPlugin(phantomMenu, new ImageFillDisk(), "Fill Disk...");
        addPlugin(phantomMenu, new ImageFillEllipse(), "Fill Ellipse...");
        addPlugin(phantomMenu, new ImageFillBox(), "Fill Box...");
        addPlugin(phantomMenu, new ImageFillEllipsoid(), "Fill Ellipsoid...");
        phantomMenu.addSeparator();
        addPlugin(phantomMenu, new ImageSelectionToMask(), "Selection To Mask");
        addPlugin(phantomMenu, new ImageSelectionToDistanceMap(), "Selection To Distance Map");
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
        addPlugin(mathsMenu, new ImageApplyMathFunction(), "Apply Function...", hasScalarImage);
        addPlugin(mathsMenu, new ImageApplySingleValueOperator(), "Math operator (image+value)...");
        addPlugin(mathsMenu, new ImageApplyMathBinaryOperator(), "Math operator (Image pair)...");
        mathsMenu.addSeparator();
        addPlugin(mathsMenu, new ImageApplyLogicalBinaryOperator(), "Logical operator (Image pair)...");
		menu.add(mathsMenu);
		menu.addSeparator();

		// Noise reduction filters
        JMenu filtersMenu = new JMenu("Filters");
        addPlugin(filtersMenu, new ImageBoxFilter(), "Box Filter...");
		addPlugin(filtersMenu, new BoxFilter3x3FloatPlugin(), "Box Filter 2D 3x3 (float)", hasScalarImage);
		addArrayOperatorPlugin(filtersMenu, new GaussianFilter5x5(), "Gaussian Filter 5x5", hasScalarImage && hasImage2D);
        addPlugin(filtersMenu, new ImageMedianFilterBox(), "Median Filter...");
        addPlugin(filtersMenu, new BinaryImageBoxMedianFilter(), "Binary Median Filter...");
        addPlugin(filtersMenu, new ImageMinMaxFilterBox(), "Min/Max Filter...");
        filtersMenu.addSeparator();
		addPlugin(filtersMenu, new ImageVarianceFilterBox(), "Variance Filter...");
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
        addPlugin(morphologyMenu, new BinaryImageMorphologicalFilter(), "Binary Morphological Filters...");

		morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, new ImageRegionalExtrema(), "Regional Min./Max...", hasScalarImage);
        addPlugin(morphologyMenu, new ImageExtendedExtrema(), "Extended Min./Max...", hasScalarImage);
        addPlugin(morphologyMenu, new ImageImposeExtrema(), "Impose Min./Max...", hasScalarImage);
		addPlugin(morphologyMenu, new ImageMorphologicalReconstruction(), "Morphological Reconstruction...");
        addPlugin(morphologyMenu, new BinaryImageMorphologicalReconstruction(), "Binary Morphological Reconstruction...");
        addPlugin(morphologyMenu, new ImageIteratedGeodesicDilations(), "Geodesic Dilation...");
		morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, new ImageFillHoles(), "Fill Holes");
        addPlugin(morphologyMenu, new BinaryImageFillHoles(), "Binary Fill Holes");
        addPlugin(morphologyMenu, new ImageKillBorders(), "Kill Borders");
        addPlugin(morphologyMenu, new BinaryImageKillBorders(), "Binary Kill Borders");
        menu.add(morphologyMenu);
		
        menu.addSeparator();
        addPlugin(menu, new ImagePair2DRegister(), "Simple Image Registration", hasScalarImage);
        
        menu.addSeparator();
        addPlugin(menu, new ImageFindNonZeroPixels(),
                "Find Non-Zeros Elements", hasImage2D && hasScalarImage);
        addPlugin(menu, new ImageIsocontour(), "Isocontour...");
        addPlugin(menu, new ExportIsosurface(), "Export Isosurface...");
        addPlugin(menu, new Image3DIsosurface(), "Compute Isosurface...");
        addPlugin(menu, new Image3DKymograph(), "Kymograph", hasImage3D && hasScalarImage);

        // operators specific to binary images
        menu.addSeparator();
        JMenu segmentationMenu = new JMenu("Segmentation");
		addPlugin(segmentationMenu, new ImageOtsuThreshold(), "Otsu Threshold", hasScalarImage);
        addPlugin(segmentationMenu, new ImageManualThreshold(), "Manual Threshold", hasScalarImage);
        addPlugin(segmentationMenu, new ImageHysteresisThreshold(), "Hysteresis Threshold", hasScalarImage);
        segmentationMenu.addSeparator();
        addPlugin(segmentationMenu, new ImageKMeansSegmentation(), "K-Means Segmentation", hasImage);
        segmentationMenu.addSeparator();
        addPlugin(segmentationMenu, new ImageWatershed(), "Watershed", hasScalarImage);
        addPlugin(segmentationMenu, new ImageMarkerControlledWatershed(), "Marker-Based Watershed", hasScalarImage);
        menu.add(segmentationMenu);

        // operators specific to binary images
        menu.addSeparator();
		JMenu binaryMenu = new JMenu("Binary Images");
		addPlugin(binaryMenu, new BinaryImageConnectedComponentsLabeling(), "Connected Components Labeling");
        binaryMenu.addSeparator();
		addPlugin(binaryMenu, new BinaryImageChamferDistanceMap(), "Distance Map", hasBinaryImage);
        addPlugin(binaryMenu, new BinaryImage3DDepthMap(), "3D Binary Image Depth Map");
		addPlugin(binaryMenu, new ImageGeodesicDistanceMap(), "Geodesic Distance Map...");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, new BinaryImageSkeleton(), "IJ Skeleton");
        addPlugin(binaryMenu, new BinaryImageSplitCoalescentParticles(), "Split Particles...");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, new BinaryImageOverlay(), "Binary Overlay...");
        addPlugin(binaryMenu, new ApplyBinaryMask(), "Apply Binary Mask...");
        menu.add(binaryMenu);
        
        // operators specific to binary images
        JMenu labelMenu = new JMenu("Binary / Label Images");
        addPlugin(labelMenu, new BinaryImageBoundaryGraph(),
                "Boundary Graph", hasImage2D && hasBinaryImage);
        addPlugin(labelMenu, new LabelMapToBoundaryPolygons(),
                "Region Boundaries to Polygons", hasImage2D && hasLabelImage);
        menu.add(labelMenu);
		
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
        addPlugin(regions2dMenu, new LabelImageIntrinsicVolumes(), "Regions Intrinsic Volumes", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageEquivalentDisks(), "Regions Equivalent Disks", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageEquivalentEllipses(), "Regions Equivalent Ellipses", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageOrientedBoxes(), "Regions Oriented Boxes", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageConvexHulls(), "Regions Conxex Hulls", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageMaxFeretDiameters(), "Regions Max. Feret Diameters", hasImage2D && hasLabelImage);
        addPlugin(regions2dMenu, new LabelImageGeodesicDiameters(), "Regions Geodesic Diameters", hasImage2D && hasLabelImage);
        regions2dMenu.addSeparator();
        addPlugin(regions2dMenu, new LabelImageAdjacencies(), "Regions Adjacencies", hasImage2D && hasLabelImage);
        menu.add(regions2dMenu);

        JMenu regions3dMenu = new JMenu("Regions (3D)");
        regions3dMenu.setEnabled(hasImage3D && hasLabelImage);
        addPlugin(regions3dMenu, new LabelImageEquivalentEllipsoids(), "Regions Equivalent Ellipsoids", hasImage3D && hasLabelImage);
        menu.add(regions3dMenu);

        addPlugin(menu, new LabelImageBoundingBoxes(), "Bounding Boxes", (hasImage2D || hasImage3D) && hasLabelImage);
        addPlugin(menu, new LabelImageCentroids(), "Regions Centroids", (hasImage2D || hasImage3D) && hasLabelImage);

        menu.addSeparator();
        addPlugin(menu, new GrayLevelImageCooccurenceMatrix(), "Gray Level Co-Occurence Matrix", hasImage2D && hasScalarImage);
        
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
                addPlugin(toolsMenu, 
                        new ChangeCurrentTool(new SelectPolylineTool(viewer, "selectPolyline")),
                        "Select Polyline", hasImage);
    
                toolsMenu.addSeparator();
                addPlugin(toolsMenu, 
                        new ChangeCurrentTool(new DrawValueTool(viewer, "drawValue")),
                        "Draw (Dot)", hasScalarImage);
                addPlugin(toolsMenu, 
                        new ChangeCurrentTool(new DrawBrushValueTool(viewer, "drawBrushValue")),
                        "Draw (Brush)", hasScalarImage);
                addPlugin(toolsMenu, 
                        new ChangeCurrentTool(new FloodFillTool(viewer, "floodFillValue")),
                        "Flood-Fill", hasScalarImage);
    //            addPlugin(editMenu, 
    //                    new ChangeCurrentTool(new DrawValueTool(viewer, "drawBlack", 0.0)),
    //                    "Draw Black", hasScalarImage);
    
                toolsMenu.addSeparator();
                addPlugin(toolsMenu, new ImageSetScaleFromLineSelection(), "Set Scale from Selection...", hasImage);
    		}
    
    		return toolsMenu;
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
        menu.add(devMenu);
        addPlugin(devMenu, new DisplayExceptionDialog(), "Show Demo Exception");
        addPlugin(devMenu, new PrintFrameList(), "Print Frame List");
        addPlugin(devMenu, new PrintDocumentList(), "Print Document List");
        addPlugin(devMenu, new PrintWorkspaceContent(), "Print Workspace Content");
        
        // Add some domain-specific plugins, to be transformed into user plugins in the future
        menu.addSeparator();
        JMenu perigrainMenu = new JMenu("Perigrain");
        addPlugin(perigrainMenu, new Crop3DPlugin(), "Crop 3D");
        addPlugin(perigrainMenu, new CreateSurface3DPlugin(), "Surface 3D");
        addPlugin(perigrainMenu, new ImportImage3DPolylineSeries(), "Import Polyline Series");
        menu.add(perigrainMenu);
        
        // Add the user plugins
        menu.addSeparator();
        for (PluginHandler handler : frame.gui.pluginHandlers)
        {
            addPlugin(menu, handler);
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

    private JMenuItem addArrayOperatorPlugin(JMenu menu, ArrayOperator operator, String label, String newNamePattern)
    {
        FramePlugin plugin = new ImageArrayOperatorPlugin(operator, label, newNamePattern);
        return addPlugin(menu, plugin, label, plugin.isEnabled(frame));
    }

    private void addPlugin(JMenu menu, PluginHandler handler)
    {
        System.out.println("add plugin entry: " + handler.getName());
        
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
        item.setMargin(new Insets(0, 0, 0, 0));
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
