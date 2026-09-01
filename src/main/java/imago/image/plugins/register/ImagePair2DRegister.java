/**
 * 
 */
package imago.image.plugins.register;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.stream.JsonWriter;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.util.GuiHelper;
import imago.gui.widgets.NumericValueTextField;
import imago.gui.widgets.NumericValueTextIncDecWidget;
import imago.gui.widgets.WidgetListener;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.interp.LinearInterpolatedArray2D;
import net.sci.geom.geom2d.AffineTransform2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.Transform2D;
import net.sci.image.Image;
import net.sci.register.image.TransformedImage2D;
import net.sci.register.transform.CenteredMotion2D;
import net.sci.register.transform.CenteredSimilarity2D;
import net.sci.register.transform.TranslationModel2D;

/**
 * Provides a simple interface to quickly register two 2D images. 
 */
public class ImagePair2DRegister implements FramePlugin
{
    // ===================================================================
    // Enumerations
    
    public enum DisplayType
    {
        CHECKERBOARD("Checkerboard"), 
        MAGENTA_GREEN("Magenta-Green"),
        DIFFERENCE("Difference"),
        ABSOLUTE_DIFFERENCE("Abs. Difference"),
        MAX_INTENSITY("Max Of Intensities"),
        AVERAGE("Average Intensity"),
        SUM("Sum Of Intensities");
        
        String label;
        
        DisplayType(String label)
        {
            this.label = label;
        }
        
        @Override
        public String toString()
        {
            return label;
        }
    }
    
    // ===================================================================
    // Class properties
    
    ImagoFrame parentFrame;
    
    Image refImage;
    
    Image movingImage;
    
    int[] outputImageDims;
    ScalarArray.Factory<?> outputArrayFactory = UInt8Array.defaultFactory;
    
    protected DisplayType displayType = DisplayType.MAGENTA_GREEN;
  
    /** the translation vector (in pixels) */
    double xShift = 0.0;
    double yShift = 0.0;
    
    /** rotation angle (degrees)*/
    double rotationAngle = 0.0;
    
    /** binary logarithm of the scaling factor (for Similarity transform) */
    double logScaling = 0.0;
    
    boolean validParams = true;

    /** The transform model from reference space to moving image space */
    Transform2D transform = new TranslationModel2D();
    
    /** The reference image displayed in the new basis */
    Image registeredImage1;
    
    /** The result of the transform applied on the moving image */
    Image registeredImage2; 
    
    SingleImageDisplayFrame resultDisplay = null;
    
    Image resultImage = null;
    
    JFrame pluginFrame = null;

    JFileChooser saveWindow = null;
    
    
    // ----------------------------------------------------
    // GUI Widgets
    
    JComboBox<String> imageNames1Combo;
    JComboBox<String> imageNames2Combo;
    
    JComboBox<String> transformModelCombo;

    NumericValueTextField outputSizeXWidget;
    NumericValueTextField outputSizeYWidget;
    JComboBox<DisplayType> displayTypeCombo;
    
    JLabel xShiftLabel;
    NumericValueTextIncDecWidget xShiftWidget;
    JLabel yShiftLabel;
    NumericValueTextIncDecWidget yShiftWidget;
    JLabel rotationAngleLabel;
    NumericValueTextIncDecWidget rotationAngleWidget;
    JLabel logScalingLabel;
    NumericValueTextIncDecWidget logScalingWidget;
    
    JCheckBox autoUpdateCheckBox;
    JButton runButton;

    boolean isComputing = false;
    
    
    // ===================================================================
    // Implementation of the Plugin interface    
    
    @Override
    public void run(ImagoFrame frame, String args)
    {
        this.parentFrame = frame;
        
        // build control frame
        this.pluginFrame = new JFrame("Simple Registration");
        if (parentFrame != null)
        {
            Point pos = parentFrame.getWidget().getLocation();
            this.pluginFrame.setLocation(pos.x + 30, pos.y + 20);
        }
        
        // initialize size of output image from size of current image
        if (frame instanceof ImageFrame)
        {
            Image img = ((ImageFrame) frame).getImageHandle().getImage();
            this.outputImageDims = img.getSize();
        }
        
        // create frame
        initWidgets();
        setupLayout(pluginFrame);
        setupMenu(pluginFrame);
        
        pluginFrame.pack();
        pluginFrame.setVisible(true);
    }
    

    // ====================================================
    // Main processing methods
 
