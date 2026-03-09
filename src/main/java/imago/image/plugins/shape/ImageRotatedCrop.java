/**
 * 
 */
package imago.image.plugins.shape;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.util.GuiHelper;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.image.viewers.ImageDisplay;
import imago.image.viewers.XYImageViewer;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.Array3D;
import net.sci.array.color.RGB8Array;
import net.sci.array.color.RGB8Array2D;
import net.sci.array.color.RGB8Array3D;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.array.numeric.UInt8;
import net.sci.array.numeric.UInt8Array2D;
import net.sci.array.numeric.UInt8Array3D;
import net.sci.array.numeric.interp.ScalarFunction3D;
import net.sci.geom.geom2d.AffineTransform2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom3d.AffineTransform3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.image.Image;
import net.sci.register.image.TransformedImage2D;
import net.sci.register.image.TransformedImage3D;

/**
 * @author dlegland
 *
 */
public class ImageRotatedCrop implements FramePlugin
{
    // ==================================================
    // Static methods
 
    public static final Array2D<?> rotatedCrop(Array2D<?> array, OrientedBox2D box)
    {
        int[] dims = new int[] {box.sizeX, box.sizeY};
        AffineTransform2D transfo = box.localToGlobalTransform();
        
        // create rotated array (as view)
        if (array instanceof ScalarArray<?> scalar)
        {
            return new UInt8TransformedArray2D(dims, ScalarArray2D.wrap(scalar), transfo);
        }
        else if (array instanceof RGB8Array rgbArray)
        {
            RGB8Array2D rgb2d = RGB8Array2D.wrap(rgbArray);
            UInt8Array2D red   = new UInt8TransformedArray2D(dims, rgb2d.channel(0), transfo);
            UInt8Array2D green = new UInt8TransformedArray2D(dims, rgb2d.channel(1), transfo);
            UInt8Array2D blue  = new UInt8TransformedArray2D(dims, rgb2d.channel(2), transfo);
            return RGB8Array2D.wrap(RGB8Array.mergeChannels(red, green, blue));
        }
        else
        {
            throw new IllegalArgumentException("Requires a scalar array as input");
        }
    }

    public static final Array3D<?> rotatedCrop(Array3D<?> array, OrientedBox3D box)
    {
        // Computes the transform that will map indices from within result image
        // into coordinates within source image
        int[] dims = new int[] {box.sizeX, box.sizeY, box.sizeZ};
        AffineTransform3D transfo = box.localToGlobalTransform();
        
        if (array instanceof ScalarArray3D<?>)
        {
            return rotatedCropScalar((ScalarArray3D<?>) array, dims, transfo);
        }
        else if (array instanceof RGB8Array3D)
        {
            // allocate result image
            RGB8Array3D rgbArray = (RGB8Array3D) array;
            UInt8Array3D red   = (UInt8Array3D) rotatedCropScalar(rgbArray.channel(0), dims, transfo);
            UInt8Array3D green = (UInt8Array3D) rotatedCropScalar(rgbArray.channel(1), dims, transfo);
            UInt8Array3D blue  = (UInt8Array3D) rotatedCropScalar(rgbArray.channel(2), dims, transfo);
            RGB8Array3D res = (RGB8Array3D) RGB8Array.mergeChannels(red, green, blue);
            return res;
        }
        else
        {
            throw new IllegalArgumentException("Requires a scalar array as input");
        }
    }
    
    private static final ScalarArray3D<?> rotatedCropScalar(ScalarArray3D<?> image, int[] dims, AffineTransform3D transfo)
    {
        // Create interpolation class, that encapsulates both the image and the
        // transform
        ScalarFunction3D interp = new TransformedImage3D(image, transfo);

        // allocate result image
        ScalarArray3D<?> res = ScalarArray3D.wrapScalar3d(image.newInstance(dims[0], dims[1], dims[2]));
        res.fillValues((x,y,z) -> interp.evaluate(x, y, z));
        
        return res;
    }
    
