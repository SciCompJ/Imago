/**
 * 
 */
package imago.gui;

import imago.app.ImagoDoc;
import imago.gui.action.RunPluginAction;
import imago.gui.tool.DrawValueTool;
import imago.gui.tool.SelectLineSegmentTool;
import imago.gui.tool.SelectPolygonTool;
import imago.gui.tool.SelectRectangleTool;
import imago.gui.tool.SelectionTool;
import imago.plugin.BoxFilter3x3FloatPlugin;
import imago.plugin.CloseCurrentFrame;
import imago.plugin.CloseWithChildren;
import imago.plugin.QuitApplication;
import imago.plugin.edit.ChangeCurrentTool;
import imago.plugin.edit.DocClearShapes;
import imago.plugin.edit.PrintDocumentList;
import imago.plugin.edit.PrintFrameList;
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
import imago.plugin.image.convert.ConvertScalarImageToUInt8;
import imago.plugin.image.edit.CreateColorCubeImage3D;
import imago.plugin.image.edit.CreateDistanceToOctahedronImage3D;
import imago.plugin.image.edit.ImageFillDisk;
import imago.plugin.image.edit.ImageFillEllipse;
import imago.plugin.image.edit.ImageSelectionToDistanceMap;
import imago.plugin.image.edit.ImageSelectionToMask;
import imago.plugin.image.edit.ImageSetBackgroundColor;
import imago.plugin.image.edit.ImageSetColorMapFactory;
import imago.plugin.image.edit.ImageSetScale;
import imago.plugin.image.edit.PrintImageInfos;
import imago.plugin.image.edit.PrintImageTiffTags;
import imago.plugin.image.edit.RefreshDisplay;
import imago.plugin.image.edit.SetImageDisplayRange;
import imago.plugin.image.edit.SetImageDisplayRangeToData;
import imago.plugin.image.edit.SetImageDisplayRangeToDataType;
import imago.plugin.image.edit.SetImageTypeToLabel;
import imago.plugin.image.file.CreateNewImage;
import imago.plugin.image.file.ImportImageMetaImage;
import imago.plugin.image.file.ImportImageRawData;
import imago.plugin.image.file.ImportImageVgi;
import imago.plugin.image.file.OpenDemoImage;
import imago.plugin.image.file.OpenDemoStack;
import imago.plugin.image.file.OpenImage;
import imago.plugin.image.file.ReadImageTiff;
import imago.plugin.image.file.ReadTiffStackSlice;
import imago.plugin.image.process.BinaryImageChamferDistanceMap;
import imago.plugin.image.process.BinaryImageConnectedComponentsLabeling;
import imago.plugin.image.process.BinaryImageOverlay;
import imago.plugin.image.process.BinaryImageSkeleton;
import imago.plugin.image.process.ImageBoxFilter;
import imago.plugin.image.process.ColorImageExtractChannel;
import imago.plugin.image.process.Image3DGetCurrentSlice;
import imago.plugin.image.process.Image3DGetSlice;
import imago.plugin.image.process.Image3DOrthoslicesImage;
import imago.plugin.image.process.Image3DSetOrthoSlicesDisplay;
import imago.plugin.image.process.ImageBivariateHistogram;
import imago.plugin.image.process.ImageBoxMedianFilter;
import imago.plugin.image.process.ImageBoxMinMaxFilter;
import imago.plugin.image.process.ImageBoxVarianceFilter;
import imago.plugin.image.process.ImageConcatenate;
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
import imago.plugin.image.process.ImageRegionalExtrema;
import imago.plugin.image.process.ImageReshape;
import imago.plugin.image.process.ImageRotateAroundCenter;
import imago.plugin.image.process.ImageSplitChannels;
import imago.plugin.image.process.MergeChannelImages;
import imago.plugin.image.process.VectorImageChannelView;
import imago.plugin.image.vectorize.BinaryImageBoundaryGraph;
import imago.plugin.image.vectorize.ImageFindNonZeroPixels;
import imago.plugin.image.vectorize.ImageIsocontour;
import imago.plugin.table.OpenTable;
import imago.plugin.table.SaveTable;
import imago.plugin.table.ShowDemoTable;
import imago.plugin.table.plot.TableLinePlot;
import imago.plugin.table.plot.TableScatterPlot;