    /**
     * The main processing method. It applies several processing steps:
     * <ul>
     * <li> Retrieve input arguments </li>
     * <li> Compute the transform </li>
     * <li> Apply transform to moving image</li>
     * <li> Compute result image showing result</li>
     * </ul>
     */
    private void runRegistration()
    {
        updateInputImages();
        
        // need to update transform after updating images (to compute center)
        updateTransform();
        
        // avoid running heavy computation many times
        if (!isComputing)
        {
            isComputing = true;
            updateResultImage();
            updateResultDisplay();
            isComputing = false;
        }
    }
    
    private void updateInputImages()
    {
        // retrieve name of images
        String imageName1 = (String) this.imageNames1Combo.getSelectedItem();
        String imageName2 = (String) this.imageNames2Combo.getSelectedItem();
        
        // retrieve image data
        ImagoApp app = this.parentFrame.getGui().getAppli();
        this.refImage = ImageHandle.findFromName(app, imageName1).getImage();
        this.movingImage = ImageHandle.findFromName(app, imageName2).getImage();
    }
    
    public void updateTransform()
    {
        // pre-compute center
        double sizeX = this.refImage.getSize(0);
        double sizeY = this.refImage.getSize(1);
        Point2D center = new Point2D(sizeX / 2, sizeY / 2);
        
        // parse translation params
        this.xShift = xShiftWidget.getValue();
        this.yShift = yShiftWidget.getValue();
        
        // parse rotation angle (degrees)
        if (this.transformModelCombo.getSelectedIndex() > 0)
        {
            this.rotationAngle = rotationAngleWidget.getValue();
        }

        // parse scaling factor
        if (this.transformModelCombo.getSelectedIndex() > 1)
        {
            this.logScaling = logScalingWidget.getValue();
        }

        int transfoIndex = this.transformModelCombo.getSelectedIndex();
        this.transform = switch (transfoIndex)
        {
            case 0 -> new TranslationModel2D(this.xShift, this.yShift);
            case 1 -> new CenteredMotion2D(center, this.rotationAngle, this.xShift, this.yShift);
            case 2 -> new CenteredSimilarity2D(center, this.logScaling, this.rotationAngle, this.xShift, this.yShift);
            default -> throw new RuntimeException(
                    "This transformation is not implemented: " + this.transform.getClass().getName());
        };
    }

    /**
     * Creates a new empty result image from the two input images.
     */
    public void updateResultImage()
    {
        // apply transform on fixed image
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ScalarArray2D<?> ref2d = ScalarArray2D.wrap(ScalarArray.wrap((Array<Scalar>) refImage.getData()));
        TransformedImage2D tim1 = new TransformedImage2D(new LinearInterpolatedArray2D(ref2d, Double.NaN), AffineTransform2D.IDENTITY);
        
        // apply transform on moving image
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ScalarArray2D<?> moving2d = ScalarArray2D.wrap(ScalarArray.wrap((Array<Scalar>) movingImage.getData()));
        TransformedImage2D tim2 = new TransformedImage2D(new LinearInterpolatedArray2D(moving2d, Double.NaN), transform);
        
        outputImageDims[0] = (int) outputSizeXWidget.getValue();
        outputImageDims[1] = (int) outputSizeYWidget.getValue();
        int sizeX = (int) outputSizeXWidget.getValue();
        int sizeY = (int) outputSizeYWidget.getValue();
        
        FunctionPairRenderer2D compositer = switch (displayType)
        {
            case MAGENTA_GREEN -> new FunctionPairRenderer2D.MagentaGreen(sizeX, sizeY);
            case MAX_INTENSITY -> new FunctionPairRenderer2D.MaxIntensity(sizeX, sizeY);
            case AVERAGE -> new FunctionPairRenderer2D.AverageIntensity(sizeX, sizeY);
            case DIFFERENCE -> new FunctionPairRenderer2D.Difference(sizeX, sizeY);
            case ABSOLUTE_DIFFERENCE -> new FunctionPairRenderer2D.AbsoluteDifference(sizeX, sizeY);
            case SUM -> new FunctionPairRenderer2D.IntensitySum(sizeX, sizeY);
            case CHECKERBOARD -> new FunctionPairRenderer2D.CheckerBoard(sizeX, sizeY);
            default -> throw new IllegalArgumentException("Unexpected value: " + displayType);
        };

        Array<?> resultArray = compositer.combine(tim1, tim2);
        this.resultImage = new Image(resultArray, refImage);
    }
    
    /**
     * Updates the current display of result, by combining the result of
     * registration with the reference image.
     */
    public void updateResultDisplay()
    {
        if (resultDisplay == null)
        {
            this.resultDisplay = SingleImageDisplayFrame.create(resultImage, this.parentFrame);
        } 
        else
        {
            SingleImageViewer viewer = this.resultDisplay.getViewer();
            viewer.setImage(resultImage);
            viewer.refreshDisplay();
            this.resultDisplay.repaint();
        }
    }
    
