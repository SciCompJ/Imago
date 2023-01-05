/**
 * 
 */
package imago.plugin.plugin.crop;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.stream.JsonReader;

import imago.app.ImageHandle;
import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.scene.Style;
import imago.app.scene.io.JsonSceneReader;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.array.scalar.UInt8Array2D;
import net.sci.array.scalar.UInt8Array3D;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.image.Image;
import net.sci.image.io.MetaImageInfo;
import net.sci.image.io.MetaImageWriter;

/**
 * Performs 3D Crop on a 3D stack. This class contains the processing methods,
 * and interacts with the contents of the ImageHandle.
 * 
 * Several Processing steps:
 * <ol>
 * <li>Initialize node tree for the plugin. Create a node "crop3d" and two
 * subnodes: "polygons", and "interpolate".</li>
 * <li>Populate the 'polygons" node, either manually or by loading data from a
 * JSON file.</li>
 * <li>Interpolate polygons to populate also slices within annotated
 * slices.</li>
 * <li>Compute cropped image of each slice, and save the result in a 3D image
 * using MetaImage file format.</li>
 * </ol>
 * 
 * @see Crop3DPlugin
 * @see Surface3D
 * 
 * @author dlegland
 *
 */
public class Crop3D extends AlgoStub implements AlgoListener
{
    // ===================================================================
    // Static factories
    
    /**
     * Reads the contents of a Crop3D analysis file.
     * 
     * @param file
     *            the input file, in JSON format, usually ending with ".crop3d".
     * @param parentFrame
     *            the parent frame, used to locate the new frame.
     * @return a new Crop3D object
     * @throws IOException
     *             if a problem occurred.
     */
    public static final Crop3D loadAnalysis(File file, ImagoFrame parentFrame) throws IOException
    {
        // creates the reader from the file in JSON format
        Crop3DDataReader reader = new Crop3DDataReader(file);
        
        // load data and keep relevant ones
        Crop3DData data = reader.readCrop3DData();
        ImageInfo imageInfo = data.imageInfo;
        
        // Check necessary information have been loaded
        if (imageInfo.filePath == null)
        {
            throw new RuntimeException("Could not load image file information.");
        }
        
        // create a new plugin
        Crop3DPlugin plugin = new Crop3DPlugin();
        plugin.run(parentFrame, null);
        plugin.initialize(data);
        
        Crop3D crop3d = plugin.crop3d;
        if (crop3d == null)
        {
            throw new RuntimeException("Expect inner crop3d field to be initialized");
        }
        
        // need to call this to update items to display 
        ImageViewer viewer = plugin.imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
        
        return crop3d;
    }


    // ===================================================================
    // Class members
    
    /**
     * The data for performing crop: image info, polygons...
     */
    Crop3DData data;
    
    /**
     * The image handle, that can be retrieved from the image frame.
     */
    ImageHandle imageHandle;
    
    Crop3DRegion currentRegion = null;
    
    String sliceIndexPattern = "%02d";
    
    Style polygonStyle = new Style();
    Style interpStyle = new Style();
    
    
    // ===================================================================
    // Constructor

    public Crop3D(ImageHandle imageHandle)
    {
        this(new Crop3DData(imageHandle.getImage()), imageHandle);
    }
    
    public Crop3D(Crop3DData data, ImageHandle imageHandle)
    {
        this.data = data;
        this.imageHandle = imageHandle;
        
        // various initializations
        initializeDrawStyles();
        initializeSliceIndexPattern();
        initializeCrop3dNodes();
    }

    /**
     * Initialize drawing styles for polygons.
     */
    private void initializeDrawStyles()
    {
        polygonStyle.setLineWidth(3.5);
        interpStyle.setColor(Color.MAGENTA);
        interpStyle.setLineWidth(1.0);
    }
    
    private void initializeSliceIndexPattern()
    {
        // retrieve number of slices from crop data
        int nSlices = this.data.imageInfo.size[2];
        
        // create slice name patterns based on image size
        int nDigits = (int) Math.ceil(Math.log10(nSlices));
        this.sliceIndexPattern = "%0" + nDigits + "d";
    }
    
    
    // ===================================================================
    // Management of regions
    