import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.sci.array.Array;
import net.sci.array.ArrayOperator;
import net.sci.array.color.RGB8Array;
import net.sci.array.process.PowerOfTwo;
import net.sci.array.process.Sqrt;
import net.sci.array.process.shape.Rotate90;
import net.sci.image.ColorMaps;
import net.sci.image.Image;
import net.sci.image.ImageOperator;
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
        addPlugin(fileMenu, new ReadTiffStackSlice(), "Read TIFF Slice");

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

		// tool selection items
		if (frame instanceof ImagoDocViewer)
		{
			ImagoTool tool;
			ImagoDocViewer viewer = (ImagoDocViewer) frame;

			tool = new SelectionTool(viewer, "select");
			addPlugin(editMenu, new ChangeCurrentTool(tool), "Select", hasImage);
			addPlugin(editMenu, 
                    new ChangeCurrentTool(new SelectLineSegmentTool(viewer, "selectLineSegment")),
                    "Select Line", hasImage);
            addPlugin(editMenu, 
                    new ChangeCurrentTool(new SelectRectangleTool(viewer, "selectRectangle")),
                    "Select Rectangle", hasImage);
            addPlugin(editMenu, 
                    new ChangeCurrentTool(new SelectPolygonTool(viewer, "selectPolygon")),
                    "Select Polygon", hasImage);

            editMenu.addSeparator();
            addPlugin(editMenu, 
                    new ChangeCurrentTool(new DrawValueTool(viewer, "drawWhite", Double.MAX_VALUE)),
                    "Draw White", hasScalarImage);
            addPlugin(editMenu, 
                    new ChangeCurrentTool(new DrawValueTool(viewer, "drawBlack", 0.0)),
                    "Draw Black", hasScalarImage);

            editMenu.addSeparator();
		}

		// zoom items
		addPlugin(editMenu, new ZoomIn(), "Zoom In", hasImage);
		addPlugin(editMenu, new ZoomOut(), "Zoom Out", hasImage);
		addPlugin(editMenu, new ZoomOne(), "Zoom One", hasImage);
        addPlugin(editMenu, new RefreshDisplay(), "Refresh Display", hasImage);
		
        // add utility
		editMenu.addSeparator();
		addPlugin(editMenu, new PrintFrameList(), "Print Frame List");
		addPlugin(editMenu, new PrintDocumentList(), "Print Document List");
		addPlugin(editMenu, new DocClearShapes(), "Clear Shapes");
		
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
        
	      // Type conversion items
        addPlugin(menu, new ConvertScalarImageToUInt8(), "Convert to Gray8", hasScalarImage);
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

        JMenu vectorMenu = new JMenu("Vector");
        addPlugin(vectorMenu, new VectorImageChannelView(), "Channel View", hasVectorImage);
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
        addPlugin(geometryMenu, new ImageReshape(), "Reshape Image");
        addPlugin(geometryMenu, new ImageDownsample(), "Downsample Image", hasImage);
        geometryMenu.addSeparator();
        addPlugin(geometryMenu, new ImageConcatenate(), "Concatenate");
        
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
	 * Creates the sub-menu for the "process" item in the main Menu bar.
	 */
	private JMenu createImageProcessMenu()
	{
		JMenu menu = new JMenu("Process");

		JMenu mathsMenu = new JMenu("Maths");
        addArrayOperatorPlugin(mathsMenu, new Sqrt(), "Sqrt", hasScalarImage);
        addArrayOperatorPlugin(mathsMenu, new PowerOfTwo(), "Power Of Two", hasScalarImage);
		menu.add(mathsMenu);
		menu.addSeparator();

		// Noise reduction filters
        addPlugin(menu, new ImageBoxFilter(), "Box Filter...");
		addPlugin(menu, new BoxFilter3x3FloatPlugin(), "Box Filter 2D 3x3 (float)", hasScalarImage);
        addPlugin(menu, new ImageBoxMedianFilter(), "Median Filter...");
        addPlugin(menu, new ImageBoxMinMaxFilter(), "Min/Max Filter...");
		addPlugin(menu, new ImageBoxVarianceFilter(), "Variance Filter...");
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
		addPlugin(morphologyMenu, new ImageMorphologicalFilter(), "Morphological Filtering...");

		morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, new ImageRegionalExtrema(), "Regional Min./Max...", hasScalarImage);
		addPlugin(morphologyMenu, new ImageExtendedExtrema(), "Extended Min./Max...", hasScalarImage);
		addPlugin(morphologyMenu, new ImageMorphologicalReconstruction(), "Morphological Reconstruction...");
		morphologyMenu.addSeparator();
        addPlugin(morphologyMenu, new ImageFillHoles(), "Fill Holes");
        addPlugin(morphologyMenu, new ImageKillBorders(), "Kill Borders");
		menu.add(morphologyMenu);

		// operators specific to binary images
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
        addPlugin(menu, new ImageBivariateHistogram(), "Bivariate Histogram");
		menu.addSeparator();
		addPlugin(menu, new ImageLineProfile(), "Line Profile", hasImage);

        menu.addSeparator();
        addPlugin(menu, new LabelImageBoundingBoxes(), "Bounding Boxes", (hasImage2D || hasImage3D) && hasLabelImage);
        addPlugin(menu, new LabelImageCentroids(), "Regions Centroids", (hasImage2D || hasImage3D) && hasLabelImage);
        addPlugin(menu, new LabelImageEquivalentDisks(), "Regions Equivalent Disks", hasImage2D && hasLabelImage);
        addPlugin(menu, new LabelImageInertiaEllipses(), "Regions Inertia Ellipses", hasImage2D && hasLabelImage);

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