    /**
     * Callback for the creating a composite image.
     */
    private void onCreateComboImage()
    {
        updateResultImage();
        updateResultDisplay();
        
        Image res = this.resultDisplay.getViewer().getImage();
        res.setName(movingImage.getName() + "regCompo");
        ImageFrame.create(res, parentFrame);
    }
    
    /**
     * Callback for the "Save Registration" menu item.
     */
    private void onSaveRegistration()
    {
        // create file dialog using last save path
        String pattern = "register_%s_to_%s.json";
        String defaultName = String.format(pattern, movingImage.getName(), refImage.getName());
        JFileChooser saveWindow = new JFileChooser(new File(defaultName));
        saveWindow.setDialogTitle("Save Registration Data");
        FileFilter jsonFileFilter = new FileNameExtensionFilter("JSON files (*.json)", "json");
        saveWindow.addChoosableFileFilter(jsonFileFilter);
        saveWindow.addChoosableFileFilter(new FileNameExtensionFilter("All files (*.*)", "*"));
        saveWindow.setFileFilter(jsonFileFilter);

        // Open dialog to choose the file
        int ret = saveWindow.showSaveDialog(pluginFrame);
        if (ret != JFileChooser.APPROVE_OPTION) 
        {
            return;
        }

        // Check the chosen file is valid
        File file = saveWindow.getSelectedFile();
        if (!file.getName().endsWith(".json"))
        {
            File parent = file.getParentFile();
            file = new File(parent, file.getName() + ".json");
        }
        
        try 
        {
            // open a text file to write JSON data
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            JsonWriter jsonWriter = new JsonWriter(new PrintWriter(fileWriter));
            jsonWriter.setIndent("  ");
            
            // wrap into a Registration writer
            JsonRegistrationWriter writer = new JsonRegistrationWriter(jsonWriter);
            writer.writeRegistrationInfo(refImage, movingImage, transform);

            fileWriter.close();
       }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }


    // ===================================================================
    // Implementation of the Plugin interface    

