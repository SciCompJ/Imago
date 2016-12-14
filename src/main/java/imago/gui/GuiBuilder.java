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
import imago.gui.action.ImageArrayOperatorAction;
import imago.gui.action.ImageOperatorAction;
import imago.gui.action.SelectToolAction;
import imago.gui.action.analyze.ImageHistogramAction;
import imago.gui.action.edit.PrintDocumentListAction;
import imago.gui.action.edit.ZoomInAction;
import imago.gui.action.edit.ZoomOneAction;
import imago.gui.action.edit.ZoomOutAction;
import imago.gui.action.file.CreateColorCubeStack;
import imago.gui.action.file.CreateEmptyImageAction;
import imago.gui.action.file.ImportMetaImageFileAction;
import imago.gui.action.file.OpenDemoImage;
import imago.gui.action.file.OpenDemoStack;
import imago.gui.action.file.OpenImageAction;
import imago.gui.action.file.ReadTiffAction;
import imago.gui.action.image.ConvertToFloat32ImageAction;
import imago.gui.action.image.ConvertToFloat64ImageAction;
import imago.gui.action.image.ConvertToInt16ImageAction;
import imago.gui.action.image.ConvertToInt32ImageAction;
import imago.gui.action.image.ConvertToUInt16ImageAction;
import imago.gui.action.image.ConvertToUInt8ImageAction;
import imago.gui.action.image.MergeChannelImagesAction;
import imago.gui.action.image.MiddleSliceImageAction;
import imago.gui.action.image.PrintImageTiffTagsAction;
import imago.gui.action.image.SetDataTypeDisplayRangeAction;
import imago.gui.action.image.SetImageDisplayRangeAction;
import imago.gui.action.image.SetManualDisplayRangeAction;
import imago.gui.action.image.SplitImageChannelsAction;
import imago.gui.action.image.StackToVectorImageAction;
import imago.gui.action.process.BoxFilter3x3Float;
import imago.gui.action.process.BoxFilterAction;
import imago.gui.action.process.BoxMinMaxFilterAction;
import imago.gui.action.process.ImageOtsuThresholdAction;
import imago.gui.action.process.MedianBoxFilterAction;
import imago.gui.tool.SelectionTool;
import net.sci.array.Array;
import net.sci.array.data.ScalarArray;
import net.sci.array.process.PowerOfTwo;
import net.sci.array.process.Sqrt;
import net.sci.array.process.shape.Flip;
import net.sci.image.Image;
import net.sci.image.binary.ChamferWeights;
import net.sci.image.binary.FloodFillComponentLabeling2D;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DFloat;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DShort;
import net.sci.image.data.Connectivity2D;
import net.sci.image.morphology.extrema.ExtremaType;
import net.sci.image.morphology.extrema.RegionalExtrema2D;
import net.sci.image.process.DynamicAdjustment;
import net.sci.image.process.ImageInverter;
import net.sci.image.process.ImageThreshold;
import net.sci.image.process.RotationAroundCenter;
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

	ImagoFrame frame;

	Icon emptyIcon;

	public GuiBuilder(ImagoFrame frame)
	{
		this.frame = frame;

		createEmptyIcon();
	}

	public void createMenuBar()
	{

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(createImageMenu());
		menuBar.add(createProcessMenu());
		menuBar.add(createAnalyzeMenu());
		menuBar.add(createHelpMenu());

		frame.setJMenuBar(menuBar);
	}

	/**
	 * Creates the sub-menu for the "File" item in the main menu bar.
	 */
	private JMenu createFileMenu()
	{
		JMenu fileMenu = new JMenu("File");
		addMenuItem(fileMenu, new CreateEmptyImageAction(frame, "createEmptyImage"), 
				"New...");
		addMenuItem(fileMenu, new OpenImageAction(frame, "openImage"),
				"Open...");
		addMenuItem(fileMenu, new ReadTiffAction(frame, "readTiffImage"),
				"Read TIFF...");
		addMenuItem(fileMenu, new ImportMetaImageFileAction(frame,
				"readMetaImageFormat"), "Read MetaImage Format...");

		JMenu demoMenu = new JMenu("Demo images");
		addMenuItem(demoMenu, new OpenDemoImage(frame, "openDemoLena",
				"files/lena_gray_512.bmp"), "Lena");
		addMenuItem(demoMenu, new OpenDemoImage(frame, "openDemoSunflower",
				"files/sunflower.png"), "Sunflower");
		addMenuItem(demoMenu, new OpenDemoStack(frame, "openDemoStack"),
				"Demo Stack");
		addMenuItem(demoMenu, new CreateColorCubeStack(frame,
				"createDemoColorCube"), "3D Color Cube");
//
//		addMenuItem(demoMenu, new CreateWhiteNoiseImageAction(frame,
//				"createWhiteNoiseImage"), "White Noise Array<?>");
		fileMenu.add(demoMenu);

		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem("Quit"));
		return fileMenu;
	}

	/**
	 * Creates the sub-menu for the "Edit" item in the main menu bar.
	 */
	private JMenu createEditMenu()
	{
		boolean isImage = hasImageDoc(frame);
		boolean isScalar = hasScalarImage(frame);
		boolean isVector = hasVectorImage(frame);
		boolean isColor = hasRGB8Image(frame);
			
		JMenu editMenu = new JMenu("Edit");

		// Type conversion items
		JMenu convertTypeMenu = new JMenu("Convert Type");
		convertTypeMenu.setEnabled(isImage);
		// addMenuItem(convertTypeMenu, new MetaImageOperatorAction(frame,
		// "toGray8",
		// new Gray8Converter()), "Gray8", isImage);
		// addMenuItem(convertTypeMenu, new MetaImageOperatorAction(frame,
		// "toFloat",
		// new FloatConverter()), "Float", isImage);
		// editMenu.add(convertTypeMenu);
		addMenuItem(convertTypeMenu, new ConvertToUInt8ImageAction(frame, "convertToUInt8"),
				"UInt8", isScalar);
		addMenuItem(convertTypeMenu, new ConvertToUInt16ImageAction(frame, "convertToUInt16"),
				"UInt16", isScalar);
		convertTypeMenu.addSeparator();
		addMenuItem(convertTypeMenu, new ConvertToInt16ImageAction(frame, "convertToInt16"),
				"Int16", isScalar);
		addMenuItem(convertTypeMenu, new ConvertToInt32ImageAction(frame, "convertToInt32"),
				"Int32", isScalar);
		convertTypeMenu.addSeparator();
		addMenuItem(convertTypeMenu, new ConvertToFloat32ImageAction(frame, "convertToFloat32"),
				"Float32", isImage);
		addMenuItem(convertTypeMenu, new ConvertToFloat64ImageAction(frame, "convertToFloat64"),
				"Float64", isImage);
		editMenu.add(convertTypeMenu);


		// Color conversion items
		JMenu colorMenu = new JMenu("Color");
		addMenuItem(colorMenu, new SplitImageChannelsAction(frame,
				"splitChannels"), "Split Channels", isVector || isColor);
		addMenuItem(colorMenu, new MergeChannelImagesAction(frame,
				"mergeChannels"), "Merge Channels");
		// addMenuItem(editMenu, new MetaImageOperatorAction(frame,
		// "colorToGray",
		// new Gray8Converter()), "RGB -> Gray8", isColor);
		editMenu.add(colorMenu);

		editMenu.addSeparator();
		addMenuItem(editMenu, 
				new ImageArrayOperatorAction(frame, "invert",
				new ImageInverter()), "Invert", isScalar || isColor);
		editMenu.addSeparator();

		// tool selection items
		if (frame instanceof ImagoDocViewer)
		{
			ImagoTool tool;
			ImagoDocViewer viewer = (ImagoDocViewer) frame;

			tool = new SelectionTool(viewer, "select");
			addMenuItem(editMenu, new SelectToolAction(viewer, tool), "Select",
					isImage);

//			tool = new SelectLineTool(viewer, "selectLine");
			addMenuItem(editMenu, new SelectToolAction(viewer, tool),
					"Select Line", isImage);

			editMenu.addSeparator();
		}

		// zoom items
		addMenuItem(editMenu, new ZoomInAction(frame, "zoomIn"), "Zoom In",
				isImage);
		addMenuItem(editMenu, new ZoomOutAction(frame, "zoomOut"), "Zoom Out",
				isImage);
		addMenuItem(editMenu, new ZoomOneAction(frame, "zoomOne"), "Zoom One",
				isImage);
		
		// add utility
		editMenu.addSeparator();
		addMenuItem(editMenu, new PrintDocumentListAction(frame, "printDocumentList"), "Print Document List");
		
		return editMenu;
	}

	/**
	 * Creates the sub-menu for the "IMAGE" item in the main Menu bar.
	 */
	private JMenu createImageMenu()
	{
		boolean isImage = hasImageDoc(frame);
		boolean is3D = has3DImage(frame);
		boolean isScalar = hasScalarImage(frame);
		boolean isVector = hasVectorImage(frame);

		JMenu menu = new JMenu("Image");
		
		JMenu displayRangeMenu = new JMenu("Display Range");
		addMenuItem(displayRangeMenu, new SetDataTypeDisplayRangeAction(frame, "setDataTypeDisplayRange"), "Set Data Type Display Range",
				isScalar);
		addMenuItem(displayRangeMenu, new SetImageDisplayRangeAction(frame, "setImageDisplayRange"), "Set Image Display Range",
				isScalar | isVector);
		addMenuItem(displayRangeMenu, new SetManualDisplayRangeAction(frame, "setManualDisplayRange"), "Set Manual Display Range",
				isScalar | isVector);
		// addMenuItem(editMenu, new SetDisplayRangeUnitIntervalAction(frame,
		// "setDisplayRangeUnitInterval"),
		// "Set Display Range [0 ; 1]", isScalar || isVector);
		menu.add(displayRangeMenu);

		addMenuItem(menu,
				 new ArrayOperatorAction(frame, "adjustDynamic", new DynamicAdjustment(.01)),
				 "Adjust Grayscale Dynamic", isScalar);

		JMenu geometryMenu = new JMenu("Geometry");
		geometryMenu.setEnabled(isImage);
		addMenuItem(geometryMenu, new ArrayOperatorAction(frame,
				"flipXFilter", new Flip(0)), "Flip Horizontal", isImage);
		addMenuItem(geometryMenu, new ArrayOperatorAction(frame,
				"flipYFilter", new Flip(1)), "Flip Vertical", isImage);
		addMenuItem(geometryMenu, new ArrayOperatorAction(frame,
				"flipZFilter", new Flip(2)), "Flip Slices", is3D);
		geometryMenu.addSeparator();
		// addMenuItem(geometryMenu,
		// new ImageOperatorAction(frame, "rotation90", new Rotation90()),
		// "Rotation 90", has2D);
		addMenuItem(geometryMenu,
				 new ArrayOperatorAction(frame, "rotateImage", new RotationAroundCenter(30)),
				 "Rotate Image", isImage);

		menu.add(geometryMenu);

		JMenu stackMenu = new JMenu("Stacks");
		stackMenu.setEnabled(is3D);

		addMenuItem(stackMenu, 
				new MiddleSliceImageAction(frame, "middleSlice"), "Middle Slice", is3D);
		addMenuItem(stackMenu, new StackToVectorImageAction(frame, "stackToVector"),
				"Stack To Vector", is3D);

//		addMenuItem(stackMenu, new StackSliceAction(frame, "stackSlice"),
//				"Get Slice Stack", has3D);
//
		menu.add(stackMenu);
//
//		addMenuItem(menu, new DuplicateAction(frame, "Duplicate"), "Duplicate",
//				isImage);

		addMenuItem(menu, new PrintImageTiffTagsAction(frame,
				"printImageTiffTags"), "Print TIFF Tags", isImage);
		return menu;
	}

	/**
	 * Creates the sub-menu for the "process" item in the main Menu bar.
	 */
	private JMenu createProcessMenu()
	{
		boolean isImage = hasImageDoc(frame);
		boolean is2D = has2DImage(frame);
		boolean isScalar = hasScalarImage(frame);
		boolean isVector = hasVectorImage(frame);
		boolean isBinary = hasBinaryImage(frame);

		JMenu menu = new JMenu("Process");
		addMenuItem(menu,
				new ArrayOperatorAction(frame, "sqrt", new Sqrt()),
				"Sqrt", isScalar);
		addMenuItem(menu,
				new ArrayOperatorAction(frame, "powerOfTwo", new PowerOfTwo()),
				"Power Of Two", isScalar);
		
		menu.addSeparator();
		addMenuItem(menu, 
				new ImageOtsuThresholdAction(frame, "otsuThreshold"),
				"Otsu Threshold", isScalar);
		addMenuItem(menu,
				new ImageOperatorAction(frame, "threshold", new ImageThreshold(20)),
				"Threshold (20)", isScalar);

		menu.addSeparator();
		addMenuItem(menu, new BoxFilterAction(frame, "boxFilter"),
				"Box Filter", isImage);
		addMenuItem(menu, new BoxFilter3x3Float(frame, "boxFilter3x3Float"),
				"Box Filter 2D 3x3 (float)", isScalar);
		addMenuItem(menu, new MedianBoxFilterAction(frame, "medianFilter"),
				"Median Filter", isScalar);
		addMenuItem(menu, new BoxMinMaxFilterAction(frame, "minMaxFilter"),
				"Min/Max Filter", isScalar);
		// addMenuItem(menu, new Dilation2D11x11Action(frame, "dilation11x11"),
		// "Dilation 11x11", isImage);
		menu.addSeparator();
		addMenuItem(menu, new ImageOperatorAction(frame, "regionalMin",
				new RegionalExtrema2D(ExtremaType.MINIMA, Connectivity2D.C4)), "Regional Minima", isScalar);
		addMenuItem(menu, new ImageOperatorAction(frame, "regionalMax",
				new RegionalExtrema2D(ExtremaType.MAXIMA, Connectivity2D.C4)), "Regional Maxima", isScalar);

//		menu.addSeparator();
//		addMenuItem(menu, new ImageLogAction(frame, "imageLog"), "Array<?> Log",
//				isImage);

		menu.addSeparator();
		addMenuItem(menu, new ImageOperatorAction(frame, "sobelGradientNorm",
				new SobelGradientNorm()), "Sobel Gradient Norm", isScalar);
		addMenuItem(menu, new ImageArrayOperatorAction(frame, "sobelGradient",
				new SobelGradient()), "Sobel Gradient", isScalar);
		addMenuItem(menu, new ImageArrayOperatorAction(frame, "vectorImageNorm",
				new VectorArrayNorm()), "Vector Image Norm", isVector);
		// addMenuItem(menu, new ImageOperatorAction(frame, "vectorAngle",
		// new VectorImageAngle()),
		// "Array<?> Angle", isImage);

		// operators specific to binary images
		menu.addSeparator();
		addMenuItem(menu, new ImageArrayOperatorAction(frame, "connectedComponentLabeling",
				new FloodFillComponentLabeling2D()), "Connected Component Labeling", is2D && isBinary);
		addMenuItem(menu, new ArrayOperatorAction(frame, "distanceMap2dShort",
				new ChamferDistanceTransform2DShort(ChamferWeights.CHESSKNIGHT, false)),
				"Distance Map", is2D && isBinary);
		addMenuItem(menu, new ArrayOperatorAction(frame, "distanceMap2dFloat",
				new ChamferDistanceTransform2DFloat(ChamferWeights.CHESSKNIGHT, false)),
				"Distance Map (float)", is2D && isBinary);

		return menu;
	}

	/**
	 * Creates the sub-menu for the "process" item in the main Menu bar.
	 */
	private JMenu createAnalyzeMenu()
	{
		 boolean isImage = hasImageDoc(frame);
		// boolean has3D = has3DImage(frame);
		// boolean has2D = has2DImage(frame);

		JMenu menu = new JMenu("Analyze");

		addMenuItem(menu, new ImageHistogramAction(frame, "histogram"),
				"Histogram", isImage);
//		addMenuItem(menu, new RGBJointHistogramsAction(frame,
//				"rgbJointHistograms"), "RGB Joint Histograms",
//				hasRGB8Image(frame));
//		menu.addSeparator();
//		addMenuItem(menu, new ImageLineProfileDemoAction(frame, "lineProfile"),
//				"Line Profile", isImage);

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

	private final static boolean hasImageDoc(ImagoFrame frame)
	{
		ImagoDoc doc = null;
		if (frame instanceof ImagoDocViewer)
		{
			doc = ((ImagoDocViewer) frame).getDocument();
		}
		return doc != null;
	}

	private final static boolean hasScalarImage(ImagoFrame frame)
	{
		ImagoDoc doc = null;
		if (frame instanceof ImagoDocViewer)
		{
			doc = ((ImagoDocViewer) frame).getDocument();
		}
		if (doc == null)
			return false;

		boolean isScalar = false;
		Array<?> img = doc.getImage().getData();
		if (img instanceof ScalarArray<?>)
		{
			isScalar = true;
		}
		return isScalar;
	}

	private final static boolean hasVectorImage(ImagoFrame frame)
	{
		ImagoDoc doc = null;
		if (frame instanceof ImagoDocViewer)
		{
			doc = ((ImagoDocViewer) frame).getDocument();
		}
		if (doc == null)
			return false;

		switch(doc.getImage().getType())
		{
		case VECTOR:
		case COLOR:
		case COMPLEX:
			return true;
		default:
			return false;
		}
	}

//	private final static boolean hasColorImage(ImagoFrame frame)
//	{
//		ImagoDoc doc = null;
//		if (frame instanceof ImagoDocViewer)
//		{
//			doc = ((ImagoDocViewer) frame).getDocument();
//		}
//		if (doc == null)
//			return false;
//
//		return doc.getImage().getType() == Image.Type.COLOR;
//	}

	private final static boolean has2DImage(ImagoFrame frame)
	{
		ImagoDoc doc = null;
		if (frame instanceof ImagoDocViewer)
		{
			doc = ((ImagoDocViewer) frame).getDocument();
		}
		if (doc == null)
			return false;

		Array<?> array = doc.getImage().getData();
		return array.dimensionality() == 2;
	}

	private final static boolean has3DImage(ImagoFrame frame)
	{
		ImagoDoc doc = null;
		if (frame instanceof ImagoDocViewer)
		{
			doc = ((ImagoDocViewer) frame).getDocument();
		}
		if (doc == null)
			return false;

		Array<?> array = doc.getImage().getData();
		return array.dimensionality() == 3;
	}

	private final static boolean hasRGB8Image(ImagoFrame frame)
	{
		ImagoDoc doc = null;
		if (frame instanceof ImagoDocViewer)
		{
			doc = ((ImagoDocViewer) frame).getDocument();
		}
		if (doc == null)
			return false;

		return doc.getImage().getType() == Image.Type.COLOR;
	}
	
	private final static boolean hasBinaryImage(ImagoFrame frame)
	{
		ImagoDoc doc = null;
		if (frame instanceof ImagoDocViewer)
		{
			doc = ((ImagoDocViewer) frame).getDocument();
		}
		if (doc == null)
			return false;

		return doc.getImage().getType() == Image.Type.BINARY;
	}
}