    private static final ScalarArray2D<?> orthoSlices(ScalarFunction3D fun, int[] stackDims)
    {
        int sizeX = stackDims[0];
        int sizeY = stackDims[1];
        int sizeZ = stackDims[2];
        
        int posX = sizeX / 2;
        int posY = sizeY / 2;
        int posZ = sizeZ / 2;
        
        int sizeX2 = 2 * sizeX + sizeY;
        int sizeY2 = Math.max(sizeY,  sizeZ);
        UInt8Array2D res = UInt8Array2D.create(sizeX2, sizeY2);
        
        // add XY slice
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                res.setInt(x, y, (int) fun.evaluate(x, y, posZ));
            }
        }
        
        // add XZ slice
        for (int z = 0; z < sizeZ; z++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                res.setInt(x + sizeX, z, (int) fun.evaluate(x, posY, z));
            }
        }
        
        // add YZ slice
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                res.setInt(y + 2 * sizeX, z, (int) fun.evaluate(posX, y, z));
            }
        }
        
        return res;
    }
    

    // ==================================================
    // Implementation of the FramePlugin interface
 
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        // switch processing according to image dimensionality
        int nd = array.dimensionality();
        if (nd == 2)
        {
            // use center of image as default position for box center
            int sizeX = array.size(0);
            int sizeY = array.size(1);
            Point2D refPoint = new Point2D(sizeX * 0.5, sizeY * 0.5);
            
            int[] dims = new int[] {sizeX, sizeY};
            SettingsFrame2D settingsFrame = new SettingsFrame2D(imageFrame, dims, refPoint, 0.0);
            settingsFrame.setVisible(true);
        }
        else if (nd == 3)
        {
            // use center of image as default position for box center
            int sizeX = array.size(0);
            int sizeY = array.size(1);
            int sizeZ = array.size(2);
            Point3D refPoint = new Point3D(sizeX * 0.5, sizeY * 0.5, sizeZ * 0.5);

            int[] dims = new int[] {sizeX, sizeY, sizeZ};
            double[] angles = new double[] {0.0, 0.0, 0.0};

            SettingsFrame3D settingsFrame = new SettingsFrame3D(imageFrame, dims, refPoint, angles);
            settingsFrame.setVisible(true);
            
            ImageViewer viewer = imageFrame.getImageViewer();
            if (viewer instanceof XYImageViewer)
            {
                ImageDisplay imageDisplay = ((XYImageViewer) viewer).getImageDisplay();
                imageDisplay.addMouseListener(settingsFrame);
            }
        }
        else
        {
            frame.showErrorDialog("Requires a 2D or 3D image as input", "Input Image error");
            return;
        }
    }
 
    
    // ==================================================
    // Inner classes
 
    /**
     * Concatenates the settings for the choice of oriented box.
     */
    class OrientedBox2D
    {
        int sizeX;
        int sizeY;
        double centerX;
        double centerY;
        double angle;
        
        public OrientedBox2D()
        {
        }
        
        public OrientedBox2D(int[] boxSize, Point2D refPoint, double rotAngle)
        {
            this.sizeX = boxSize[0];
            this.sizeY = boxSize[1];
            this.centerX = refPoint.x();
            this.centerY = refPoint.y();
            this.angle = rotAngle;
        }
        
        /**
         * Computes the affine transform that maps a points in the box inner
         * coordinate system to the global coordinate system.
         * 
         * @return the transform between local and global coordinate system
         */
        public AffineTransform2D localToGlobalTransform()
        {
            // create elementary transforms
            AffineTransform2D trBoxCenter = AffineTransform2D.createTranslation(-sizeX / 2, -sizeY / 2);
            AffineTransform2D rot = AffineTransform2D.createRotation(Math.toRadians(angle));
            AffineTransform2D trRefPoint = AffineTransform2D.createTranslation(centerX, centerY);

            // concatenate into global transform
            return AffineTransform2D.compose(trRefPoint, rot, trBoxCenter);
        }
    }
    
    /**
     * Concatenates the settings for the choice of 3D oriented box.
     */
    class OrientedBox3D
    {
        int sizeX;
        int sizeY;
        int sizeZ;
        double centerX;
        double centerY;
        double centerZ;
        double rotAngleX;
        double rotAngleY;
        double rotAngleZ;

        public OrientedBox3D()
        {
        }
        
        public OrientedBox3D(int[] dims, Point3D refPoint, double[] anglesInDegrees)
        {
            this.sizeX = dims[0];
            this.sizeY = dims[1];
            this.sizeZ = dims[2];
            this.centerX = refPoint.x();
            this.centerY = refPoint.y();
            this.centerZ = refPoint.z();
            this.rotAngleX = anglesInDegrees[0];
            this.rotAngleY = anglesInDegrees[1];
            this.rotAngleZ = anglesInDegrees[2];
        }
        
        /**
         * Computes the affine transform that maps a points in the box inner
         * coordinate system to the global coordinate system.
         * 
         * @return the transform between local and global coordinate system
         */
        public AffineTransform3D localToGlobalTransform()
        {
            // translation of origin to the center of the box
            AffineTransform3D trBoxCenter = AffineTransform3D.createTranslation(-sizeX / 2, -sizeY / 2, -sizeZ / 2);
            
            // three rotation by Euler angles 
            AffineTransform3D rotX = AffineTransform3D.createRotationOx(Math.toRadians(rotAngleX));
            AffineTransform3D rotY = AffineTransform3D.createRotationOy(Math.toRadians(rotAngleY));
            AffineTransform3D rotZ = AffineTransform3D.createRotationOz(Math.toRadians(rotAngleZ));
            
            // translation of box center to reference point
            AffineTransform3D trRefPoint = AffineTransform3D.createTranslation(centerX, centerY, centerZ);
            
            // concatenate into global transform
            return AffineTransform3D.compose(trRefPoint, rotZ, rotY, rotX, trBoxCenter);
        }
    }


    public class SettingsFrame2D extends JFrame implements MouseListener
    {
        // ====================================================
        // Static fields

        /**
         * Version ID.
         */
        private static final long serialVersionUID = 1L;

        // ====================================================
        // Class properties
        
        ImageFrame parentFrame;
       
        Image image;
        Array2D<?> array;
        
        OrientedBox2D box;
        
        
        // ====================================================
        // GUI Widgets
        
        JSpinner sizeXWidget;
        JSpinner sizeYWidget;
        JSpinner boxCenterXWidget;
        JSpinner boxCenterYWidget;
        JSpinner boxAngleWidget;
        
        JCheckBox autoUpdateCheckBox;
        JButton previewButton;
        JButton runButton;
        
        /** The frame used to preview the result of rotated crop. */
        ImageFrame previewFrame = null;
        
        
        public SettingsFrame2D(ImageFrame parentFrame, int[] boxSize, Point2D refPoint, double rotAngle)
        {
            super("Crop Oriented Box");
            
            this.parentFrame = parentFrame;
            this.image = parentFrame.getImageHandle().getImage();
            this.array = Array2D.wrap(image.getData());
            
            // init default values
            box = new OrientedBox2D(boxSize, refPoint, rotAngle);

            setupWidgets();
            setupLayout();

            this.pack();
            this.centerFrame();
            
            if (parentFrame.getImageViewer() instanceof XYImageViewer xyViewer)
            {
                xyViewer.getImageDisplay().addMouseListener(this);
            }

            setVisible(true);
        }
        
        private void setupWidgets()
        {
            sizeXWidget = GuiHelper.createNumberSpinner(box.sizeX, 0, 10000, 1);
            sizeXWidget.addChangeListener(evt -> 
            {
                this.box.sizeX = ((SpinnerNumberModel) sizeXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            sizeYWidget = GuiHelper.createNumberSpinner(box.sizeY, 0, 10000, 1);
            sizeYWidget.addChangeListener(evt -> 
            {
                this.box.sizeY = ((SpinnerNumberModel) sizeYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterXWidget = GuiHelper.createNumberSpinner(box.centerX, 0, 10000, 1);
            boxCenterXWidget.addChangeListener(evt -> 
            {
                this.box.centerX = ((SpinnerNumberModel) boxCenterXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterYWidget = GuiHelper.createNumberSpinner(box.centerY, 0, 10000, 1);
            boxCenterYWidget.addChangeListener(evt -> 
            {
                this.box.centerY = ((SpinnerNumberModel) boxCenterYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            
            boxAngleWidget = GuiHelper.createNumberSpinner(box.angle, -180, 180, 1);
            boxAngleWidget.addChangeListener(evt -> 
            {
                this.box.angle = ((SpinnerNumberModel) boxAngleWidget.getModel()).getNumber().doubleValue();
                updatePreviewIfNeeded();
            });

            autoUpdateCheckBox = new JCheckBox("Auto-Update", false);
            autoUpdateCheckBox.addItemListener(evt -> updatePreviewIfNeeded());
            
            previewButton = new JButton("Preview");
            previewButton.addActionListener(evt -> updatePreview());

            runButton = new JButton("Create Result!");
            runButton.addActionListener(evt -> createResultImageAndClose());
        }

        private void setupLayout()
        {
            // encapsulate into a main panel
            JPanel mainPanel = new JPanel();
            mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

            JPanel sizePanel = GuiHelper.createOptionsPanel("Result Size");
            sizePanel.setLayout(new GridLayout(2, 2));
            sizePanel.add(new JLabel("Size X:"));
            sizePanel.add(sizeXWidget);
            sizePanel.add(new JLabel("Size Y:"));
            sizePanel.add(sizeYWidget);
            mainPanel.add(sizePanel);
            
            JPanel boxPanel = GuiHelper.createOptionsPanel("Rotated Box");
            boxPanel.setLayout(new GridLayout(3, 2));
            boxPanel.add(new JLabel("Center X:"));
            boxPanel.add(boxCenterXWidget);
            boxPanel.add(new JLabel("Center Y:"));
            boxPanel.add(boxCenterYWidget);
            boxPanel.add(new JLabel("Angle (degrees):"));
            boxPanel.add(boxAngleWidget);
            mainPanel.add(boxPanel);
            
            // also add buttons
            GuiHelper.addInLine(mainPanel, FlowLayout.CENTER, autoUpdateCheckBox, previewButton);
            GuiHelper.addInLine(mainPanel, FlowLayout.CENTER, runButton);
            
            // put main panel in the middle of frame
            this.setLayout(new BorderLayout());
            this.add(mainPanel, BorderLayout.CENTER);
        }
        
        /**
         * Puts this frame in the center of the screen.
         */
        private void centerFrame()
        {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = this.getSize();
            
            int posX = screenSize.width / 2 - frameSize.width / 2;
            int posY = Math.max((screenSize.height - frameSize.height) / 4, 0);
            this.setLocation(posX, posY);
        }
        
        public void updatePreview()
        {
            Array2D<?> res = rotatedCrop(this.array, box);
            Image previewImage = new Image(res, image);
            previewImage.setName(image.getName() + "-cropPreview");
            
            // retrieve frame for displaying result
            if (this.previewFrame == null)
            {
                this.previewFrame = ImageFrame.create(previewImage, this.parentFrame);
            }
            
            // update display frame
            this.previewFrame.getImageViewer().setPreviewImage(previewImage);
            this.previewFrame.getImageViewer().refreshDisplay();
            this.previewFrame.setVisible(true);
        }

        public void createResultImageAndClose()
        {
            Array2D<?> res = rotatedCrop(this.array, box);
            Image resultImage = new Image(res, image);
            resultImage.setName(image.getName() + "-crop");
            
            ImageFrame refFrame = this.previewFrame != null ? this.previewFrame : this.parentFrame;
            ImageFrame.create(resultImage, refFrame);
            
            if (this.previewFrame != null)
            {
                this.previewFrame.setVisible(false);
                this.previewFrame.close();
            }
            this.setVisible(false);
            
            // remove listener
            if (parentFrame.getImageViewer() instanceof XYImageViewer xyViewer)
            {
                xyViewer.getImageDisplay().removeMouseListener(this);
            }
            
            this.dispose();
        }
        
        private void updatePreviewIfNeeded()
        {
            if (this.autoUpdateCheckBox.isSelected())
            {
                updatePreview();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
        }

        @Override
        public void mousePressed(MouseEvent evt)
        {
            ImageDisplay imageDisplay = (ImageDisplay) evt.getComponent();
            Point p = evt.getPoint();
            Point2D point = imageDisplay.displayToImage(p);
            
            this.box.centerX = point.x();
            ((SpinnerNumberModel) this.boxCenterXWidget.getModel()).setValue(this.box.centerX);
            this.box.centerY = point.y();
            ((SpinnerNumberModel) this.boxCenterYWidget.getModel()).setValue(this.box.centerY);
            
            if (this.autoUpdateCheckBox.isSelected())
            {
                updatePreview();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
        }
    }
    
    
    /**
     * Inner class for managing the frame containing control widgets.
     */
    public class SettingsFrame3D extends JFrame implements MouseListener
    {
        // ====================================================
        // Static fields

        /**
         * Version ID.
         */
        private static final long serialVersionUID = 1L;

        // ====================================================
        // Class properties
        
        ImageFrame parentFrame;
       
        Image image;
        ScalarArray3D<?> array;
        
        OrientedBox3D box;
        

        // ====================================================
        // GUI Widgets
        
        JSpinner sizeXWidget;
        JSpinner sizeYWidget;
        JSpinner sizeZWidget;
        JSpinner boxCenterXWidget;
        JSpinner boxCenterYWidget;
        JSpinner boxCenterZWidget;
        JSpinner boxRotZWidget;
        JSpinner boxRotYWidget;
        JSpinner boxRotXWidget;

        JCheckBox autoPreviewCheckBox;
        JButton previewButton;
        JButton runButton;
        
        /** The frame used to display the result of rotated crop. */
        ImageFrame previewFrame = null;
        

        // ====================================================
        // Constructor

        public SettingsFrame3D(ImageFrame parentFrame, int[] boxSize, Point3D refPoint, double[] rotAngles)
        {
            super("Crop Oriented Box");
            
            this.parentFrame = parentFrame;
            this.image = parentFrame.getImageHandle().getImage();
            this.array = ScalarArray3D.wrapScalar3d((ScalarArray<?>) image.getData());
            
            // init default values
            this.box = new OrientedBox3D(boxSize, refPoint, rotAngles);

            setupWidgets();
            setupLayout();

            this.pack();
            this.centerFrame();
            
            setVisible(true);
        }
        
        private void setupWidgets()
        {
            sizeXWidget = GuiHelper.createNumberSpinner(box.sizeX, 0, 10000, 1);
            sizeXWidget.addChangeListener(evt -> 
            {
                this.box.sizeX = ((SpinnerNumberModel) sizeXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            sizeYWidget = GuiHelper.createNumberSpinner(box.sizeY, 0, 10000, 1);
            sizeYWidget.addChangeListener(evt -> 
            {
                this.box.sizeY = ((SpinnerNumberModel) sizeYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            sizeZWidget = GuiHelper.createNumberSpinner(box.sizeZ, 0, 10000, 1);
            sizeZWidget.addChangeListener(evt -> 
            {
                this.box.sizeZ = ((SpinnerNumberModel) sizeZWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterXWidget = GuiHelper.createNumberSpinner(box.centerX, 0, 10000, 1);
            boxCenterXWidget.addChangeListener(evt -> 
            {
                this.box.centerX = ((SpinnerNumberModel) boxCenterXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterYWidget = GuiHelper.createNumberSpinner(box.centerY, 0, 10000, 1);
            boxCenterYWidget.addChangeListener(evt -> 
            {
                this.box.centerY = ((SpinnerNumberModel) boxCenterYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterZWidget = GuiHelper.createNumberSpinner(box.centerZ, 0, 10000, 1);
            boxCenterZWidget.addChangeListener(evt -> 
            {
                this.box.centerZ = ((SpinnerNumberModel) boxCenterZWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            
            boxRotXWidget = GuiHelper.createNumberSpinner(box.rotAngleX, -180, 180, 1);
            boxRotXWidget.addChangeListener(evt -> 
            {
                this.box.rotAngleX = ((SpinnerNumberModel) boxRotXWidget.getModel()).getNumber().doubleValue();
                updatePreviewIfNeeded();
            });
            
            boxRotYWidget = GuiHelper.createNumberSpinner(box.rotAngleY, -180, 180, 1);
            boxRotYWidget.addChangeListener(evt -> 
            {
                this.box.rotAngleY = ((SpinnerNumberModel) boxRotYWidget.getModel()).getNumber().doubleValue();
                updatePreviewIfNeeded();
            });
            
            boxRotZWidget = GuiHelper.createNumberSpinner(box.rotAngleZ, -180, 180, 1);
            boxRotZWidget.addChangeListener(evt -> 
            {
                this.box.rotAngleZ = ((SpinnerNumberModel) boxRotZWidget.getModel()).getNumber().doubleValue();
                updatePreviewIfNeeded();
            });
            
            autoPreviewCheckBox = new JCheckBox("Auto-Update", false);
            autoPreviewCheckBox.addItemListener(evt -> updatePreviewIfNeeded());

            previewButton = new JButton("Preview");
            previewButton.addActionListener(evt -> updatePreview());
            
            runButton = new JButton("Create Result Image");
            runButton.addActionListener(evt -> displayResult());
        }
        
        private void setupLayout()
        {
            // encapsulate into a main panel
            JPanel mainPanel = new JPanel();
            mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

            JPanel sizePanel = GuiHelper.createOptionsPanel("Result Size");
            sizePanel.setLayout(new GridLayout(3, 2));
            sizePanel.add(new JLabel("Size X:"));
            sizePanel.add(sizeXWidget);
            sizePanel.add(new JLabel("Size Y:"));
            sizePanel.add(sizeYWidget);
            sizePanel.add(new JLabel("Size Z:"));
            sizePanel.add(sizeZWidget);
            mainPanel.add(sizePanel);
            
            JPanel boxPanel = GuiHelper.createOptionsPanel("Box Center");
            boxPanel.setLayout(new GridLayout(3, 2));
            boxPanel.add(new JLabel("Center X:"));
            boxPanel.add(boxCenterXWidget);
            boxPanel.add(new JLabel("Center Y:"));
            boxPanel.add(boxCenterYWidget);
            boxPanel.add(new JLabel("Center Z:"));
            boxPanel.add(boxCenterZWidget);
            mainPanel.add(boxPanel);
            
            JPanel rotationPanel = GuiHelper.createOptionsPanel("Box Rotation");
            rotationPanel.setLayout(new GridLayout(3, 2));
            rotationPanel.add(new JLabel("Rotation X:"));
            rotationPanel.add(boxRotXWidget);
            rotationPanel.add(new JLabel("Rotation Y:"));
            rotationPanel.add(boxRotYWidget);
            rotationPanel.add(new JLabel("Rotation Z:"));
            rotationPanel.add(boxRotZWidget);
            mainPanel.add(rotationPanel);
            
            // also add buttons
            GuiHelper.addInLine(mainPanel, FlowLayout.CENTER, autoPreviewCheckBox, previewButton);
            GuiHelper.addInLine(mainPanel, FlowLayout.CENTER, runButton);
            
            // put main panel in the middle of frame
            this.setLayout(new BorderLayout());
            this.add(mainPanel, BorderLayout.CENTER);
        }
        
        /**
         * Puts this frame in the center of the screen.
         */
        private void centerFrame()
        {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = this.getSize();
            
            int posX = screenSize.width / 2 - frameSize.width / 2;
            int posY = Math.max((screenSize.height - frameSize.height) / 4, 0);
            this.setLocation(posX, posY);
        }
        
        public void updatePreview()
        {
            // compute the transform
            AffineTransform3D transfo = box.localToGlobalTransform();

            // Create interpolation class, that encapsulates both the image and
            // the transform
            TransformedImage3D interp = new TransformedImage3D(this.array, transfo);

            int[] dims = new int[] {box.sizeX, box.sizeY, box.sizeZ};
            ScalarArray2D<?> preview = orthoSlices(interp, dims);
            Image previewImage = new Image(preview, this.image);
            previewImage.setName("Rotated Crop Preview");

            // retrieve frame for displaying result
            if (this.previewFrame == null)
            {
                this.previewFrame = ImageFrame.create(previewImage, this.parentFrame);
            }
            
            // update display frame
            this.previewFrame.getImageViewer().setPreviewImage(previewImage);
            this.previewFrame.getImageViewer().refreshDisplay();
            this.previewFrame.setVisible(true);
        }

        public void displayResult()
        {
            System.out.println("Rot Crop With params: ");
            System.out.println(String.format("  box size: %d x %d x %d", box.sizeX, box.sizeY, box.sizeZ));
            System.out.println(String.format("  refPoint: " + new Point3D(box.centerX, box.centerY, box.centerZ)));
            System.out.println(String.format("  Euler Angles: %5.2f, %5.2f, %5.2f", box.rotAngleX, box.rotAngleY, box.rotAngleZ));
            
            // compute the crop
            Array3D<?> res = rotatedCrop(this.array, box);
            Image resultImage = new Image(res, this.image);
            resultImage.setName(this.image.getName() + "-crop");
            
            // display in a new frame
            ImageFrame.create(resultImage, this.parentFrame);
        }

        private void updatePreviewIfNeeded()
        {
            if (this.autoPreviewCheckBox.isSelected())
            {
                updatePreview();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
        }

        @Override
        public void mousePressed(MouseEvent evt)
        {
            // retrieve position of last mouse click
            ImageDisplay imageDisplay = (ImageDisplay) evt.getComponent();
            Point2D point = imageDisplay.displayToImage(evt.getPoint());
            int zSlice = parentFrame.getImageViewer().getSlicingPosition(2);
            
            // update position of crop box center
            this.box.centerX = point.x();
            ((SpinnerNumberModel) this.boxCenterXWidget.getModel()).setValue(this.box.centerX);
            this.box.centerY = point.y();
            ((SpinnerNumberModel) this.boxCenterYWidget.getModel()).setValue(this.box.centerY);
            this.box.centerZ = zSlice;
            ((SpinnerNumberModel) this.boxCenterZWidget.getModel()).setValue(this.box.centerZ);
            
            if (this.autoPreviewCheckBox.isSelected())
            {
                updatePreview();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
        }
    }
    
    /**
     * Implements a transformed view on a scalar array with results as UInt8.
     */
    public static class UInt8TransformedArray2D extends UInt8Array2D implements Array.View<UInt8>
    {
        /**
         * The array to interpolate. Can be of any scalar type, but only values
         * between 0 and 255 will be correctly converted.
         */
        ScalarArray2D<?> refArray;
        
        /**
         * The interpolation function, created during array creation.
         */
        TransformedImage2D interp;
        
        protected UInt8TransformedArray2D(int[] dims, ScalarArray2D<?> refArray, AffineTransform2D transfo)
        {
            super(dims[0], dims[1]);
            this.refArray = refArray;
            
            // create the interpolation class encapsulating both the array and
            // the transform
            this.interp = new TransformedImage2D(refArray, transfo);
        }
        
        @Override
        public byte getByte(int x, int y)
        {
            return (byte) UInt8.convert(interp.evaluate(x, y));
        }

        @Override
        public int getInt(int x, int y)
        {
            return UInt8.convert(interp.evaluate(x, y));
        }

        @Override
        public double getValue(int x, int y)
        {
            return interp.evaluate(x, y);
        }

        @Override
        public void setByte(int x, int y, byte b)
        {
            throw new RuntimeException("Can not modify a transformed array view");
        }

        @Override
        public Collection<Array<?>> parentArrays()
        {
            return List.of(this.refArray);
        }
    }
}
