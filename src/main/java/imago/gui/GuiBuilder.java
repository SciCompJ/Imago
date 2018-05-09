/**
 * 
 */
package imago.gui;

import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import imago.app.ImagoDoc;
import imago.gui.action.ArrayOperatorAction;
import imago.gui.action.RunPluginAction;
import imago.gui.action.SelectToolAction;
import imago.gui.action.edit.DocClearShapesAction;
import imago.gui.action.edit.ImageFillDiskAction;
import imago.gui.action.edit.ImageFillEllipseAction;
import imago.gui.action.edit.ImageSelectionToDistanceMapAction;
import imago.gui.action.edit.ImageSelectionToMaskAction;
import imago.gui.action.edit.PrintDocumentListAction;
import imago.gui.action.edit.PrintFrameListAction;
import imago.gui.action.edit.ZoomInAction;
import imago.gui.action.edit.ZoomOneAction;
import imago.gui.action.edit.ZoomOutAction;
import imago.gui.action.file.CloseFrameAction;
import imago.gui.action.file.CreateColorCubeStack;
import imago.gui.action.file.CreateDistanceToOctahedronStack;
import imago.gui.action.file.CreateEmptyImageAction;
import imago.gui.action.file.ImportMetaImageFileAction;
import imago.gui.action.file.ImportRawDataAction;
import imago.gui.action.file.ImportVgiImageAction;
import imago.gui.action.file.OpenDemoImage;
import imago.gui.action.file.OpenDemoStack;
import imago.gui.action.file.OpenImageAction;
import imago.gui.action.file.OpenTableAction;
import imago.gui.action.file.QuitAction;
import imago.gui.action.file.ReadTiffAction;
import imago.gui.action.file.SaveTableAction;
import imago.gui.action.file.ShowDemoTable;
import imago.gui.action.file.TableScatterPlotAction;
import imago.gui.action.image.SetImageTypeToLabelAction;
import imago.gui.action.process.BinaryImageOverlayAction;
import imago.gui.action.process.BoxFilter3x3Float;
import imago.gui.tool.SelectLineSegmentTool;
import imago.gui.tool.SelectPolygonTool;
import imago.gui.tool.SelectionTool;
import imago.plugin.image.ImageArrayOperatorPlugin;
import imago.plugin.image.ImageOperatorPlugin;
import imago.plugin.image.analyze.ColorImageBivariateHistograms;
import imago.plugin.image.analyze.ImageHistogram;
import imago.plugin.image.analyze.ImageLineProfile;
import imago.plugin.image.analyze.ImageMeanValue;
import imago.plugin.image.analyze.ImageRoiHistogram;
import imago.plugin.image.analyze.LabelImageBoundingBoxes;
import imago.plugin.image.analyze.LabelImageCentroids;
import imago.plugin.image.analyze.LabelImageEquivalentDisks;
import imago.plugin.image.analyze.LabelImageInertiaEllipses;
import imago.plugin.image.convert.ConvertImage3DToVectorImage;
import imago.plugin.image.convert.ConvertImageToBinary;
import imago.plugin.image.convert.ConvertImageToFloat32;
import imago.plugin.image.convert.ConvertImageToFloat64;
import imago.plugin.image.convert.ConvertImageToInt16;
import imago.plugin.image.convert.ConvertImageToInt32;
import imago.plugin.image.convert.ConvertImageToUInt16;
import imago.plugin.image.convert.ConvertImageToUInt8;
import imago.plugin.image.convert.ConvertRGB8ImageToUInt8;
import imago.plugin.image.edit.ImageSetBackgroundColor;
import imago.plugin.image.edit.ImageSetColorMapFactory;
import imago.plugin.image.edit.ImageSetScale;
import imago.plugin.image.edit.PrintImageInfos;
import imago.plugin.image.edit.PrintImageTiffTags;
import imago.plugin.image.edit.SetImageDisplayRange;
import imago.plugin.image.edit.SetImageDisplayRangeToData;
import imago.plugin.image.edit.SetImageDisplayRangeToDataType;
import imago.plugin.image.process.BinaryImageConnectedComponentsLabeling;
import imago.plugin.image.process.BinaryImageSkeleton;
import imago.plugin.image.process.BoxFilter;
import imago.plugin.image.process.ColorImageExtractChannel;
import imago.plugin.image.process.Image3DGetCurrentSlice;
import imago.plugin.image.process.Image3DGetSlice;
import imago.plugin.image.process.Image3DOrthoslicesImage;
import imago.plugin.image.process.Image3DSetOrthoSlicesDisplay;
import imago.plugin.image.process.ImageBoxMedianFilter;
import imago.plugin.image.process.ImageBoxMinMaxFilter;
import imago.plugin.image.process.ImageBoxVarianceFilter;
import imago.plugin.image.process.ImageDownsample;
import imago.plugin.image.process.ImageDuplicate;
import imago.plugin.image.process.ImageExtendedExtrema;
import imago.plugin.image.process.ImageFillHoles;
import imago.plugin.image.process.ImageFlip;
import imago.plugin.image.process.ImageGeodesicDistanceMap;
import imago.plugin.image.process.ImageKillBorders;
import imago.plugin.image.process.ImageManualThreshold;
import imago.plugin.image.process.ImageMorphologicalFilter;
import imago.plugin.image.process.ImageMorphologicalReconstruction;
import imago.plugin.image.process.ImageOtsuThreshold;
import imago.plugin.image.process.ImageReshape;
import imago.plugin.image.process.ImageRotateAroundCenter;
import imago.plugin.image.process.ImageSplitChannels;
import imago.plugin.image.process.MergeChannelImages;
import imago.plugin.image.vectorize.BinaryImageBoundaryGraph;
import imago.plugin.image.vectorize.ImageFindNonZeroPixels;
import imago.plugin.image.vectorize.ImageIsocontour;
import net.sci.array.Array;
import net.sci.array.ArrayOperator;
import net.sci.array.data.color.RGB8Array;
import net.sci.array.process.PowerOfTwo;
import net.sci.array.process.Sqrt;
import net.sci.array.process.shape.Rotate90;
import net.sci.image.ColorMaps;
import net.sci.image.Image;
import net.sci.image.ImageOperator;
import net.sci.image.binary.ChamferWeights2D;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DFloat;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DUInt16;
import net.sci.image.data.Connectivity2D;
import net.sci.image.morphology.MinimaAndMaxima;
import net.sci.image.morphology.extrema.RegionalExtrema2D;
import net.sci.image.process.DynamicAdjustment;
import net.sci.image.process.ImageInverter;
import net.sci.image.process.SobelGradient;
import net.sci.image.process.SobelGradientNorm;
import net.sci.image.process.VectorArrayNorm;

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
		if (frame instanceof ImagoDocViewer || frame instanceof ImagoEmptyFrame)
		{
	        menuBar.add(createImageFileMenu());
    		menuBar.add(createImageEditMenu());
    		menuBar.add(createImageMenu());
    		menuBar.add(createImageProcessMenu());
    		menuBar.add(createImageAnalyzeMenu());
		}
		else if (frame instanceof ImagoTableFrame)
		{
		    menuBar.add(createTableFileMenu());
		}
		menuBar.add(createHelpMenu());

		frame.getWidget().setJMenuBar(menuBar);
	}

	private void computeFlags()
	{
	    ImagoDoc doc = null;
        if (frame instanceof ImagoDocViewer)
        {
            doc = ((ImagoDocViewer) frame).getDocument();

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
            this.hasVectorImage = array instanceof RGB8Array;
        }
	}
	
	/**
	 * Creates the sub-menu for the "File" item in the main menu bar.
     */
    private JMenu createTableFileMenu()
    {
        JMenu fileMenu = new JMenu("File");
        
        addMenuItem(fileMenu, new OpenTableAction(frame, "openTable"), "Open Table...");
        
        fileMenu.addSeparator();
        addMenuItem(fileMenu, new SaveTableAction(frame, "saveTable"), "Save Table...");

        fileMenu.addSeparator();
        addMenuItem(fileMenu, new TableScatterPlotAction(frame, "scatterPlot"), "Scatter Plot...");

        fileMenu.addSeparator();
        addMenuItem(fileMenu, new CloseFrameAction(frame, "close"), "Close", !(frame instanceof ImagoEmptyFrame));
        addMenuItem(fileMenu, new QuitAction(frame, "quit"), "Quit");

        return fileMenu;
    }

	/**
	 * Creates the sub-menu for the "File" item in the main menu bar.
	 */
	private JMenu createImageFileMenu()
	{
		JMenu fileMenu = new JMenu("File");
		addMenuItem(fileMenu, new CreateEmptyImageAction(frame, "createEmptyImage"), 
				"New...");
		addMenuItem(fileMenu, new OpenImageAction(frame, "openImage"),
				"Open...");
		addMenuItem(fileMenu, new ReadTiffAction(frame, "readTiffImage"),
				"Read TIFF...");

		// Import demo images
		JMenu demoMenu = new JMenu("Open Demo Image");
        addMenuItem(demoMenu, new OpenDemoImage(frame, "openDemoRice",
                "files/grains.png"), "Rice grains");
		addMenuItem(demoMenu, new OpenDemoImage(frame, "openDemoLena",
				"files/lena_gray_512.tif"), "Lena");
		addMenuItem(demoMenu, new OpenDemoImage(frame, "openDemoSunflower",
				"files/sunflower.png"), "Sunflower");
        addMenuItem(demoMenu, new OpenDemoStack(frame, "openDemoStack"),
                "Demo Stack");
        addMenuItem(demoMenu, new CreateDistanceToOctahedronStack(frame, "octahedronDistanceMap"),
                "Octahedron Distance Map");
		addMenuItem(demoMenu, new CreateColorCubeStack(frame,
				"createDemoColorCube"), "3D Color Cube");
        fileMenu.add(demoMenu);

		// Import less common file formats
		JMenu fileImportMenu = new JMenu("Import");
		addMenuItem(fileImportMenu, new ImportRawDataAction(frame,
		        "importRawData"), "Raw Data...");
		addMenuItem(fileImportMenu, new ImportMetaImageFileAction(frame,
		        "readMetaImageFormat"), "MetaImage Data...");
		addMenuItem(fileImportMenu, new ImportVgiImageAction(frame,
		        "importVgiImage"), "VGI Image...");
		fileMenu.add(fileImportMenu);

//		addMenuItem(demoMenu, new CreateWhiteNoiseImageAction(frame,
//				"createWhiteNoiseImage"), "White Noise Array<?>");
        fileMenu.addSeparator();
        addMenuItem(fileMenu, new OpenTableAction(frame, "openTable"), "Open Table...");
        addMenuItem(fileMenu, new ShowDemoTable(frame, "showDemoTableFrame"), "Show Demo Table");
        
		fileMenu.addSeparator();
		addMenuItem(fileMenu, new CloseFrameAction(frame, "close"), "Close", !(frame instanceof ImagoEmptyFrame));
		addMenuItem(fileMenu, new QuitAction(frame, "quit"), "Quit");
		return fileMenu;
	}

	/**
	 * Creates the sub-menu for the "Edit" item in the main menu bar.
	 */
	private JMenu createImageEditMenu()
	{
		JMenu editMenu = new JMenu("Edit");

		// tool selection items
		if (frame instanceof ImagoDocViewer)
		{
			ImagoTool tool;
			ImagoDocViewer viewer = (ImagoDocViewer) frame;

			tool = new SelectionTool(viewer, "select");
			addMenuItem(editMenu, new SelectToolAction(viewer, tool), "Select",
					hasImage);

            addMenuItem(editMenu, 
                    new SelectToolAction(viewer, new SelectLineSegmentTool(viewer, "selectLineSegment")),
                    "Select Line", hasImage);
            addMenuItem(editMenu, 
                    new SelectToolAction(viewer, new SelectPolygonTool(viewer, "selectPolygon")),
                    "Select Polygon", hasImage);

			editMenu.addSeparator();
		}

		// zoom items
		addMenuItem(editMenu, new ZoomInAction(frame, "zoomIn"), "Zoom In",
				hasImage);
		addMenuItem(editMenu, new ZoomOutAction(frame, "zoomOut"), "Zoom Out",
				hasImage);
		addMenuItem(editMenu, new ZoomOneAction(frame, "zoomOne"), "Zoom One",
				hasImage);
		
        // add utility
		editMenu.addSeparator();
        addMenuItem(editMenu, new PrintFrameListAction(frame, "printFrameList"), 
                "Print Frame List");
        addMenuItem(editMenu, new PrintDocumentListAction(frame, "printDocumentList"), 
                "Print Document List");
        addMenuItem(editMenu, new DocClearShapesAction(frame, "docClearShapes"),
                "Clear Shapes");
		
		return editMenu;
	}

	/**
	 * Creates the sub-menu for the "IMAGE" item in the main Menu bar.
	 */
	private JMenu createImageMenu()
	{
		JMenu menu = new JMenu("Image");
		
        JMenu imageTypeMenu = new JMenu("Image Type");
        addMenuItem(imageTypeMenu, new SetImageTypeToLabelAction(frame, "convertTypeToLabel"),
                "Set to Label Image", hasScalarImage);
        menu.add(imageTypeMenu);
        
	      // Type conversion items
        JMenu convertDataTypeMenu = new JMenu("Convert Data-Type");
        convertDataTypeMenu.setEnabled(hasImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToBinary(), "Binary", hasScalarImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToUInt8(), "UInt8", hasScalarImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToUInt16(), "UInt16", hasScalarImage);
        convertDataTypeMenu.addSeparator();
        addPlugin(convertDataTypeMenu, new ConvertImageToInt16(), "Int16", hasScalarImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToInt32(), "Int32", hasScalarImage);
        convertDataTypeMenu.addSeparator();
        addPlugin(convertDataTypeMenu, new ConvertImageToFloat32(), "Float32", hasImage);
        addPlugin(convertDataTypeMenu, new ConvertImageToFloat64(), "Float64", hasImage);
        menu.add(convertDataTypeMenu);
        
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

        
        // Color conversion items
        menu.addSeparator();
        JMenu colorMenu = new JMenu("Color");
        // editMenu.add(convertTypeMenu);
        addPlugin(colorMenu, new ConvertRGB8ImageToUInt8(), "Convert to UInt8", hasColorImage);
        addPlugin(colorMenu, new ImageSplitChannels(), "Split Channels", hasVectorImage || hasColorImage);
        addPlugin(colorMenu, new MergeChannelImages(), "Merge Channels");
        addPlugin(colorMenu, new ColorImageExtractChannel(), "Extract Channel...", hasColorImage);
        // addMenuItem(editMenu, new MetaImageOperatorAction(frame,
        // "colorToGray",
        // new Gray8Converter()), "RGB -> Gray8", hasColorImage);
        menu.add(colorMenu);

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
        addPlugin(geometryMenu, new ImageReshape(), "Reshape Image");
        addPlugin(geometryMenu, new ImageDownsample(), "Downsample Image", hasImage);
        
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
        addMenuItem(phantomMenu, new ImageFillDiskAction(frame, "imageFillDisk"), 
                "Fill Disk...");
        addMenuItem(phantomMenu, new ImageFillEllipseAction(frame, "imageFillEllipse"), 
                "Fill Ellipse...");
        addMenuItem(phantomMenu, new ImageSelectionToMaskAction(frame, "imageSelectionToMask"), 
                "Selection To Mask");
        addMenuItem(phantomMenu, new ImageSelectionToDistanceMapAction(frame, "imageSelectionToDistanceMap"), 
                "Selection To Distance Map");
        menu.add(phantomMenu);
        
		menu.addSeparator();
        addPlugin(menu, new PrintImageTiffTags(), "Print TIFF Tags", hasImage);
        addPlugin(menu, new PrintImageInfos(), "Print Image Info", hasImage);
		return menu;
	}

	/**
	 * Creates the sub-menu for the "process" item in the main Menu bar.
	 */
	private JMenu createImageProcessMenu()
	{
		JMenu menu = new JMenu("Process");

		JMenu mathsMenu = new JMenu("Maths");
		addMenuItem(mathsMenu,
				new ArrayOperatorAction(frame, "sqrt", new Sqrt()),
				"Sqrt", hasScalarImage);
		addMenuItem(mathsMenu,
				new ArrayOperatorAction(frame, "powerOfTwo", new PowerOfTwo()),
				"Power Of Two", hasScalarImage);
		menu.add(mathsMenu);
		menu.addSeparator();

		// Noise reduction filters
        addPlugin(menu, new BoxFilter(), "Box Filter");
		addMenuItem(menu, new BoxFilter3x3Float(frame, "boxFilter3x3Float"),
				"Box Filter 2D 3x3 (float)", hasScalarImage);
        addPlugin(menu, new ImageBoxMedianFilter(), "Median Filter");
        addPlugin(menu, new ImageBoxMinMaxFilter(), "Min/Max Filter");
		addPlugin(menu, new ImageBoxVarianceFilter(), "Variance Filter");
		menu.addSeparator();
		
		// Gradient filters
        addImageOperatorPlugin(menu, new SobelGradient(), "Sobel Gradient", hasScalarImage);
		addImageOperatorPlugin(menu, new SobelGradientNorm(), "Sobel Gradient Norm", hasScalarImage);
		addImageOperatorPlugin(menu, new VectorArrayNorm(), "Vector Image Norm", hasVectorImage);
		// addMenuItem(menu, new ImageOperatorAction(frame, "vectorAngle",
		// new VectorImageAngle()),
		// "Array<?> Angle", hasImage);
		menu.addSeparator();
		
		JMenu morphologyMenu = new JMenu("Mathematical Morphology");
		addPlugin(morphologyMenu, new ImageMorphologicalFilter(), "Morphological Filtering");

		morphologyMenu.addSeparator();
        addImageOperatorPlugin(morphologyMenu,
                new RegionalExtrema2D(MinimaAndMaxima.Type.MINIMA, Connectivity2D.C4),
                "Regional Minima (2D)", hasScalarImage && hasImage2D);
        addImageOperatorPlugin(morphologyMenu, 
				new RegionalExtrema2D(MinimaAndMaxima.Type.MAXIMA, Connectivity2D.C4), 
				"Regional Maxima (2D)", hasScalarImage && hasImage2D);
		addPlugin(morphologyMenu, new ImageExtendedExtrema(), "Extended Min./Max.", hasScalarImage);
		addPlugin(morphologyMenu, new ImageMorphologicalReconstruction(), "Morphological Reconstruction");
		morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, new ImageFillHoles(), "Fill Holes");
        addPlugin(morphologyMenu, new ImageKillBorders(), "Kill Borders");
		menu.add(morphologyMenu);

		// operators specific to binary images
		JMenu binaryMenu = new JMenu("Binary Images");
        addPlugin(binaryMenu, new BinaryImageConnectedComponentsLabeling(), "Connected Components Labeling");
        addArrayOperatorPlugin(binaryMenu, new ChamferDistanceTransform2DUInt16(ChamferWeights2D.CHESSKNIGHT, false),
				"Distance Map", hasImage2D && hasBinaryImage);
        addArrayOperatorPlugin(binaryMenu, new ChamferDistanceTransform2DFloat(ChamferWeights2D.CHESSKNIGHT, false),
				"Distance Map (float)", hasImage2D && hasBinaryImage);
		addPlugin(binaryMenu, new ImageGeodesicDistanceMap(), "Geodesic Distance Map...");
        addPlugin(binaryMenu, new BinaryImageSkeleton(), "IJ Skeleton");
        addMenuItem(binaryMenu, new BinaryImageOverlayAction(frame, "binaryImageOverlay"),
                "Binary Overlay...");
        binaryMenu.addSeparator();
        addPlugin(binaryMenu, new BinaryImageBoundaryGraph(),
                "Boundary Graph", hasImage2D && hasBinaryImage);
		menu.add(binaryMenu);

		menu.addSeparator();

		addPlugin(menu, new ImageOtsuThreshold(), "Otsu Threshold", hasScalarImage);
        addPlugin(menu, new ImageManualThreshold(), "Manual Threshold", hasScalarImage);
        addPlugin(menu, new ImageFindNonZeroPixels(),
                "Find Non-Zeros Elements", hasImage2D && hasScalarImage);
        addPlugin(menu, new ImageIsocontour(), "Isocontour...");

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
		menu.addSeparator();
		addPlugin(menu, new ImageLineProfile(), "Line Profile", hasImage);

        menu.addSeparator();
        addPlugin(menu, new LabelImageBoundingBoxes(), "Bounding Boxes", (hasImage2D || hasImage3D) && hasLabelImage);
        addPlugin(menu, new LabelImageCentroids(), "Regions Centroids", (hasImage2D || hasImage3D) && hasLabelImage);
        addPlugin(menu, new LabelImageEquivalentDisks(), "Regions Equivalent Disks", hasImage2D && hasLabelImage);
        addPlugin(menu, new LabelImageInertiaEllipses(), "Regions Inertia Ellipses", hasImage2D && hasLabelImage);
		return menu;
	}

	private JMenu createHelpMenu()
	{
		JMenu menu = new JMenu("Help");
		addMenuItem(menu, null, "About...", true);
		return menu;
	}

	private JMenuItem addMenuItem(JMenu menu, ImagoAction action, String label)
	{
		return addMenuItem(menu, action, label, true);
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
        Plugin plugin = new ImageOperatorPlugin(operator);
        return addPlugin(menu, plugin, label, enabled);
    }

    private JMenuItem addArrayOperatorPlugin(JMenu menu, ArrayOperator operator, String label, boolean enabled)
    {
        Plugin plugin = new ImageArrayOperatorPlugin(operator);
        return addPlugin(menu, plugin, label, enabled);
    }

    private JMenuItem addPlugin(JMenu menu, Plugin plugin, String label)
    {
       return addPlugin(menu, plugin, label, plugin.isEnabled(frame));
    }

    private JMenuItem addPlugin(JMenu menu, Plugin plugin, String label, boolean enabled)
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
