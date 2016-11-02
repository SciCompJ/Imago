/**
 * 
 */
package imago.gui;

import imago.app.ImagoDoc;
import imago.gui.action.ImageArrayOperatorAction;
import imago.gui.action.ImageOperatorAction;
import imago.gui.action.SelectToolAction;
import imago.gui.action.analyze.ImageHistogramAction;
import imago.gui.action.edit.PrintDocumentListAction;
import imago.gui.action.edit.ZoomInAction;
import imago.gui.action.edit.ZoomOneAction;
import imago.gui.action.edit.ZoomOutAction;
import imago.gui.action.file.CreateColorCubeStack;
import imago.gui.action.file.ImportMetaImageFileAction;
import imago.gui.action.file.OpenDemoImage;
import imago.gui.action.file.OpenDemoStack;
import imago.gui.action.file.OpenImageAction;
import imago.gui.action.file.ReadTiffAction;
import imago.gui.action.image.PrintImageTiffTagsAction;
import imago.gui.action.image.SetDataTypeDisplayRangeAction;
import imago.gui.action.image.SetImageDisplayRangeAction;
import imago.gui.action.image.SetManualDisplayRangeAction;
import imago.gui.action.process.MiddleSliceImageAction;
import imago.gui.tool.SelectionTool;

import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.sci.array.Array;
import net.sci.array.data.Array2D;
import net.sci.array.data.Array3D;
import net.sci.array.data.ScalarArray;
import net.sci.array.process.PowerOfTwo;
import net.sci.array.process.SobelGradient;
import net.sci.array.process.Sqrt;
import net.sci.array.process.shape.Flip;
import net.sci.image.Image;
import net.sci.image.process.ImageThreshold;
import net.sci.image.process.SobelGradientNorm;

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

		// Color conversion items
		// JMenu colorMenu = new JMenu("Color");
//		addMenuItem(editMenu, new SplitImageChannelsAction(frame,
//				"splitChannels"), "Split Channels", isVector);
		// addMenuItem(editMenu, new MetaImageOperatorAction(frame,
		// "colorToGray",
		// new Gray8Converter()), "RGB -> Gray8", isColor);
		// // editMenu.add(colorMenu);
		// addMenuItem(editMenu, new MetaImageOperatorAction(frame, "invert",
		// new Inverter()), "Invert", isImage);
//		editMenu.addSeparator();

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
				isScalar | isVector);
		addMenuItem(displayRangeMenu, new SetImageDisplayRangeAction(frame, "setImageDisplayRange"), "Set Image Display Range",
				isScalar | isVector);
		addMenuItem(displayRangeMenu, new SetManualDisplayRangeAction(frame, "setManualDisplayRange"), "Set Manual Display Range",
				isScalar | isVector);
		// addMenuItem(editMenu, new SetDisplayRangeUnitIntervalAction(frame,
		// "setDisplayRangeUnitInterval"),
		// "Set Display Range [0 ; 1]", isScalar || isVector);
		menu.add(displayRangeMenu);


		JMenu geometryMenu = new JMenu("Geometry");
		geometryMenu.setEnabled(isImage);
		addMenuItem(geometryMenu, new ImageArrayOperatorAction(frame,
				"flipXFilter", new Flip(0)), "Flip Horizontal", isImage);
		addMenuItem(geometryMenu, 
				new MiddleSliceImageAction(frame, "middleSlice"), "Middle Slice", is3D);
		// addMenuItem(geometryMenu,
		// new ImageOperatorAction(frame, "flipYFilter", new Flip(1)),
		// "Flip Vertical", isImage);
		// addMenuItem(geometryMenu,
		// new ImageOperatorAction(frame, "flipZFilter", new Flip(2)),
		// "Flip Z", has3D);
		// addMenuItem(geometryMenu,
		// new ImageOperatorAction(frame, "rotation90", new Rotation90()),
		// "Rotation 90", has2D);
		// addMenuItem(geometryMenu,
		// new ImageOperatorAction(frame, "rotateImage", new Rotation(30)),
		// "Rotate Array<?>", isImage);

		menu.add(geometryMenu);

		JMenu stackMenu = new JMenu("Stacks");
		stackMenu.setEnabled(is3D);

