/**
 * 
 */
package imago.plugin.image.shape;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
import imago.gui.image.ImageDisplay;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.image.PlanarImageViewer;
import imago.gui.image.StackSliceViewer;
import imago.gui.util.GuiHelper;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.Array3D;
import net.sci.array.color.RGB8Array;
import net.sci.array.color.RGB8Array2D;
import net.sci.array.color.RGB8Array3D;
import net.sci.array.interp.ScalarFunction3D;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.array.scalar.UInt8Array2D;
import net.sci.array.scalar.UInt8Array3D;
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
 
    public static final Array2D<?> rotatedCrop(Array2D<?> array, int[] dims, Point2D refPoint, double angleInDegrees)
    {
        AffineTransform2D transfo = computeTransform(refPoint, dims, angleInDegrees);

        if (array instanceof ScalarArray2D<?>)
        {
            return rotatedCropUInt8((ScalarArray2D<?>) array, dims, transfo);
        }
        else if (array instanceof RGB8Array2D)
        {
            // allocate result image
            RGB8Array2D rgbArray = (RGB8Array2D) array;
            UInt8Array2D red   = rotatedCropUInt8(rgbArray.channel(0), dims, transfo);
            UInt8Array2D green = rotatedCropUInt8(rgbArray.channel(1), dims, transfo);
            UInt8Array2D blue  = rotatedCropUInt8(rgbArray.channel(2), dims, transfo);
            RGB8Array2D res = RGB8Array2D.wrap(RGB8Array.mergeChannels(red, green, blue));
            return res;
        }
        else
        {
            throw new IllegalArgumentException("Requires a scalar array as input");
        }
    }
    
    private static final AffineTransform2D computeTransform(Point2D refPoint, int[] dims, double angleInDegrees)
    {
        // retrieve image dimensions
        int sizeX = dims[0];
        int sizeY = dims[1];

        // create elementary transforms
        AffineTransform2D trBoxCenter = AffineTransform2D.createTranslation(-sizeX / 2, -sizeY / 2);
        AffineTransform2D rot = AffineTransform2D.createRotation(Math.toRadians(angleInDegrees));
        AffineTransform2D trRefPoint = AffineTransform2D.createTranslation(refPoint);

        // concatenate into global display-image-to-source-image transform
        return trRefPoint.compose(rot).compose(trBoxCenter);
    }
    
    private static final UInt8Array2D rotatedCropUInt8(ScalarArray2D<?> array, int[] dims, AffineTransform2D transfo)
    {
        // Create interpolation class, that encapsulates both the image and the transform
        TransformedImage2D interp = new TransformedImage2D(array, transfo);

        // allocate result image
        UInt8Array2D res = UInt8Array2D.create(dims[0], dims[1]);
        res.fillValues((x, y) -> interp.evaluate(x, y));

        return res;
    }
    
    
    public static final Array3D<?> rotatedCrop(Array3D<?> array, int[] dims, Point3D refPoint, double[] anglesInDegrees)
    {
        // Computes the transform that will map indices from within result image
        // into coordinates within source image
        AffineTransform3D transfo = computeTransform(refPoint, dims, anglesInDegrees);
        
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
    
    private static final AffineTransform3D computeTransform(Point3D boxCenter, int[] boxSize, double[] anglesInDegrees)
    {
        // create a translation to put center of the box at the origin
        int sizeX = boxSize[0];
        int sizeY = boxSize[1];
        int sizeZ = boxSize[2];
        AffineTransform3D trBoxCenter = AffineTransform3D.createTranslation(-sizeX / 2, -sizeY / 2, -sizeZ / 2);
        
        // then, apply 3D rotation by Euler angles followed by translation to
        // put origin (= box center) on the reference point
        AffineTransform3D transfo = rotateAndShift(anglesInDegrees, boxCenter).concatenate(trBoxCenter);
        
        return transfo;
    }

    /**
     * Computes the box-to-world transform, that will transform coordinates from
     * the box basis into the world (global) basis. The origin in the box basis
     * will be mapped into the box center in the global basis.
     * 
     * @param anglesInDegrees
     *            the three Euler angles (in degrees) that define the box
     *            orientation ("XYZ" convention)
     * @param boxCenter
     *            the center of the box
     * @return an affine transform that can be used to compute coordinates of
     *         box corners in global basis
     */
    private static final AffineTransform3D rotateAndShift(double[] anglesInDegrees, Point3D refPoint)
    {
        AffineTransform3D rotX = AffineTransform3D.createRotationOx(Math.toRadians(anglesInDegrees[0]));
        AffineTransform3D rotY = AffineTransform3D.createRotationOy(Math.toRadians(anglesInDegrees[1]));
        AffineTransform3D rotZ = AffineTransform3D.createRotationOz(Math.toRadians(anglesInDegrees[2]));
        AffineTransform3D trans = AffineTransform3D.createTranslation(refPoint);
        
        // concatenate into global display-image-to-source-image transform
        return trans.concatenate(rotZ).concatenate(rotY).concatenate(rotX);
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
            
            // TODO: should try to avoid class cast
            ImageDisplay imageDisplay = ((PlanarImageViewer) imageFrame.getImageViewer()).getImageDisplay();
            imageDisplay.addMouseListener(settingsFrame);
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

            
            // TODO: should try to avoid class cast
            ImageViewer viewer = imageFrame.getImageViewer();
            if (viewer instanceof StackSliceViewer)
            {
                ImageDisplay imageDisplay = ((StackSliceViewer) viewer).getImageDisplay();
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
        
        int boxSizeX;
        int boxSizeY;
        double boxCenterX;
        double boxCenterY;
        double boxAngle;
        // TODO: create a "Box"/"OrientedBox" inner class?
        
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
            boxSizeX = boxSize[0];
            boxSizeY = boxSize[1];
            boxCenterX = refPoint.x();
            boxCenterY = refPoint.y();
            boxAngle = rotAngle;

            setupWidgets();
            setupLayout();

            this.pack();
            this.centerFrame();
            
            setVisible(true);
        }
        
        private void setupWidgets()
        {
            sizeXWidget = GuiHelper.createNumberSpinner(boxSizeX, 0, 10000, 1);
            sizeXWidget.addChangeListener(evt -> 
            {
                this.boxSizeX = ((SpinnerNumberModel) sizeXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            sizeYWidget = GuiHelper.createNumberSpinner(boxSizeY, 0, 10000, 1);
            sizeYWidget.addChangeListener(evt -> 
            {
                this.boxSizeY = ((SpinnerNumberModel) sizeYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterXWidget = GuiHelper.createNumberSpinner(boxCenterX, 0, 10000, 1);
            boxCenterXWidget.addChangeListener(evt -> 
            {
                this.boxCenterX = ((SpinnerNumberModel) boxCenterXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterYWidget = GuiHelper.createNumberSpinner(boxCenterY, 0, 10000, 1);
            boxCenterYWidget.addChangeListener(evt -> 
            {
                this.boxCenterY = ((SpinnerNumberModel) boxCenterYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            
            boxAngleWidget = GuiHelper.createNumberSpinner(boxAngle, -180, 180, 1);
            boxAngleWidget.addChangeListener(evt -> 
            {
                this.boxAngle = ((SpinnerNumberModel) boxAngleWidget.getModel()).getNumber().doubleValue();
                updatePreviewIfNeeded();
            });

            autoUpdateCheckBox = new JCheckBox("Auto-Update", false);
            autoUpdateCheckBox.addItemListener(evt -> updatePreviewIfNeeded());
            
            previewButton = new JButton("Preview");
            previewButton.addActionListener(evt -> updatePreview());

            runButton = new JButton("Create Result!");
            runButton.addActionListener(evt -> displayResult());
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
            int[] dims = new int[] {boxSizeX, boxSizeY};
            Point2D cropCenter = new Point2D(boxCenterX, boxCenterY);
            
            Array2D<?> res = rotatedCrop(this.array, dims, cropCenter, boxAngle);
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

        public void displayResult()
        {
            int[] dims = new int[] {boxSizeX, boxSizeY};
            Point2D cropCenter = new Point2D(boxCenterX, boxCenterY);
            
            Array2D<?> res = rotatedCrop(this.array, dims, cropCenter, boxAngle);
            Image resultImage = new Image(res, image);
            resultImage.setName(image.getName() + "-crop");
            
            ImageFrame.create(resultImage, this.parentFrame);
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
            
            this.boxCenterX = point.x();
            ((SpinnerNumberModel) this.boxCenterXWidget.getModel()).setValue(this.boxCenterX);
            this.boxCenterY = point.y();
            ((SpinnerNumberModel) this.boxCenterYWidget.getModel()).setValue(this.boxCenterY);
            
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
        
        int boxSizeX;
        int boxSizeY;
        int boxSizeZ;
        double boxCenterX;
        double boxCenterY;
        double boxCenterZ;
        double boxRotX;
        double boxRotY;
        double boxRotZ;
        // TODO: create a "Box"/"OrientedBox" inner class?


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
            boxSizeX = boxSize[0];
            boxSizeY = boxSize[1];
            boxSizeZ = boxSize[2];
            boxCenterX = refPoint.x();
            boxCenterY = refPoint.y();
            boxCenterZ = refPoint.z();
            boxRotX = rotAngles[0];
            boxRotY = rotAngles[1];
            boxRotZ = rotAngles[2];

            setupWidgets();
            setupLayout();

            this.pack();
            this.centerFrame();
            
            setVisible(true);
        }
        
        private void setupWidgets()
        {
            sizeXWidget = GuiHelper.createNumberSpinner(boxSizeX, 0, 10000, 1);
            sizeXWidget.addChangeListener(evt -> 
            {
                this.boxSizeX = ((SpinnerNumberModel) sizeXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            sizeYWidget = GuiHelper.createNumberSpinner(boxSizeY, 0, 10000, 1);
            sizeYWidget.addChangeListener(evt -> 
            {
                this.boxSizeY = ((SpinnerNumberModel) sizeYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            sizeZWidget = GuiHelper.createNumberSpinner(boxSizeZ, 0, 10000, 1);
            sizeZWidget.addChangeListener(evt -> 
            {
                this.boxSizeZ = ((SpinnerNumberModel) sizeZWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterXWidget = GuiHelper.createNumberSpinner(boxCenterX, 0, 10000, 1);
            boxCenterXWidget.addChangeListener(evt -> 
            {
                this.boxCenterX = ((SpinnerNumberModel) boxCenterXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterYWidget = GuiHelper.createNumberSpinner(boxCenterY, 0, 10000, 1);
            boxCenterYWidget.addChangeListener(evt -> 
            {
                this.boxCenterY = ((SpinnerNumberModel) boxCenterYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterZWidget = GuiHelper.createNumberSpinner(boxCenterZ, 0, 10000, 1);
            boxCenterZWidget.addChangeListener(evt -> 
            {
                this.boxCenterZ = ((SpinnerNumberModel) boxCenterZWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            
            boxRotXWidget = GuiHelper.createNumberSpinner(boxRotX, -180, 180, 1);
            boxRotXWidget.addChangeListener(evt -> 
            {
                this.boxRotX = ((SpinnerNumberModel) boxRotXWidget.getModel()).getNumber().doubleValue();
                updatePreviewIfNeeded();
            });
            
            boxRotYWidget = GuiHelper.createNumberSpinner(boxRotY, -180, 180, 1);
            boxRotYWidget.addChangeListener(evt -> 
            {
                this.boxRotY = ((SpinnerNumberModel) boxRotYWidget.getModel()).getNumber().doubleValue();
                updatePreviewIfNeeded();
            });
            
            boxRotZWidget = GuiHelper.createNumberSpinner(boxRotZ, -180, 180, 1);
            boxRotZWidget.addChangeListener(evt -> 
            {
                this.boxRotZ = ((SpinnerNumberModel) boxRotZWidget.getModel()).getNumber().doubleValue();
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
            int[] dims = new int[] {boxSizeX, boxSizeY, boxSizeZ};
            Point3D cropCenter = new Point3D(boxCenterX, boxCenterY, boxCenterZ);
            double[] angles = new double[] {boxRotX, boxRotY, boxRotZ};

            // compute the transform
            AffineTransform3D transfo = computeTransform(cropCenter, dims, angles);

            // Create interpolation class, that encapsulates both the image and the
            // transform
            TransformedImage3D interp = new TransformedImage3D(this.array, transfo);

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
            int[] dims = new int[] {boxSizeX, boxSizeY, boxSizeZ};
            Point3D cropCenter = new Point3D(boxCenterX, boxCenterY, boxCenterZ);
            double[] angles = new double[] {boxRotX, boxRotY, boxRotZ};

            System.out.println("Rot Crop With params: ");
            System.out.println(String.format("  box size: %d x %d x %d", boxSizeX, boxSizeY, boxSizeZ));
            System.out.println(String.format("  refPoint: " + cropCenter));
            System.out.println(String.format("  Euler Angles: %5.2f, %5.2f, %5.2f", boxRotX, boxRotY, boxRotZ));
            
            // compute the crop
            Array3D<?> res = rotatedCrop(this.array, dims, cropCenter, angles);
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
            this.boxCenterX = point.x();
            ((SpinnerNumberModel) this.boxCenterXWidget.getModel()).setValue(this.boxCenterX);
            this.boxCenterY = point.y();
            ((SpinnerNumberModel) this.boxCenterYWidget.getModel()).setValue(this.boxCenterY);
            this.boxCenterZ = zSlice;
            ((SpinnerNumberModel) this.boxCenterZWidget.getModel()).setValue(this.boxCenterZ);
            
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
}
