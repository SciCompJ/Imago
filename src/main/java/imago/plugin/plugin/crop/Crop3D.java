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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JOptionPane;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import imago.app.ImageHandle;
import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.scene.io.JsonSceneReader;
import imago.app.scene.io.JsonSceneWriter;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.viewer.StackSliceViewer;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.array.scalar.UInt8Array;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.StraightLine2D;
import net.sci.geom.geom2d.Vector2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.image.Image;
import net.sci.image.io.MetaImageInfo;
import net.sci.image.io.MetaImageWriter;

/**
 * Performs 3D Crop on a 3D stack. This class contains the processing methods,
 * the main GUI is managed by the Crop3DPlugin class.
 * 
 * Several Processing steps:
 * <ol>
 * <li>Initialize node tree for the plugin. Create a node "crop3d" and three
 * subnodes: "polygons", "smooth", and "interpolate".</li>
 * <li>Populate the 'polygons" node, either manually or by loading data from a
 * JSON file.</li>
 * <li>Smooth each polygons (resample and smooth).</li>
 * <li>Interpolate smoothed polygons to populate also slices within annotated
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
public class Crop3D extends AlgoStub
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
        // Default values to load
        ImageInfo imageInfo = null;
        ImageSerialSectionsNode polygonsNode = null;

        // parse file using JSON
        // create json file reader
        FileReader fileReader = new FileReader(file);
        JsonReader reader = new JsonReader(new BufferedReader(fileReader));

        // start parsing Crop3D object
        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();

            if (name.equalsIgnoreCase("type"))
            {
                // check the file start with the Crop3D data header
                String string = reader.nextString();
                if (!string.equalsIgnoreCase("Crop3D"))
                {
                    throw new RuntimeException("Expect a file containing a Crop3D type data");
                }
            }
            else if(name.equalsIgnoreCase("saveDate"))
            {
                reader.skipValue();
            } 
            else if(name.equalsIgnoreCase("image"))
            {
                imageInfo = readImageInfo(reader);
            } 
            else if(name.equalsIgnoreCase("models"))
            {
                // In the future we may envision several models, but in current version we manage only one.
                reader.beginArray();
                reader.beginObject();
                String name2 = reader.nextName();
                if (!name2.equalsIgnoreCase("crop"))
                {
                    throw new RuntimeException("Expect the file to contain a \"models\"/\"crop\" node");
                }
                reader.beginObject();
                
                String name3 = reader.nextName();
                if(name3.equalsIgnoreCase("polygons"))
                {
                    // create a scene reader to parse polyline
                    JsonSceneReader sceneReader = new JsonSceneReader(reader);
                    Node node = sceneReader.readNode();

                    // expect a group node...
                    if (!(node instanceof ImageSerialSectionsNode))
                    {
                        throw new RuntimeException("JSON file should contains a single ImageSerialSectionsNode instance.");
                    }

                    polygonsNode = (ImageSerialSectionsNode) node;
                } 
                else
                {
                    System.out.println("Unknown field name when reading Crop3D: " + name3);
                    reader.skipValue();
                }
                reader.endObject(); // crop object
                reader.endObject(); // array item
                
                // drop all remaining models
                if (reader.hasNext())
                {
                    System.err.println("Warning: additional models are specified in Crop3D, but will be discarded." + name);
                    while (reader.hasNext())
                    {
                        reader.skipValue();
                    }
                }
                reader.endArray();
            }
        }
        reader.endObject();

        // Check necessary information have been loaded
        if (imageInfo.filePath == null)
        {
            throw new RuntimeException("Could not load image file information.");
        }
        
        // create a new plugin
        Crop3DPlugin newFrame = new Crop3DPlugin();
        newFrame.run(parentFrame, null);
        
        // create an image viewer for the file given in ImageInfo
        newFrame.openImage(new File(imageInfo.filePath));
        
        Crop3D crop3d = newFrame.crop3d;
        if (crop3d == null)
        {
            throw new RuntimeException("Expect inner crop3d field to be initialized");
        }
        
        // update polylines of the new analysis
        crop3d.populatePolygons(polygonsNode);
        newFrame.updatePolygonListView();
        
        // need to call this to update items to display 
        ImageViewer viewer = newFrame.imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
        
        return crop3d;
    }


    private static final ImageInfo readImageInfo(JsonReader reader) throws IOException
    {
        // initialize image info object with default values
        ImageInfo imageInfo = new ImageInfo();
    
        // parse object fields
        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();
            if (name.equalsIgnoreCase("type"))
            {
                // check the file start with the Crop3D data header
                if (!reader.nextString().equalsIgnoreCase("Image3D"))
                {
                    throw new RuntimeException("Expect a file containing a Image3D type data");
                }
            }
            else if(name.equalsIgnoreCase("name"))
            {
                imageInfo.name = reader.nextString();
            }
            else if(name.equalsIgnoreCase("filePath"))
            {
                imageInfo.filePath = reader.nextString();
            }
            else if(name.equalsIgnoreCase("nDims"))
            {
                imageInfo.nDims = reader.nextInt();
            }
            else if(name.equalsIgnoreCase("size"))
            {
                imageInfo.size = readIntArray(reader, imageInfo.nDims);
            }
            else
            {
                System.out.println("Unknown field name when reading Image3D: " + name);
                reader.skipValue();
            }
    
        }
        reader.endObject();
    
        return imageInfo;
    }

    private static final int[] readIntArray(JsonReader reader, int nItems) throws IOException
    {
        int[] res = new int[nItems];
        reader.beginArray();
        for (int d = 0; d < nItems; d++)
        {
            res[d] = reader.nextInt();
        }
        reader.endArray();
        return res;
    }


    // ===================================================================
    // Class members
    
    /**
     * The frame containing the image to crop.
     */
    ImageFrame image3dFrame;

    /**
     * The image handle, that can be retrieved from the image frame.
     */
    ImageHandle imageHandle;
    
    
    String polygonSliceNamePattern = "slice%02d";
    String smoothSliceNamePattern = "smooth%02d";
    String interpolatedSliceNamePattern = "interp%02d";
    
    
    // ===================================================================
    // Constructor

    public Crop3D(ImageFrame image3dFrame)
    {
        this.image3dFrame = image3dFrame;
        this.imageHandle = image3dFrame.getImageHandle();
        
        // get current image data
        ImageViewer viewer = image3dFrame.getImageView();
        if (!(viewer instanceof StackSliceViewer))
        {
            System.err.println("requires an instance of stack slice viewer");
            return;
        }
        Array<?> array = this.imageHandle.getImage().getData();

        // create slice name patterns based on image size
        int nSlices = array.size(2);
        int nDigits = (int) Math.ceil(Math.log10(nSlices));
        this.polygonSliceNamePattern = "slice%0" + nDigits + "d";
        this.smoothSliceNamePattern = "smooth%0" + nDigits + "d";
        this.interpolatedSliceNamePattern = "interp%0" + nDigits + "d";
     
        initializeCrop3dNodes();
    }
    

    // ===================================================================
    // Initialization of nodes
    
    /**
     * Reset the nodes associated to a Crop3D plugin. 
     * 
     * @param handle the ImageHandle containing the nodes to reset.
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
        cropNode.addNode(new ImageSerialSectionsNode("smooth"));
        cropNode.addNode(new ImageSerialSectionsNode("interp"));
    }


    // ===================================================================
    // Management of nodes
    
    /**
     * @param handle
     *            the ImageHandle containing the nodes.
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
        if (!(cropNode.hasChildWithName("smooth"))) return false;
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


    public void addPolygon(int sliceIndex, Polygon2D poly)
    {
        System.out.println("crop3d - add polygon");
        
        ImageViewer viewer = image3dFrame.getImageView();
        if (!(viewer instanceof StackSliceViewer))
        {
            System.out.println("requires an instance of stack slice viewer");
            return;
        }
    
        // get current image data
        Image image = this.imageHandle.getImage();
        Array<?> array = image.getData();
        if (array.dimensionality() != 3)
        {
            throw new RuntimeException("Requires an image containing 3D Array");
        }
        int nSlices = array.size(2);
    
        
        // select node containing manually delineated polygons
        ImageSerialSectionsNode polyNode = getPolygonsNode();
        
        // compute slice and polygon name 
        int nDigits = (int) Math.ceil(Math.log10(nSlices));
        String sliceName = String.format(Locale.US, "slice%0" + nDigits + "d", sliceIndex);
        
        // Create a new LinearRing shape from the boundary of the polygon
        ShapeNode shapeNode = new ShapeNode(sliceName, poly.rings().iterator().next());
        shapeNode.getStyle().setLineWidth(3.5);
        
        // get relevant slice node, or create one if necessary
        ImageSliceNode sliceNode;
        if (polyNode.hasSliceNode(sliceIndex))
        {
             sliceNode = (ImageSliceNode) polyNode.getSliceNode(sliceIndex);
        }
        else
        {
            sliceNode = new ImageSliceNode(sliceName, sliceIndex);
            polyNode.addSliceNode(sliceNode);
        }
        
        sliceNode.clear();
        sliceNode.addNode(shapeNode);
    }

    /**
     * Saves the analysis into a file in JSON format.
     * 
     * @param file
     *            the file to writes analysis data.
     * @throws IOException
     *             if a I/O problem occurred.
     */
    public void saveAnalysisAsJson(File file) throws IOException
    {
        // initialize JSON writer
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        JsonWriter jsonWriter = new JsonWriter(new PrintWriter(fileWriter));
        jsonWriter.setIndent("  ");
        
        // open Crop3D node
        jsonWriter.beginObject();
        jsonWriter.name("type").value("Crop3D");
        String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
        jsonWriter.name("saveDate").value(dateString);
        
        // one node for the 3D image
        Image image = this.imageHandle.getImage();
        jsonWriter.name("image");
        jsonWriter.beginObject();
        jsonWriter.name("type").value("Image3D");
        jsonWriter.name("name").value(image.getName());
        jsonWriter.name("filePath").value(image.getFilePath());
        int[] dims = image.getData().size();
        jsonWriter.name("nDims").value(dims.length);
        jsonWriter.name("size").beginArray();
        for (int d : dims)
        {
            jsonWriter.value(d);
        }
        jsonWriter.endArray();
        jsonWriter.endObject();
        
        // one node for the collection of models
        jsonWriter.name("models").beginArray();
        
        // one node for the default crop polygons
        jsonWriter.beginObject();
        jsonWriter.name("crop").beginObject();
        // write crop polygon data
        ImageSerialSectionsNode polyNode = getPolygonsNode();
        if (polyNode != null)
        {
            JsonSceneWriter sceneWriter = new JsonSceneWriter(jsonWriter);
            jsonWriter.name("polygons");
            sceneWriter.writeNode(polyNode);
        }
      
        jsonWriter.endObject(); // crop object
        jsonWriter.endObject(); // array item
        jsonWriter.endArray(); // array of models

        
        // close Crop3D
        jsonWriter.endObject();

        fileWriter.close();
        
        System.out.println("Saving Crop3D terminated.");
    }

    public void readPolygonsFromJson(File file) throws IOException
    {
        // reset current state of the Crop3D plugin
        initializeCrop3dNodes();

        JsonSceneReader sceneReader;

        FileReader fileReader = new FileReader(file);
        JsonReader jsonReader = new JsonReader(new BufferedReader(fileReader));
        sceneReader = new JsonSceneReader(jsonReader);

        try 
        {
            // expect a group node...
            Node node = sceneReader.readNode();
            if (!(node instanceof ImageSerialSectionsNode))
            {
                throw new RuntimeException("JSON file should contains a single ImageSerialSectionsNode instance.");
            }
            
            ImageSerialSectionsNode polyNode = getPolygonsNode();
            for(ImageSliceNode child : ((ImageSerialSectionsNode) node).children())
            {
                polyNode.addSliceNode(child);
            }

        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        System.out.println("reading polygons terminated.");
    }
    
    public void savePolygonsAsJson(File file) throws IOException
    {
        ImageSerialSectionsNode polyNode = getPolygonsNode();
        if (polyNode == null)
        {
            System.err.println("Current image does not contain Crop3D polygon information");
            return;
        }
        
        try 
        {
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            JsonWriter jsonWriter = new JsonWriter(new PrintWriter(fileWriter));
            jsonWriter.setIndent("  ");
            JsonSceneWriter writer = new JsonSceneWriter(jsonWriter);

            writer.writeNode(polyNode);

            fileWriter.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        System.out.println("Saving polygon terminated.");
    }
    
    /**
     * Populates the "polygons" node of current image handle from the specified
     * ImageSerialSectionsNode.
     * 
     * @param polygonsNode
     *            the node containing the map between slice indices and shape
     */
    public void populatePolygons(ImageSerialSectionsNode polygonsNode)
    {
        ImageSerialSectionsNode polyNode = getPolygonsNode();
        for(ImageSliceNode child : polygonsNode.children())
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
    }
    

    // ===================================================================
    // Computation of interpolated polygons
    
    /**
     * Computes interpolated polygons. Retrieves the "crop3d/polygons" node,
     * apply smoothing to each polygon, and interpolated polygons for slices in
     * between slices containing polygons.
     */
    public void smoothAndInterpolatePolygons()
    {
        System.out.println("crop3d - interpolate polygons");

        smoothPolygons();
        interpolatePolygons();
    }
    
    /**
     * Computes the "smooth" node from the "polygons" node.
     */
    private void smoothPolygons()
    {
        ImageSerialSectionsNode polyNode = getPolygonsNode();
        if (polyNode.isLeaf())
        {
            JOptionPane.showMessageDialog(image3dFrame.getWidget(),
                    "Requires the frame to contains valid Crop3D Polygons",
                    "Crop3D Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // clear output nodes
        ImageSerialSectionsNode smoothNode = getSmoothPolygonsNode();
        smoothNode.clear();
        
        // iterate over polygons to create a smoothed version
        for (ImageSliceNode sliceNode : polyNode.children())
        {
            int sliceIndex = sliceNode.getSliceIndex(); 
            System.out.println("smooth polygon on slice " + sliceIndex);
            
            // retrieve current polygon
            LinearRing2D ring = getLinearRing(polyNode, sliceIndex);
            
            // resample (every two pixels) and smooth
            LinearRing2D ring2 = ring.resampleBySpacing(2.0);
            ring2 = ring2.smooth(7);
            
            ImageSliceNode sliceNode2 = createSmoothPolylineNode(ring2, sliceIndex, smoothSliceNamePattern);

            smoothNode.addSliceNode(sliceNode2);
        }

        // clear interpolated polygons node
        ImageSerialSectionsNode interpNode = getInterpolatedPolygonsNode();
        interpNode.clear();

        Collection<Integer> indices = polyNode.getSliceIndices();
        Iterator<Integer> sliceIndexIter = indices.iterator();
        if (!sliceIndexIter.hasNext())
        {
            return;
        }
        
        // Extract polygon of bottom slice
        int currentSliceIndex = sliceIndexIter.next();
        int minSliceIndex = currentSliceIndex;
        
        // get last index and indices number
        int lastIndex = minSliceIndex;
        for (int ind : polyNode.getSliceIndices())
        {
            lastIndex = ind;
        }
        int indexCount = lastIndex - minSliceIndex + 1;
        
        LinearRing2D currentPoly = getLinearRing(smoothNode, currentSliceIndex);
        
        // iterate over pairs of indices
        while (sliceIndexIter.hasNext())
        {
            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            this.fireStatusChanged(new AlgoEvent(this, "process slice range " + currentSliceIndex + " - " + nextSliceIndex));
            
            // Extract polygon of upper slice
            LinearRing2D nextPolyRef = getLinearRing(smoothNode, nextSliceIndex);
            
            // compute projection points of current poly over next poly
            LinearRing2D nextPoly = projectRingVerticesNormal(currentPoly, nextPolyRef);
            // smooth and re-project to have vertices distributed more regularly along target polygon
            nextPoly = projectRingVerticesNormal(nextPoly.smooth(15), nextPolyRef);

            // create shape for interpolated polygon
            interpNode.addSliceNode(createInterpolatedPolygonNode(currentPoly, currentSliceIndex));

            // iterate over slices in-between bottom and upper
            double dz = nextSliceIndex - currentSliceIndex;
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex < nextSliceIndex; sliceIndex++)
            {
                System.out.println("  interpolate slice " + sliceIndex);
                this.fireProgressChanged(new AlgoEvent(this, sliceIndex - minSliceIndex, indexCount));
                
                double t0 = ((double) (sliceIndex - currentSliceIndex)) / dz;
                LinearRing2D interpPoly = LinearRing2D.interpolate(currentPoly, nextPoly, t0);
                
                // create shape for interpolated polygon
                interpNode.addSliceNode(createInterpolatedPolygonNode(interpPoly, sliceIndex));
            }
            
            // prepare for next pair of indices
            currentSliceIndex = nextSliceIndex;
            currentPoly = nextPoly;
        }

        this.fireProgressChanged(new AlgoEvent(this, 1, 1));
        
        // create shape for interpolated polygon
        interpNode.addSliceNode(createInterpolatedPolygonNode(currentPoly, currentSliceIndex));
    }
    
    private ImageSliceNode createSmoothPolylineNode(LinearRing2D ring, int sliceIndex, String namePattern)
    {
        // compute name (of both shape and slice nodes)
        String sliceName = String.format(Locale.US, smoothSliceNamePattern, sliceIndex);
        
        // create the shape node
        ShapeNode shapeNode = new ShapeNode(sliceName, ring);
        shapeNode.getStyle().setColor(Color.GREEN);
        shapeNode.getStyle().setLineWidth(0.5);
        
        // create the slice node containing the shape
        ImageSliceNode sliceNode = new ImageSliceNode(sliceName, sliceIndex);
        sliceNode.addNode(shapeNode);
        
        return sliceNode;
    }
    

    /**
     * Computes interpolated polygons from the smoothed polygons.
     */
    private void interpolatePolygons()
    {
        // retrieve smooth node
        ImageSerialSectionsNode smoothNode = getSmoothPolygonsNode();

        // clear interpolated polygons node
        ImageSerialSectionsNode interpNode = getInterpolatedPolygonsNode();
        interpNode.clear();

        Collection<Integer> indices = smoothNode.getSliceIndices();
        Iterator<Integer> sliceIndexIter = indices.iterator();
        if (!sliceIndexIter.hasNext())
        {
            return;
        }
        
        // Extract polygon of bottom slice
        int currentSliceIndex = sliceIndexIter.next();
        int minSliceIndex = currentSliceIndex;
        
        // get last index and indices number
        int lastIndex = minSliceIndex;
        for (int ind : smoothNode.getSliceIndices())
        {
            lastIndex = ind;
        }
        int indexCount = lastIndex - minSliceIndex + 1;
        
        LinearRing2D currentPoly = getLinearRing(smoothNode, currentSliceIndex);
        
        // iterate over pairs of indices
        while (sliceIndexIter.hasNext())
        {
            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            this.fireStatusChanged(new AlgoEvent(this, "process slice range " + currentSliceIndex + " - " + nextSliceIndex));
            
            // Extract polygon of upper slice
            LinearRing2D nextPolyRef = getLinearRing(smoothNode, nextSliceIndex);
            
            // compute projection points of current poly over next poly
            LinearRing2D nextPoly = projectRingVerticesNormal(currentPoly, nextPolyRef);
            // smooth and re-project to have vertices distributed more regularly along target polygon
            nextPoly = projectRingVerticesNormal(nextPoly.smooth(15), nextPolyRef);

            // create shape for interpolated polygon
            interpNode.addSliceNode(createInterpolatedPolygonNode(currentPoly, currentSliceIndex));

            // iterate over slices in-between bottom and upper
            double dz = nextSliceIndex - currentSliceIndex;
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex < nextSliceIndex; sliceIndex++)
            {
                System.out.println("  interpolate slice " + sliceIndex);
                this.fireProgressChanged(new AlgoEvent(this, sliceIndex - minSliceIndex, indexCount));
                
                double t0 = ((double) (sliceIndex - currentSliceIndex)) / dz;
                LinearRing2D interpPoly = LinearRing2D.interpolate(currentPoly, nextPoly, t0);
                
                // create shape for interpolated polygon
                interpNode.addSliceNode(createInterpolatedPolygonNode(interpPoly, sliceIndex));
            }
            
            // prepare for next pair of indices
            currentSliceIndex = nextSliceIndex;
            currentPoly = nextPoly;
        }

        this.fireProgressChanged(new AlgoEvent(this, 1, 1));
        
        // create shape for interpolated polygon
        interpNode.addSliceNode(createInterpolatedPolygonNode(currentPoly, currentSliceIndex));
    }
    
    /**
     * Computes the projection of each vertex of the source ring onto the target
     * ring, by restricting on edges whose normal is towards the point.
     * 
     * @param sourceRing
     *            the polyline that will be projected.
     * @param targetRing
     *            the polyline to project on.
     * @return a new polyline whose vertices 1) are in correspondence with
     *         vertices, and 2) belong to (edges of) target ring.
     */
    
    private static final LinearRing2D projectRingVerticesNormal(LinearRing2D sourceRing, LinearRing2D targetRing)
    {
        int nv = sourceRing.vertexCount();

        // compute normals to edges of source ring
        ArrayList<Vector2D> sourceEdgeNormals = new ArrayList<Vector2D>(nv);
        for (Polyline2D.Edge edge : sourceRing.edges())
        {
            Vector2D tangent = new Vector2D(edge.source().position(), edge.target().position());
            sourceEdgeNormals.add(tangent.normalize().rotate90(-1));
        }

        // compute vertex normals of source ring
        ArrayList<Vector2D> vertexNormals = new ArrayList<Vector2D>(nv);
        for (int i = 0; i < nv; i++)
        {
            Vector2D normal1 = sourceEdgeNormals.get((i + nv - 1) % nv);
            Vector2D normal2 = sourceEdgeNormals.get(i);
            vertexNormals.add(normal1.plus(normal2).times(0.5));
        }
        
        // compute normals to edges of target ring
        ArrayList<Vector2D> edgeNormals = new ArrayList<Vector2D>(targetRing.vertexCount());
        for (Polyline2D.Edge edge : targetRing.edges())
        {
            Vector2D tangent = new Vector2D(edge.source().position(), edge.target().position());
            edgeNormals.add(tangent.rotate90(-1));
        }
                    
        // compute projection points of current poly over next poly
        LinearRing2D nextPoly = LinearRing2D.create(nv);
        for (int iv = 0; iv < nv; iv++)
        {
            Point2D point = sourceRing.vertexPosition(iv);
            double x = point.getX();
            double y = point.getY();
            
            double dist, minDist = Double.POSITIVE_INFINITY;
            Point2D proj = targetRing.vertexPosition(0);
            
            for (int iEdge = 0; iEdge < targetRing.vertexCount(); iEdge++)
            {
                // do not process edges whose normal is opposite to vertex
                if (edgeNormals.get(iEdge).dotProduct(vertexNormals.get(iv)) < 0)
                {
                    continue;
                }
                
                LineSegment2D seg = targetRing.edge(iEdge).curve();
                dist = seg.distance(x, y);
                if (dist < minDist)
                {
                    minDist = dist;
                    proj = seg.projection(point);
                }
            }
            
            nextPoly.addVertex(proj);
        }
        
        return nextPoly;
    }
    
    private ImageSliceNode createInterpolatedPolygonNode(LinearRing2D ring, int sliceIndex)
    {
        // compute name (of both shape and slice nodes)
        String sliceName = String.format(Locale.US, interpolatedSliceNamePattern, sliceIndex);
    
        // create a node for the shape
        ShapeNode shapeNode = new ShapeNode(sliceName, ring);
        shapeNode.getStyle().setColor(Color.MAGENTA);
        shapeNode.getStyle().setLineWidth(1.0);
    
        // create the slice for interpolated version
        ImageSliceNode sliceNode = new ImageSliceNode(sliceName, sliceIndex);
        sliceNode.addNode(shapeNode);
        
        return sliceNode;
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
        ImageViewer viewer = image3dFrame.getImageView();
        if (!(viewer instanceof StackSliceViewer))
        {
            throw new RuntimeException("requires an instance of stack slice viewer");
        }
    
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
                Collection<Point2D> points = computeIntersectionsWithHorizontalLine(ring, y);
                if (points.size() % 2 != 0)
                {
                    System.err.println("can not manage odd number of intersections bewteen linear ring and straight line");
                    continue;
                }
                
                Iterator<Point2D> iter = points.iterator();
                while (iter.hasNext())
                {
                    int x0 = (int) Math.max(0, Math.ceil(iter.next().getX()));
                    int x1 = (int) Math.min(sizeX, Math.floor(iter.next().getX() + 1));
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

    private Collection<Point2D> computeIntersectionsWithHorizontalLine(LinearRing2D ring, int yLine)
    {
        StraightLine2D line = new StraightLine2D(new Point2D(0, yLine), new Vector2D(1, 0));
        return Point2D.sortPoints(ring.intersections(line));
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
}