//		addMenuItem(stackMenu, new StackSliceAction(frame, "stackSlice"),
//				"Get Slice Stack", has3D);
//
//		menu.add(stackMenu);
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
		boolean hasScalar = hasScalarImage(frame);
		// boolean has2D = has2DImage(frame);

		JMenu menu = new JMenu("Process");
		addMenuItem(menu,
				new ImageArrayOperatorAction(frame, "sqrt", new Sqrt()),
				"Sqrt", hasScalar);
		addMenuItem(menu,
				new ImageArrayOperatorAction(frame, "powerOfTwo", new PowerOfTwo()),
				"PowerOfTwo", hasScalar);
		
		menu.addSeparator();
		addMenuItem(menu,
				new ImageOperatorAction(frame, "threshold", new ImageThreshold(20)),
				"Threshold (20)", hasScalar);

//		addMenuItem(menu, new BoxFilter2D11x11Action(frame, "boxFilter11x11"),
//				"Box Filter 11x11", isImage);
//		addMenuItem(menu, new BoxFilter3x3(frame, "boxFilter3x3"),
//				"Box Filter 3x3", isImage);
//		addMenuItem(menu, new BoxFilterAction(frame, "boxFilter"), "Box Filter",
//				isImage);
//		addMenuItem(menu, new BoxFilter3x3Float(frame, "boxFilter3x3Float"),
//				"Box Filter 3x3 (float)", isImage);

//		menu.addSeparator();
		// addMenuItem(menu, new ImageOperatorAction(frame,
		// "medianFilter5x5scalar",
		// new MedianFilterBox2D(5, 5)),
		// "Median Filter 5x5 (int & float)", isImage);
		// addMenuItem(menu, new ImageOperatorAction(frame,
		// "medianFilter3x3scalar",
		// new MedianFilter2D_3x3()),
		// "Median Filter 3x3 (int & float)", isImage);
		// addMenuItem(menu, new Dilation2D11x11Action(frame, "dilation11x11"),
		// "Dilation 11x11", isImage);

//		menu.addSeparator();
//		addMenuItem(menu, new ImageLogAction(frame, "imageLog"), "Array<?> Log",
//				isImage);

		menu.addSeparator();
		addMenuItem(menu, new ImageOperatorAction(frame, "sobelGradientNorm",
				new SobelGradientNorm()), "Sobel Gradient Norm", isImage);
		addMenuItem(menu, new ImageArrayOperatorAction(frame, "sobelGradient",
				new SobelGradient()), "Sobel Gradient", isImage);
		// addMenuItem(menu, new ImageOperatorAction(frame, "vectorNorm",
		// new VectorImageNorm()),
		// "Array<?> Norm", isImage);
		// addMenuItem(menu, new ImageOperatorAction(frame, "vectorAngle",
		// new VectorImageAngle()),
		// "Array<?> Angle", isImage);

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

		//TODO: should be more generic (include COLOR and Complex images)
		return doc.getImage().getType() == Image.Type.VECTOR;
	}

	private final static boolean hasColorImage(ImagoFrame frame)
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

	private final static boolean has2DImage(ImagoFrame frame)
	{
		ImagoDoc doc = null;
		if (frame instanceof ImagoDocViewer)
		{
			doc = ((ImagoDocViewer) frame).getDocument();
		}
		if (doc == null)
			return false;

		boolean is2D = false;
		Array<?> img = doc.getImage().getData();
		if (img instanceof Array2D) 
		{
			// TODO: should test image dimensions instead
			is2D = true;
		}
		return is2D;
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

		boolean is3D = false;
		Array<?> img = doc.getImage().getData();
		if (img instanceof Array3D)
		{
			// TODO: should test image dimensions instead
				is3D = true;
		}
		return is3D;
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
}