    /**
     * Adds a new region with the specified name.
     * 
     * @param regionName the name of the region.
     */
    public void addRegion(String regionName)
    {
        // create the region
        Crop3DRegion region = new Crop3DRegion(regionName);
        
        // add to data
        this.data.addRegion(region);
        
        // update current region
        if (this.currentRegion == null)
        {
            this.currentRegion = region;
        }
    }

    public void removeRegion(String regionName)
    {
        this.data.removeRegion(regionName);
        
        // check if the current region was removed
        if (this.currentRegion != null)
        {
            if (this.currentRegion.name.equals(regionName))
            {
                initializeCrop3dNodes();
                this.currentRegion = null;
            }
        }
    }

    /**
     * Changes the current region, and resets the scene tree associated to Crop3D.
     * 
     * @param regionName the name of the region to select.
     */
    public void selectCurrentRegion(String regionName)
    {
        // retrieve region from name
        Crop3DRegion region = data.getRegion(regionName);
        this.currentRegion = region;
        
        // reset crop3D data
        initializeCrop3dNodes();
        
        // setup new data
        populatePolygons(region);
    }
    
    public void initializeDefaultRegions()
    {
        addRegion("Tube Cells");
        addRegion("Cross Cells");
        selectCurrentRegion("Tube Cells");
    }
    

    // ===================================================================
    // Initialization of nodes
    
    /**
     * Reset the nodes of ImageHandle associated to a Crop3D plugin. 
     */
    public void initializeCrop3dNodes()
    {
        // get root node
        GroupNode rootNode = ((GroupNode) this.imageHandle.getRootNode());
        
        // remove old crop node if it exists
        if (rootNode.hasChildWithName("crop3d"))
        {
            rootNode.removeNode(rootNode.getChild("crop3d"));
        }
        
        // create new crop node
        GroupNode cropNode = new GroupNode("crop3d");
        rootNode.addNode(cropNode);
    
        // add child nodes
        cropNode.addNode(new ImageSerialSectionsNode("polygons"));
        cropNode.addNode(new ImageSerialSectionsNode("interp"));
    }


    // ===================================================================
    // Management of nodes
    
    /**
     * @return true if the image handle contains the necessary scene nodes for
     *         performing Crop3D.
     */
    public boolean hasCrop3dNodes()
    {
        // get root node
        GroupNode rootNode = ((GroupNode) this.imageHandle.getRootNode());
        
        // remove old crop node if it exists
        if (!rootNode.hasChildWithName("crop3d"))
        {
            return false;
        }
        
        // create new crop node
        GroupNode cropNode = (GroupNode) rootNode.getChild("crop3d");
    
        // add child nodes
        if (!(cropNode.hasChildWithName("polygons"))) return false;
        if (!(cropNode.hasChildWithName("interp"))) return false;
        
        // if all conditions are checked, return true
        return true;
    }

    /**
     * @return the group node that contains the polygons node, the smooth node,
     *         and the interp node.
     */
    public GroupNode getCrop3dNode()
    {
        // get root node
        GroupNode rootNode = ((GroupNode) this.imageHandle.getRootNode());
    
        if (rootNode.hasChildWithName("crop3d"))
        {
            return (GroupNode) rootNode.getChild("crop3d");
        }
    
        GroupNode cropNode = new GroupNode("crop3d");
        rootNode.addNode(cropNode);
        return cropNode;
    }


    // ===================================================================
    // Management of polygon nodes
    
