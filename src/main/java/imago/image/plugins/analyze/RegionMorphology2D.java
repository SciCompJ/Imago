/**
 * 
 */
package imago.image.plugins.analyze;

import java.util.ArrayList;
import java.util.Collection;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.table.TableFrame;
import net.sci.image.Image;
import net.sci.image.regionfeatures.Feature;
import net.sci.image.regionfeatures.RegionFeatures;
import net.sci.image.regionfeatures.morpho2d.Area;
import net.sci.image.regionfeatures.morpho2d.AverageThickness;
import net.sci.image.regionfeatures.morpho2d.Bounds;
import net.sci.image.regionfeatures.morpho2d.Centroid;
import net.sci.image.regionfeatures.morpho2d.Circularity;
import net.sci.image.regionfeatures.morpho2d.Convexity;
import net.sci.image.regionfeatures.morpho2d.EllipseElongation;
import net.sci.image.regionfeatures.morpho2d.EquivalentEllipse;
import net.sci.image.regionfeatures.morpho2d.EulerNumber;
import net.sci.image.regionfeatures.morpho2d.GeodesicDiameter;
import net.sci.image.regionfeatures.morpho2d.GeodesicElongation;
import net.sci.image.regionfeatures.morpho2d.LargestInscribedDisk;
import net.sci.image.regionfeatures.morpho2d.MaxFeretDiameter;
import net.sci.image.regionfeatures.morpho2d.OrientedBoundingBox;
import net.sci.image.regionfeatures.morpho2d.OrientedBoxElongation;
import net.sci.image.regionfeatures.morpho2d.Perimeter;
import net.sci.image.regionfeatures.morpho2d.Tortuosity;
import net.sci.table.Table;


/**
 * The interactive plugin for computing morphological features from 2D regions
 * represented as label maps.
 * 
 * @see RegionFeatures
 */
public class RegionMorphology2D implements FramePlugin
{
    Options initialOptions = null;
    
    /**
     * Default empty constructor.
     */
    public RegionMorphology2D()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
        {
            return;
        }
        
        // retrieve image data
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
        Image image = handle.getImage();
        if (!image.isLabelImage())
        {
            throw new IllegalArgumentException("Requires label image as input");
        }
        
        // initialize options if necessary
        if (initialOptions == null)
        {
            initialOptions = new Options();
            initialOptions.features.add(Area.class);
            initialOptions.features.add(Perimeter.class);
            initialOptions.features.add(EquivalentEllipse.class);
            initialOptions.features.add(EllipseElongation.class);
        }
        
        // Choose analysis options from interactive dialog
        Options options = chooseOptions(frame, image, initialOptions);
        
        // If cancel was clicked, features is null
        if (options == null) return;
        
        // keep choices for next plugin call
        initialOptions = options;
        
        // create a Region feature analyzer from options
        RegionFeatures analyzer = options.createAnalyzer(image);
        analyzer.addAlgoListener(frame);
        Table featuresTable = analyzer.createTable();
        
        // show result
        featuresTable.setName(image.getName() + "-Morphometry");
        TableFrame.create(featuresTable, frame);
    }
    
    private static final Options chooseOptions(ImagoFrame frame, Image labelMap, Options initialChoice)
    {
        GenericDialog gd = new GenericDialog(frame, "Region Morphology");
        
        // a collection of check boxes to choose features
        Collection<Class<? extends Feature>> features = initialChoice.features;
        String[] featureNames = new String[] {
                "Area", "Perimeter", 
                "Circularity", "Euler_Number",
                "Bounding_Box", "Centroid",
                "Equivalent_Ellipse", "Ellipse_Elongation",
                "Convexity", "Max_Feret_Diameter",
                "Oriented_Box", "Oriented_Box_Elongation",
                "Geodesic_Diameter", "Tortuosity",
                "Max_Inscribed_Disk", "Average_Thickness",
                "Geodesic_Elongation",
        };
        boolean[] states = new boolean[] {
                features.contains(Area.class), features.contains(Perimeter.class),
                features.contains(Circularity.class), features.contains(EulerNumber.class),
                features.contains(Bounds.class), features.contains(Centroid.class),
                features.contains(EquivalentEllipse.class), features.contains(EllipseElongation.class),
                features.contains(Convexity.class), features.contains(MaxFeretDiameter.class),
                features.contains(OrientedBoundingBox.class), features.contains(OrientedBoxElongation.class),
                features.contains(GeodesicDiameter.class), features.contains(Tortuosity.class),
                features.contains(LargestInscribedDisk.class), features.contains(AverageThickness.class),
                features.contains(GeodesicElongation.class),
        };
        gd.addCheckboxGroup(featureNames.length / 2 + 1, 2, featureNames, states, new String[] {"Features:", ""});
        
        gd.addMessage("");
        gd.addCheckBox("Include_Image_Name", initialChoice.includeImageName);
        
        // Display dialog and wait for user validation
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return null;
        }
        
        // Extract features to quantify from image
        Options options = new Options();
        features = options.features;
        if (gd.getNextBoolean()) features.add(Area.class);
        if (gd.getNextBoolean()) features.add(Perimeter.class);
        if (gd.getNextBoolean()) features.add(Circularity.class);
        if (gd.getNextBoolean()) features.add(EulerNumber.class);
        if (gd.getNextBoolean()) features.add(Bounds.class);
        if (gd.getNextBoolean()) features.add(Centroid.class);
        if (gd.getNextBoolean()) features.add(EquivalentEllipse.class);
        if (gd.getNextBoolean()) features.add(EllipseElongation.class);
        if (gd.getNextBoolean()) features.add(Convexity.class);
        if (gd.getNextBoolean()) features.add(MaxFeretDiameter.class);
        if (gd.getNextBoolean()) features.add(OrientedBoundingBox.class);
        if (gd.getNextBoolean()) features.add(OrientedBoxElongation.class);
        if (gd.getNextBoolean()) features.add(GeodesicDiameter.class);
        if (gd.getNextBoolean()) features.add(Tortuosity.class);
        if (gd.getNextBoolean()) features.add(LargestInscribedDisk.class);
        if (gd.getNextBoolean()) features.add(AverageThickness.class);
        if (gd.getNextBoolean()) features.add(GeodesicElongation.class);
        
        options.includeImageName = gd.getNextBoolean();

        return options;
    }
    
    static class Options
    {
        /**
         * The list of features to compute.
         */
        ArrayList<Class<? extends Feature>> features = new ArrayList<>();
        
        /**
         * Display calibration unit within table column names, when appropriate.
         */
        boolean displayUnits = false;
        
        /**
         * Can be useful when concatenating results obtained on different images
         * into a single table.
         */
        boolean includeImageName = false;
        
        /**
         * Creates a new Region Feature Analyzer for the specified image.
         * 
         * @param image
         *            the image containing the label map.
         * @return a new RegionFeatures instance.
         */
        public RegionFeatures createAnalyzer(Image image)
        {
            RegionFeatures analyzer = RegionFeatures.initialize(image);
            features.stream().forEachOrdered(feature -> analyzer.add(feature));
            return analyzer;
        }
    }
}
