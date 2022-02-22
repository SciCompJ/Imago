/**
 * 
 */
package imago.plugin.plugin.crop;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import imago.app.ImageHandle;
import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.array.scalar.UInt8Array;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.Point2D;
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
        
        initializeSliceIndexPattern();
        initializeCrop3dNodes();
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
        Crop3DRegion region = new Crop3DRegion();
        region.name = regionName;
        
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
//        populatePolygons(region.polygons);
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
        setPolygonSliceNode(currentRegion.polygons, sliceIndex, poly, name);
        
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
        currentRegion.polygons.removeSliceNode(sliceIndex);
        
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
        // Process 
        ImageSerialSectionsNode polyNode = getPolygonsNode();
        polyNode.clear();
        for(ImageSliceNode child : region.polygons.children())
        {
            // check that all children of slice nodes are shape nodes with polyline2D geometry
            for (Node child2 : child.children())
            {
                if (!(child2 instanceof ShapeNode))
                {
                    throw new RuntimeException("Expect all ImageSliceNode to contain shape nodes.");
                }

                if(!(((ShapeNode) child2).getGeometry() instanceof LinearRing2D))
                {
                    throw new RuntimeException("Expect all Shape nodes to contain LinearRing2D geometries.");
                }
            }
            polyNode.addSliceNode(child);
        }
        
        ImageSerialSectionsNode interpNode = getInterpolatedPolygonsNode();
        interpNode.clear();
        for(ImageSliceNode child : region.interpolatedPolygons.children())
        {
            // check that all children of slice nodes are shape nodes with polyline2D geometry
            for (Node child2 : child.children())
            {
                if (!(child2 instanceof ShapeNode))
                {
                    throw new RuntimeException("Expect all ImageSliceNode to contain shape nodes.");
                }

                if(!(((ShapeNode) child2).getGeometry() instanceof LinearRing2D))
                {
                    throw new RuntimeException("Expect all Shape nodes to contain LinearRing2D geometries.");
                }
            }
            interpNode.addSliceNode(child);
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
        ImageSerialSectionsNode interpNode = algo.interpolatePolygons(currentRegion.polygons);
        currentRegion.interpolatedPolygons = interpNode;
        
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
    
    /**
     * Retrieve the LinearRing2D geometry at the specified index from the serial
     * sections node.
     * 
     * ImageSerialSectionsNode -> ImageSliceSection -> ShapeNode -> Geometry.
     * 
     * @param node
     *            the node mapping to ImageSliceSections
     * @param index
     *            the index of the slice
     * @return the LinearRing2D geometry contained in the specified slice.
     */
    private LinearRing2D getLinearRing(ImageSerialSectionsNode cropNode, int sliceIndex)
    {
        // retrieve shape node
        ShapeNode shapeNode = (ShapeNode) cropNode.getSliceNode(sliceIndex).children().iterator().next();
        
        // check class
        if (!(shapeNode.getGeometry() instanceof LinearRing2D))
        {
            throw new RuntimeException("Expect crop geometry to be a LinearRing2D");
        }
        
        // return with correct class cast
        return (LinearRing2D) shapeNode.getGeometry();
    }
    
    public ImageSerialSectionsNode getSmoothPolygonsNode()
    {
        GroupNode cropNode = getCrop3dNode();
        
        if (cropNode.hasChildWithName("smooth"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("smooth");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("smooth");
        cropNode.addNode(polyNode);
        return polyNode;
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
     * Computes the result of crop for each slice of the 3D image, and saves the
     * result into a file in MHD file format.
     * 
     * @param file
     *            the file to write results in.
     * @throws IOException
     *             if a problem occurred.
     */
    public void computeCroppedImage(File file) throws IOException
    {
        // get current image data
        Image image = imageHandle.getImage();
        ScalarArray<?> array = (ScalarArray<?>) image.getData();
        if (array.dimensionality() != 3)
        {
            throw new RuntimeException("Requires an image containing 3D Array");
        }
        ScalarArray3D<?> array3d = ScalarArray3D.wrap(array);
        
        // get input node reference
        ImageSerialSectionsNode interpNode = getInterpolatedPolygonsNode();
        if (interpNode.getSliceIndices().isEmpty())
        {
            throw new RuntimeException("Interpolation node is empty");
        }
        
        // open file for writing
        MetaImageWriter mhdWriter = new MetaImageWriter(file);
        MetaImageInfo info = mhdWriter.computeMetaImageInfo(image);
        info.dimSize[2] = interpNode.getSliceIndices().size();
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
        
        this.fireStatusChanged(this, "crop 3D image (" + interpNode.getSliceIndices().size() + " slices)");
    
        for (int sliceIndex : interpNode.getSliceIndices())
        {
            System.out.println("crop slice: " + sliceIndex);
            
            this.fireProgressChanged(this, sliceIndex, array.size(2));
    
            // get 2D view on array slices
            ScalarArray2D<?> slice = (ScalarArray2D<?>) array3d.slice(sliceIndex);
            
            // create 2D slice for storing crop result
            ScalarArray2D<?> resSlice = ScalarArray2D.wrapScalar2d(array.newInstance(sizeX, sizeY));
            
            // get crop polygon
            LinearRing2D ring = getLinearRing(interpNode, sliceIndex);
            
            // compute bounds in Y direction
            Bounds2D box = ring.bounds();
            int ymin = (int) Math.max(0, Math.ceil(box.getYMin()));
            int ymax = (int) Math.min(sizeY, Math.floor(box.getYMax()));
    
            // iterate over lines inside bounding box
            for (int y = ymin; y < ymax; y++)
            {
                ArrayList<Double> xCrosses = computeXIntersectionsWithHorizontalLine(ring, y);
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
                        resSlice.setValue(x, y, slice.getValue(x, y));
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
    
    /**
     * Computes the list of x-coordinates of intersection points of the input
     * polygon with a horizontal line of a given height. Specific care is taken
     * for intersection points located at the extremity of edges.
     * 
     * For references about the algorithm: <a href=
     * "https://web.cs.ucdavis.edu/~ma/ECS175_S00/Notes/0411_b.pdf">Scanline
     * Fill Algorithm (pdf)</a>.
     * 
     * Implementation based on the following page:
     * <a href= "https://alienryderflex.com/polygon_fill/"> Efficient Polygon
     * Fill Algorithm With C Code Sample</a>, by Darel Rex Finley.
     * 
     * @param ring
     *            the polygon, as a linear ring
     * @param yLine
     *            the y-coordinate of the intersection line
     * @return the (unsorted) list of x-coordinates of the intersection points.
     *         Order corresponds to iteration order along polygon.
     */
    private ArrayList<Double> computeXIntersectionsWithHorizontalLine(LinearRing2D ring, int yLine)
    {
        // number of vertices in polygon
        int nVertices = ring.vertexCount();

        //  create an array for storing x-coordinates of intersections
        ArrayList<Double> xNodes = new ArrayList<>();

        // initialize data for previous vertex
        Point2D prevPos = ring.vertexPosition(nVertices - 1);
        double yvp = prevPos.getY();

        // iterate over indices of first edge vertex
        for (Point2D pos : ring.vertexPositions())
        {
            // y-coordinate of current vertex
            double yv = pos.getY();
            
            // check conditions for intersection
            // either if:
            // 1) previous vertex is above or on, and current vertex is strictly below
            // 2) previous vertex is strictly below, and current vertex is above or on
            if  (yvp >= yLine && yv < yLine || yvp < yLine && yv >= yLine)
            {
                // slope of current edge (dy cannot be zero due to above condition)
                double edgeDx = pos.getX() - prevPos.getX();
                double edgeDy = pos.getY() - prevPos.getY();
                // x-coordinate of intersection
                double xNewNode = pos.getX() + (yLine - yv) * edgeDx / edgeDy ;
                // add to list of intersections
                xNodes.add(xNewNode);
            }
            
            // switch current vertex to previous vertex
            prevPos = pos;
            yvp = yv;
        }
        
        return xNodes;
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