    public ImageSerialSectionsNode getPolygonsNode()
    {
        GroupNode cropNode = getCrop3dNode();
        
        if (cropNode.hasChildWithName("polygons"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("polygons");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("polygons");
        cropNode.addNode(polyNode);
        return polyNode;
    }

    /**
     * Adds a new polygon to the current Crop region.
     * 
     * @param sliceIndex
     *            the index of the current XY slice
     * @param poly
     *            the polygon used to crop
     */
    public void addPolygon(int sliceIndex, Polygon2D poly)
    {
        System.out.println("crop3d - add polygon");
        
        // compute slice and polygon name 
        String name = createSliceName("slice", sliceIndex);
        
        // update current region
        LinearRing2D ring = poly.boundary();
        currentRegion.polygons.put(sliceIndex, ring);
        
        // update scene node in current image handle
        setPolygonSliceNode(getPolygonsNode(), sliceIndex, poly, name);
    }
    
    private void setPolygonSliceNode(ImageSerialSectionsNode polyNode, int index, Polygon2D poly, String name)
    {
        // create the shape node
        ShapeNode shapeNode = createPolygonShapeNode(poly, name);

        // get relevant slice node, or create one if necessary
        ImageSliceNode sliceNode;
        if (polyNode.hasSliceNode(index))
        {
             sliceNode = (ImageSliceNode) polyNode.getSliceNode(index);
        }
        else
        {
            sliceNode = new ImageSliceNode(name, index);
            polyNode.addSliceNode(sliceNode);
        }
        
        sliceNode.clear();
        sliceNode.addNode(shapeNode);
    }
    
    private ShapeNode createPolygonShapeNode(Polygon2D poly, String name)
    {
        // Create a new LinearRing shape from the boundary of the polygon
        LinearRing2D ring = poly.rings().iterator().next();
        ShapeNode shapeNode = new ShapeNode(name, ring);
        shapeNode.getStyle().setLineWidth(3.5);
        return shapeNode;
    }
    
    public void removePolygon(int sliceIndex)
    {
        System.out.println("crop3d - remove polygon");
        
        // update current region
        currentRegion.polygons.remove(sliceIndex);
        
        // update scene node in current image handle
        getPolygonsNode().removeSliceNode(sliceIndex);
    }

    public void readPolygonsFromJson(File file) throws IOException
    {
        // check current region exists
        if (this.currentRegion == null)
        {
            throw new RuntimeException("Current region is not defined");
        }
        
        // read polygons of current region
        currentRegion.readPolygonsFromJson(file);

        // reset current state of the Crop3D plugin
        initializeCrop3dNodes();
        populatePolygons(currentRegion);
        
        System.out.println("reading polygons terminated.");
    }
       
    public void readPolygonsFromImageSerialSectionsNode(File file) throws IOException
    {
        // check current region exists
        if (this.currentRegion == null)
        {
            throw new RuntimeException("Current region is not defined");
        }
        
        // create reader
        FileReader fileReader = new FileReader(file);
        JsonSceneReader reader = new JsonSceneReader(new JsonReader(new BufferedReader(fileReader)));
        
        Node node = reader.readNode();
        if (!(node instanceof ImageSerialSectionsNode))
        {
            throw new RuntimeException("Expect a JSON file containing a ImageSerialSectionsNode");
        }
         
        ImageSerialSectionsNode node2 = (ImageSerialSectionsNode) node;
        
        Map<Integer, LinearRing2D> polygons = new TreeMap<Integer, LinearRing2D>();
        for (int sliceIndex : node2.getSliceIndices())
        {
            ImageSliceNode sliceNode = node2.getSliceNode(sliceIndex);
            ShapeNode shapeNode = (ShapeNode) sliceNode.children().iterator().next();
            
            if (!(shapeNode.getGeometry() instanceof LinearRing2D))
            {
                throw new RuntimeException("Geoemtry is expected to be LinearRing2D");
            }
            LinearRing2D polygon = (LinearRing2D) shapeNode.getGeometry();
            
            polygons.put(sliceIndex, polygon);
        }
        
        currentRegion.polygons = polygons;
        currentRegion.interpolatedPolygons.clear();
        
        // reset current state of the Crop3D plugin
        initializeCrop3dNodes();
        populatePolygons(currentRegion);
        
        System.out.println("reading polygons terminated.");
    }
       
    /**
     * Populates the "polygons" and "interp" nodes of the current image handle
     * from the specified region.
     * 
     * @param region
     *            the region containing the polygons to display on the current
     *            Image Viewer
     */
    public void populatePolygons(Crop3DRegion region)
    {
        // Process polygons
        ImageSerialSectionsNode polyNode = getPolygonsNode();
        populateSliceNodes(polyNode, region.polygons, polygonStyle);
        
        // Process interpolated polygons
        ImageSerialSectionsNode interpNode = getInterpolatedPolygonsNode();
        populateSliceNodes(interpNode, region.interpolatedPolygons, interpStyle);
    }
    
    private void populateSliceNodes(ImageSerialSectionsNode node, Map<Integer, LinearRing2D> polygons, Style style)
    {
        node.clear();
        for(int sliceIndex : polygons.keySet())
        {
            LinearRing2D ring = polygons.get(sliceIndex);
            String name = createSliceName("slice", sliceIndex);
            ShapeNode shapeNode = new ShapeNode(name, ring, new Style(style));
            
            ImageSliceNode sliceNode = new ImageSliceNode(name, sliceIndex);
            sliceNode.addNode(shapeNode);
            node.addSliceNode(sliceNode);
        }
   
    }

    // ===================================================================
    // Computation of interpolated polygons

    /**
     * Computes interpolated polygons. Retrieves the "crop3d/polygons" node, and
     * interpolated polygons for slices in between slices containing polygons.
     * 
     * @see Crop3DRegionInterpolator
     */
    public void interpolatePolygons()
    {
        System.out.println("crop3d - interpolate polygons (Fuchs)");

        Crop3DRegionInterpolator algo = new Crop3DRegionInterpolator();
        algo.addAlgoListener(this);
        
        algo.interpolatePolygons(currentRegion);
        
        populatePolygons(currentRegion);
    }
    
    /**
     * Computes the name associated to a slice, based on a user-defined prefix
     * and a slice index.
     * 
     * @param prefix
     *            the prefix used to describe the slice.
     * @param sliceIndex
     *            the index of the slice
     * @return the concatenation of the prefix with a 0-padded string
     *         representation of the slice index
     */
    private String createSliceName(String prefix, int sliceIndex)
    {
        return String.format(Locale.US, prefix + sliceIndexPattern, sliceIndex);
    }
    
    public ImageSerialSectionsNode getInterpolatedPolygonsNode()
    {
        GroupNode cropNode = getCrop3dNode();
        
        if (cropNode.hasChildWithName("interp"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("interp");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("interp");
        cropNode.addNode(polyNode);
        return polyNode;
    }
    

    // ===================================================================
    // Computation of 3D image crop
    
    /**
     * Creates new cropped image using virtual crop array. The current region
     * must have interpolated polygons initialized.
     * 
     * @return a view on the cropped image.
     */
    public Image createCropImageView()
    {
        // Create new cropped image using virtual crop array
        Image image = imageHandle.getImage();
        UInt8Array3D array = (UInt8Array3D) image.getData();
        UInt8Array3D cropArray = new CroppedUInt8Array3D(array, currentRegion.interpolatedPolygons);
        Image cropImage = new Image(cropArray, image);
        cropImage.setName(image.getName() + "-crop");
        return cropImage;
    }

    /**
     * Computes the result of crop for each slice of the 3D image, and saves the
     * result into a file in MHD file format.
     * 
     * @param file
     *            the file to write results in.
     * @throws IOException
     *             if a problem occurred.
     */
    public void saveCropImage(File file) throws IOException
    {
        // get current image data
        Image image = imageHandle.getImage();
        if (!(image.getData() instanceof UInt8Array))
        {
            throw new RuntimeException("Requires an image containing UInt8 data array");
        }
        UInt8Array array = (UInt8Array) image.getData();
        if (array.dimensionality() != 3)
        {
            throw new RuntimeException("Requires an image containing 3D Array");
        }
        UInt8Array3D array3d = UInt8Array3D.wrap(array);
        
        Map<Integer, LinearRing2D> polygons = currentRegion.interpolatedPolygons;
        
        // open file for writing
        MetaImageWriter mhdWriter = new MetaImageWriter(file);
        MetaImageInfo info = mhdWriter.computeMetaImageInfo(image);
        info.elementDataFile = computeElementDataFileName(file.getName());
        
        // print header into header file
        FileOutputStream stream = new FileOutputStream(file);
        writeHeader(info, stream);
        stream.close();
        
        // open a new stream for binary data
        File binaryDataFile = new File(file.getParent(), info.elementDataFile);
        System.out.println("binary file: " + binaryDataFile);
        stream = new FileOutputStream(binaryDataFile);
        BufferedOutputStream bos = new BufferedOutputStream(stream);
        
        // size of array
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        System.out.println("size X: " + sizeX + ", size Y: " + sizeY);
        
        // allocate memory for one slice
        UInt8Array2D resSlice = UInt8Array2D.create(sizeX, sizeY); 
        
        this.fireStatusChanged(this, "crop 3D image (" + polygons.size() + " slices)");
        for (int sliceIndex = 0; sliceIndex < array.size(2); sliceIndex++)
        {
            System.out.println("crop slice: " + sliceIndex);
            
            this.fireProgressChanged(this, sliceIndex, array.size(2));
    
            // get 2D view on array slices
            UInt8Array2D slice = array3d.slice(sliceIndex);
            
            // clear result slice
            resSlice.fillInt(0);
            
            // get crop polygon
            LinearRing2D ring = polygons.get(sliceIndex);
            
            if (ring != null)
            {
                // compute bounds in Y direction
                Bounds2D box = ring.bounds();
                int ymin = (int) Math.max(0, Math.ceil(box.getYMin()));
                int ymax = (int) Math.min(sizeY, Math.floor(box.getYMax()));

                // iterate over lines inside bounding box
                for (int y = ymin; y < ymax; y++)
                {
                    ArrayList<Double> xCrosses = LinearRing2D.xIntersectionsWithHorizontalLine(ring, y);
                    Collections.sort(xCrosses);

                    if (xCrosses.size() % 2 != 0)
                    {
                        System.err.println("can not manage odd number of intersections bewteen linear ring and straight line");
                        continue;
                    }

                    Iterator<Double> iter = xCrosses.iterator();
                    while (iter.hasNext())
                    {
                        int x0 = (int) Math.max(0, Math.ceil(iter.next()));
                        int x1 = (int) Math.min(sizeX, Math.floor(iter.next() + 1));
                        for (int x = x0; x < x1; x++)
                        {
                            resSlice.setInt(x, y, slice.getInt(x, y));
                        }
                    }
                }
            }
            
            // write current slice data on stream
            writeImageData(resSlice, bos);
            bos.flush();
        }
        
        stream.close();
    
        System.out.println("end of crop 3D");
        
        this.fireStatusChanged(this, "");
        this.fireProgressChanged(this, 0, 0);
    }
    
    private String computeElementDataFileName(String fileName)
    {
        int baseLength = fileName.length();
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".mhd") || lowerFileName.endsWith(".mda"))
        {
            fileName = fileName.substring(0, baseLength - 4);
        }
        return fileName + ".raw";
    }
    
    private void writeHeader(MetaImageInfo info, OutputStream stream) throws IOException
    {
        PrintStream ps = new PrintStream(stream);
        
        ps.printf(Locale.US, "%s = %s\n", "ObjectType", "Image");
        ps.printf(Locale.US, "%s = %d\n", "NDims", info.nDims);
        String dimString = Integer.toString(info.dimSize[0]);
        for (int d = 1; d < info.dimSize.length; d++)
        {
            dimString = dimString + " " + info.dimSize[d];
        }
        ps.printf(Locale.US, "%s = %s\n", "DimSize", dimString);
        ps.printf(Locale.US, "%s = %s\n", "ElementType", info.elementType.getMetString());
    
        ps.printf(Locale.US, "%s = %s\n", "ElementDataFile", info.elementDataFile);
    }
    
    /**
     * Writes the content of the array into the specified stream.
     * 
     * @param array
     *            the array to write
     * @param bos
     *            the stream to write in
     * @throws IOException
     *             if a problem occur
     */
    private void writeImageData(Array<?> array, BufferedOutputStream bos) throws IOException
    {
        if (array instanceof UInt8Array)
        {
            UInt8Array array8 = (UInt8Array) array;
            int sizeX = array.size(0);
            int sizeY = array.size(1);
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    bos.write(array8.getByte(x, y));
                }
            }
        }
        else
        {
            throw new RuntimeException("Can not manage arays with class: " + array.getClass()); 
        }
    }

    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
        this.fireProgressChanged(evt);
    }

    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
        this.fireStatusChanged(evt);
    }
}