    private void initWidgets()
    {
        ImagoApp app = this.parentFrame.getGui().getAppli();
        String[] imageNames = ImageHandle.getAllNames(app).toArray(new String[]{});
        this.imageNames1Combo = new JComboBox<String>(imageNames);
        this.imageNames1Combo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                updateInputImages();
                if (this.autoUpdateCheckBox.isSelected()) runRegistration();
            }
        });
        this.imageNames2Combo = new JComboBox<String>(imageNames);
        this.imageNames2Combo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                updateInputImages();
                if (this.autoUpdateCheckBox.isSelected()) runRegistration();
            }
        });
        
        
        this.outputSizeXWidget = new NumericValueTextField(this.outputImageDims[0]);
        this.outputSizeXWidget.addWidgetListener(evt -> {
            if (this.autoUpdateCheckBox.isSelected())
            {
                runRegistration();
            }
        });
        this.outputSizeYWidget = new NumericValueTextField(this.outputImageDims[1]);
        this.outputSizeYWidget.addWidgetListener(evt -> {
            if (this.autoUpdateCheckBox.isSelected())
            {
                runRegistration();
            }
        });
        
        // create widget for choosing composite type
        DisplayType[] items = EnumSet.allOf(DisplayType.class).toArray(new DisplayType[] {});
        displayTypeCombo = new JComboBox<DisplayType>(items);
        displayTypeCombo.setSelectedItem(DisplayType.MAGENTA_GREEN);
        displayTypeCombo.addItemListener(evt ->
        {
            if (evt.getStateChange() != ItemEvent.SELECTED) return;
            this.displayType = (DisplayType) evt.getItem();
            updateResultImage();
            updateResultDisplay();
        });

        this.transformModelCombo = new JComboBox<String>();
        this.transformModelCombo.addItem("Translation");
        this.transformModelCombo.addItem("Motion (Trans.+Rot.)");
        this.transformModelCombo.addItem("Similarity (Trans.+Rot.+Scal.)");
        this.transformModelCombo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED) 
            {
                updateEnabledRegistrationWidgets();
                if (this.autoUpdateCheckBox.isSelected()) runRegistration();
            }
        });
        
        WidgetListener listener = evt -> {
            if (this.autoUpdateCheckBox.isSelected())
            {
                runRegistration();
            }
        };
        this.xShiftLabel = new JLabel("Shift X (pixels):");
        this.xShiftWidget = new NumericValueTextIncDecWidget(0.0, 1.0);
        this.xShiftWidget.addWidgetListener(listener);

        this.yShiftLabel = new JLabel("Shift Y (pixels):");
        this.yShiftWidget = new NumericValueTextIncDecWidget(0.0, 1.0);
        this.yShiftWidget.addWidgetListener(listener);

        this.rotationAngleLabel = new JLabel("Rotation angle (degrees):");
        this.rotationAngleWidget = new NumericValueTextIncDecWidget(0.0, 1.0);
        this.rotationAngleWidget.addWidgetListener(listener);

        this.logScalingLabel = new JLabel("Log_2 of scaling factor:");
        this.logScalingWidget = new NumericValueTextIncDecWidget(0.0, 0.01);
        this.logScalingWidget.addWidgetListener(listener);
        
        this.autoUpdateCheckBox = new JCheckBox("Auto-Update", false);
        this.autoUpdateCheckBox.addActionListener(evt -> runRegistration());

        this.runButton = new JButton("Run");
        this.runButton.addActionListener(evt -> runRegistration());
    }
    
    private void updateEnabledRegistrationWidgets()
    {
        if (transformModelCombo.getSelectedIndex() == 0)
        {
            this.rotationAngleLabel.setEnabled(false);
            this.rotationAngleWidget.setEnabled(false);
            this.logScalingLabel.setEnabled(false);
            this.logScalingWidget.setEnabled(false);
        }
        else if (transformModelCombo.getSelectedIndex() == 1)
        {
            this.rotationAngleLabel.setEnabled(true);
            this.rotationAngleWidget.setEnabled(true);
            this.logScalingLabel.setEnabled(false);
            this.logScalingWidget.setEnabled(false);
        }
        else if (transformModelCombo.getSelectedIndex() == 2)
        {
            this.rotationAngleLabel.setEnabled(true);
            this.rotationAngleWidget.setEnabled(true);
            this.logScalingLabel.setEnabled(true);
            this.logScalingWidget.setEnabled(true);
        }
    }
    
    private void setupLayout(JFrame frame)
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        
        JPanel imagesPanel = GuiHelper.createOptionsPanel("Images");
        imagesPanel.setLayout(new GridLayout(2, 2));
        imagesPanel.add(new JLabel("Reference Image: "));
        imagesPanel.add(this.imageNames1Combo);
        imagesPanel.add(new JLabel("Moving Image: "));
        imagesPanel.add(this.imageNames2Combo);

        JPanel outputImagePanel = GuiHelper.createOptionsPanel("Output Image");
        outputImagePanel.setLayout(new GridLayout(3, 2));
        outputImagePanel.add(new JLabel("Size X: "));
        outputImagePanel.add(outputSizeXWidget.getComponent());
        outputImagePanel.add(new JLabel("Size Y: "));
        outputImagePanel.add(outputSizeYWidget.getComponent());
        outputImagePanel.add(new JLabel("Display Type: "));
        outputImagePanel.add(displayTypeCombo);

        JPanel registrationPanel = GuiHelper.createOptionsPanel("Registration");
        registrationPanel.setLayout(new GridLayout(5, 2));
        registrationPanel.add(new JLabel("Registration Type:"));
        registrationPanel.add(transformModelCombo);
        registrationPanel.add(xShiftLabel);
        registrationPanel.add(xShiftWidget.getComponent());
        registrationPanel.add(yShiftLabel);
        registrationPanel.add(yShiftWidget.getComponent());
        registrationPanel.add(rotationAngleLabel);
        registrationPanel.add(rotationAngleWidget.getComponent());
        registrationPanel.add(logScalingLabel);
        registrationPanel.add(logScalingWidget.getComponent());
        updateEnabledRegistrationWidgets();
        
        mainPanel.add(imagesPanel);
        mainPanel.add(outputImagePanel);
        mainPanel.add(registrationPanel);
         
        GuiHelper.addInLine(mainPanel, FlowLayout.CENTER, autoUpdateCheckBox, runButton);
        
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupMenu(JFrame frame)
    {
        // init menu items
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        addMenuItem(fileMenu, "Display Registered Image 1", evt -> ImageFrame.create(registeredImage1, parentFrame));
        addMenuItem(fileMenu, "Display Registered Image 2", evt -> ImageFrame.create(registeredImage2, parentFrame));
        addMenuItem(fileMenu, "Create Registration Composite Image", evt -> onCreateComboImage());
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Save Registration...", evt -> onSaveRegistration());
        menuBar.add(fileMenu);
        
        frame.setJMenuBar(menuBar);
    }
    
    private void addMenuItem(JMenu menu, String label, ActionListener listener)
    {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(listener);
        menu.add(item);
    }
}
