/**
 * 
 */
package imago.plugin.plugin.crop;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.generic.GenericArray2D;
import net.sci.array.interp.LinearInterpolator3D;
import net.sci.array.interp.ScalarFunction3D;
import net.sci.array.process.math.FiniteDifferences;
import net.sci.array.scalar.Float32Array2D;
import net.sci.array.scalar.SliceBufferedUInt8Array3D;
import net.sci.array.scalar.UInt8Array2D;
import net.sci.array.scalar.UInt8Array3D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.Vector2D;
import net.sci.geom.geom2d.polygon.LineString2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.geom3d.Vector3D;
import net.sci.geom.geom3d.polyline.LineString3D;
import net.sci.image.Image;
import net.sci.image.io.MetaImageWriter;
import net.sci.image.process.filter.GaussianFilter5x5;

/**
 * Creation of 3D surface from a series of (open) polylines manually defined on
 * several XY slices.
 * 
 * Several Processing steps:
 * <ol>
 * <li>Initialize node tree for the plugin. Create a node "surface3d" and three
 * subnodes: "polylines", "smooth", and "interpolate".</li>
 * <li>Populate the 'polylines" node, either manually or by loading data from a
 * JSON file.</li>
 * <li>Smooth each polyline (resample and smooth).</li>
 * <li>Interpolate smoothed polylines to populate also slices within annotated
 * slices.</li>
 * </ol>
 * 
 * @see Crop3D
 * @see CreateSurface3DPlugin
 * 
 * @author dlegland
 *
 */
public class Surface3D extends AlgoStub implements AlgoListener
{
    // ===================================================================
    // Static factories
    
    public static final Surface3D loadAnalysis(File file, ImagoFrame parentFrame) throws IOException
    {
        // Default values to load
        ImageInfo imageInfo = null;
        ImageSerialSectionsNode polylinesNode = null;

        // parse file using JSON
        // create json file reader
        FileReader fileReader = new FileReader(file);
        JsonReader reader = new JsonReader(new BufferedReader(fileReader));

        // start parsing Surface3D object
        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();

            if (name.equalsIgnoreCase("type"))
            {
                // check the file start with the Surface3D data header
                String string = reader.nextString();
                if (!string.equalsIgnoreCase("Surface3D"))
                {
                    throw new RuntimeException("Expect a file containing a Surface3D type data");
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
            else if(name.equalsIgnoreCase("polylines"))
            {
                // create a scene reader to parse polyline
                JsonSceneReader sceneReader = new JsonSceneReader(reader);
                Node node = sceneReader.readNode();

                // expect a group node...
                if (!(node instanceof ImageSerialSectionsNode))
                {
                    throw new RuntimeException("JSON file should contains a single ImageSerialSectionsNode instance.");
                }

                polylinesNode = (ImageSerialSectionsNode) node;
            } 
            else
            {
                System.out.println("Unknown field name when reading Surface3D: " + name);
                reader.skipValue();
            }
        }
        reader.endObject();

        // Check necessary information have been loaded
        if (imageInfo.filePath == null)
        {
            throw new RuntimeException("Could not load image file information.");
        }
        
        // create a new plugin
        CreateSurface3DPlugin newFrame = new CreateSurface3DPlugin();
        newFrame.run(parentFrame, null);
        
        // create an image viewer for the file given in ImageInfo
        newFrame.openImage(new File(imageInfo.filePath));
        
        Surface3D surf3d = newFrame.surf3d;
        if (surf3d == null)
        {
            throw new RuntimeException("Expect inner surf3d field to be initialized");
        }
        
        // update polylines of the new analysis
        surf3d.populatePolylines((ImageSerialSectionsNode) polylinesNode);
        newFrame.updatePolylineListView();
        
        // need to call this to update items to display 
        ImageViewer viewer = newFrame.imageFrame.getImageView();
        viewer.refreshDisplay(); 
        viewer.repaint();
        
        return surf3d;
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
                // check the file start with the Surface3D data header
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
     * The handle to the data structure containing the image and the node tree.
     */
    ImageHandle imageHandle;
    
    int nSlices;
    
    
    // ===================================================================
    // Constructor

    public Surface3D(ImageHandle imageHandle)
    {
        this.imageHandle = imageHandle;

        // get current image data
        Image image = this.imageHandle.getImage();
        Array<?> array = image.getData();
        if (array.dimensionality() != 3)
        {
            throw new RuntimeException("Requires an image containing 3D Array");
        }
        nSlices = array.size(2);
    }
    

    // ===================================================================
    // Processing methods
    
    /**
     * Reads the series of polylines from a file in JSON format.
     * 
     * @param file
     *            the file to read polylines from
     * @throws IOException
     *             if a I/O problem occurred.
     */
    public void readPolylinesFromJson(File file) throws IOException
    {
        // reset current state of the Crop3D plugin
        initializeNodes();

        JsonSceneReader sceneReader;

        FileReader fileReader = new FileReader(file);
        JsonReader jsonReader = new JsonReader(new BufferedReader(fileReader));
        sceneReader = new JsonSceneReader(jsonReader);

        // expect a group node...
        Node node = sceneReader.readNode();
        if (!(node instanceof ImageSerialSectionsNode))
        {
            throw new RuntimeException("JSON file should contains a single ImageSerialSectionsNode instance.");
        }

//        ImageSerialSectionsNode polyNode = getPolylinesNode();
        populatePolylines((ImageSerialSectionsNode) node);

        System.out.println("reading polylines terminated.");
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
        
        // open Surface3D node
        jsonWriter.beginObject();
        jsonWriter.name("type").value("Surface3D");
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
        
        // one node for the polyline
        ImageSerialSectionsNode polyNode = getPolylinesNode();
        if (polyNode != null)
        {
            JsonSceneWriter sceneWriter = new JsonSceneWriter(jsonWriter);
            jsonWriter.name("polylines");
            sceneWriter.writeNode(polyNode);
        }
      
        // close Surface3D
        jsonWriter.endObject();

        fileWriter.close();
        
        System.out.println("Saving analysis terminated.");
    }
    
    /**
     * Saves the series of polylines into a file in JSON format.
     * 
     * @param file
     *            the file to writes polylines.
     * @throws IOException
     *             if a I/O problem occurred.
     */
    public void savePolylinesAsJson(File file) throws IOException
    {
        ImageSerialSectionsNode polyNode = getPolylinesNode();
        if (polyNode == null)
        {
            throw new RuntimeException("Current image does not contain Surface polylines information");
        }
        
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        JsonWriter jsonWriter = new JsonWriter(new PrintWriter(fileWriter));
        jsonWriter.setIndent("  ");
        JsonSceneWriter writer = new JsonSceneWriter(jsonWriter);

        writer.writeNode(polyNode);

        fileWriter.close();
        
        System.out.println("Saving polylines terminated.");
    }
    
    /**
     * Populates the "polylines" node of current image handle from the specified
     * ImageSerialSectionsNode.
     * 
     * @param polylinesNode
     *            the node containing the map between slice indices and shape
     */
    public void populatePolylines(ImageSerialSectionsNode polylinesNode)
    {
        ImageSerialSectionsNode polyNode = getPolylinesNode();
        for(ImageSliceNode child : polylinesNode.children())
        {
            // check that all children of slice nodes are shape nodes with polyline2D geometry
            for (Node child2 : child.children())
            {
                if (!(child2 instanceof ShapeNode))
                {
                    throw new RuntimeException("Expect all ImageSliceNode to contain shape nodes.");
                }

                if(!(((ShapeNode) child2).getGeometry() instanceof LineString2D))
                {
                    throw new RuntimeException("Expect all Shape nodes to contain LineString2D geometries.");
                }
            }

            polyNode.addSliceNode(child);
        }
    }
    
    public void addPolyline(int sliceIndex, Polyline2D poly)
    {
        System.out.println("surface3d - add polyline");

        // select node containing manually delineated polygons
        ImageSerialSectionsNode polyNode = getPolylinesNode();
        
        // compute slice and polygon name 
        int nDigits = (int) Math.ceil(Math.log10(this.nSlices));
        String sliceName = String.format(Locale.US, "slice%0" + nDigits + "d", sliceIndex);
        
        // Create a new LinearRing shape from the boundary of the polygon
        ShapeNode shapeNode = new ShapeNode(sliceName, poly);
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
     * Compute interpolated polylines from the manually created polylines.
     * 
     * Processing steps:
     * <ol>
     * <li>Retrieve the node containing user-created polylines</li>
     * <li>Create or refresh the "smooth" and the "interp" nodes</li>
     * <li>Resample and smooth each polyline</li>
     * <li>interpolate each pair of consecutive polylines, by projecting
     * vertices of the first polyline onto the second one, and smoothing</li>
     * </ol>
     */
    public void interpolatePolylines()
    {
        System.out.println("surface3d - interpolate polylines");

        // number of digits for creating slice names
        int nDigits = (int) Math.ceil(Math.log10(this.nSlices));
        String sliceNamePattern = "smooth%0" + nDigits + "d";
        
        ImageSerialSectionsNode polyNode = getPolylinesNode();
        if (polyNode.isLeaf())
        {
            throw new RuntimeException("Requires the frame to contains valid Surface3D Polylines");
        }
        
        // clear output nodes
        ImageSerialSectionsNode smoothNode = getSmoothPolylinesNode();
        smoothNode.clear();
        
        // iterate over polylines to create a smoothed version
        for (ImageSliceNode sliceNode : polyNode.children())
        {
            int sliceIndex = sliceNode.getSliceIndex(); 
            System.out.println("smooth polyline on slice " + sliceIndex);
            
            // retrieve current polyline
            LineString2D poly = getPolyline(polyNode, sliceIndex);
            
            // resample (every two pixels) and smooth
            LineString2D poly2 = poly.resampleBySpacing(2.0);
            poly2 = poly2.smooth(7);

            String sliceName = String.format(Locale.US, sliceNamePattern, sliceIndex);
            ShapeNode shapeNode2 = createPolylineNode(poly2, sliceName);
            
            // create the slice for smooth version
            ImageSliceNode sliceNode2 = new ImageSliceNode(sliceName, sliceIndex);
            sliceNode2.addNode(shapeNode2);
            
            smoothNode.addSliceNode(sliceNode2);
        }

        // clear interpolated polygons node
        ImageSerialSectionsNode interpNode = getInterpolatedPolylinesNode();
        interpNode.clear();
        
        // retrieve the array of indices for slices containing user polyline
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
        
        LineString2D currentPoly = getPolyline(smoothNode, currentSliceIndex);
        
        // iterate over pairs of indices
        while (sliceIndexIter.hasNext())
        {
            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            this.fireStatusChanged(this, "process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            
            // Extract polyline of upper slice
            LineString2D nextPolyRef = getPolyline(smoothNode, nextSliceIndex);
            
            // compute projection points of current poly over next poly
            LineString2D nextPoly = projectLineString(currentPoly, nextPolyRef);
            
            // smooth and re-project to have vertices distributed more regularly along target polyline
            nextPoly = projectLineString(nextPoly.smooth(15), nextPolyRef);

            // create shape for interpolated polygon
            interpNode.addSliceNode(createInterpNode(currentPoly, currentSliceIndex, nDigits));

            // iterate over slices in-between bottom and upper
            double dz = nextSliceIndex - currentSliceIndex;
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex < nextSliceIndex; sliceIndex++)
            {
                System.out.println("  interpolate slice " + sliceIndex);
                this.fireProgressChanged(this, sliceIndex, lastIndex);
                
                double t0 = ((double) (sliceIndex - currentSliceIndex)) / dz;
                LineString2D interpPoly = LineString2D.interpolate(currentPoly, nextPoly, t0);
                
                // create shape for interpolated polygon
                interpNode.addSliceNode(createInterpNode(interpPoly, sliceIndex, nDigits));
            }
            
            // prepare for next pair of indices
            currentSliceIndex = nextSliceIndex;
            currentPoly = nextPoly;
        }

        this.fireProgressChanged(this, 1, 1);
        
        // create shape for interpolated polyline
        interpNode.addSliceNode(createInterpNode(currentPoly, currentSliceIndex, nDigits));
    }
    
    private static final ShapeNode createPolylineNode(Polyline2D poly, String name)
    {
        ShapeNode node = new ShapeNode(name, poly);
        node.getStyle().setColor(Color.GREEN);
        node.getStyle().setLineWidth(0.5);
        return node;
    }
    
    private static final ImageSliceNode createInterpNode(LineString2D poly, int sliceIndex, int nDigits)
    {
        // create a node for the shape
        String sliceName = String.format(Locale.US, "interp%0" + nDigits + "d", sliceIndex);
        ShapeNode shapeNode = new ShapeNode(sliceName, poly);
        shapeNode.getStyle().setColor(Color.MAGENTA);
        shapeNode.getStyle().setLineWidth(1.0);

        // create the slice for interpolated version
        ImageSliceNode sliceNode = new ImageSliceNode(sliceName, sliceIndex);
        sliceNode.addNode(shapeNode);
        
        return sliceNode;
    }
    
    /**
     * Computes the projection of an open polyline onto another open polyline,
     * by restricting projection to edges whose normal is in the same direction
     * of source vertices.
     * 
     * @param sourcePoly
     *            the polyline to project.
     * @param targetPoly
     *            the polyline to project on.
     * @return a new polyline whose vertices are located on target polyline.
     */
    private static final LineString2D projectLineString(LineString2D sourcePoly, LineString2D targetPoly)
    {
        // compute normals to edges of source ring
        ArrayList<Vector2D> sourceEdgeNormals = lineStringEdgeNormals(sourcePoly);

        // compute vertex normals of source ring
        ArrayList<Vector2D> vertexNormals = computeVertexNormals(sourceEdgeNormals);
        
        // pre-compute normals to edges of target ring
        ArrayList<Vector2D> edgeNormals = lineStringEdgeNormals(targetPoly);
        
        // allocate memory for result
        int nv = sourcePoly.vertexCount();
        LineString2D nextPoly = LineString2D.create(nv);

        // compute projection points of current poly over next poly
        for (int iv = 0; iv < nv; iv++)
        {
            // retrieve coordinates of current vertex 
            Point2D point = sourcePoly.vertexPosition(iv);
            double x = point.getX();
            double y = point.getY();
            
            // the normal associated to the current vertex
            Vector2D normal = vertexNormals.get(iv);
            
            // initialize projection result of current vertex
            double dist, minDist = Double.POSITIVE_INFINITY;
            Point2D proj = targetPoly.vertexPosition(0);
            
            // iterate over edges of target polyline
            for (int iEdge = 0; iEdge < targetPoly.vertexCount()-1; iEdge++)
            {
                // do not process edges whose normal is opposite to normal of current vertex
                if (edgeNormals.get(iEdge).dotProduct(normal) < 0)
                {
                    continue;
                }
                
                // compute distance between vertex and target edge
                LineSegment2D seg = targetPoly.edge(iEdge).curve();
                dist = seg.distance(x, y);
                
                // update closest edge
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
    
    private static final ArrayList<Vector2D> lineStringEdgeNormals(LineString2D poly)
    {
        // allocate memory for result
        int nv = poly.edgeCount();
        ArrayList<Vector2D> edgeNormals = new ArrayList<Vector2D>(nv - 1);

        // iterate over edges
        for (Polyline2D.Edge edge : poly.edges())
        {
            // compute normal of current edge
            edgeNormals.add(tangentVector(edge).normalize().rotate90(-1));
        }
        
        return edgeNormals;
    }

    private static final Vector2D tangentVector(Polyline2D.Edge edge)
    {
        return new Vector2D(edge.source().position(), edge.target().position());
    }
    
    private static final ArrayList<Vector2D> computeVertexNormals(ArrayList<Vector2D> edgeNormals)
    {
        // allocate memory for result
        int nv = edgeNormals.size() + 1;
        ArrayList<Vector2D> vertexNormals = new ArrayList<Vector2D>(nv);
        
        Iterator<Vector2D> iter = edgeNormals.iterator();
        
        // first vertex -> only one adjacent edge
        Vector2D normal1 = iter.next();
        vertexNormals.add(normal1);
        
        // iterate over inner vertices
        while(iter.hasNext())
        {
            Vector2D normal2 = iter.next();
            vertexNormals.add(normal1.plus(normal2).times(0.5));
            normal1 = normal2;
        }

        // last vertex -> only one adjacent edge
        vertexNormals.add(normal1);
        
        return vertexNormals;
    }
    
    
    // ===================================================================
    // Computation of flattened image
    
    public void flattenSurface3d(int width, int minDepth, int maxDepth, File outputFileName)
    {
        this.fireStatusChanged(this, "Compute flattened surface 3D image");
        
        // determine dimensions of result image
        UInt8Array3D array = (UInt8Array3D) this.imageHandle.getImage().getData();
        int height = array.size(2);
        int depth = maxDepth - minDepth + 1;
        System.out.println(String.format("Output image size: [%d, %d, %d]", width, height, depth));
        
        // allocate memory
        UInt8Array2D res2d = UInt8Array2D.create(width, height);
        UInt8Array3D res3d = UInt8Array3D.create(width, height, depth);
            
        // convert 2D polylines to 3D polylines
        ImageSerialSectionsNode polyNode = getInterpolatedPolylinesNode();
        Map<Integer, LineString3D> polylines3d = convert2DPolylinesTo3DPolylines(polyNode);
        
        System.out.println("Compute coordinates of 3D mesh");
        this.fireStatusChanged(this, "Compute coordinates of 3D mesh");
        Float32Array2D[] coordArrays = computeCoordArrays(width, height, polylines3d);
        Float32Array2D xCoords = coordArrays[0]; 
        Float32Array2D yCoords = coordArrays[1]; 
        Float32Array2D zCoords = coordArrays[2]; 
        
        System.out.println("Smooth coordinates of 3D mesh...");
        this.fireStatusChanged(this, "Smooth coordinates of 3D mesh...");
        // Smooth coordinate arrays
        GaussianFilter5x5 smooth = new GaussianFilter5x5();
        for (int iIter = 0; iIter < 3; iIter++)
        {
            System.out.println("  iter " + iIter);
            xCoords = (Float32Array2D) smooth.processScalar(xCoords);
            yCoords = (Float32Array2D) smooth.processScalar(yCoords);
            zCoords = (Float32Array2D) smooth.processScalar(zCoords);
        }
        
        System.out.println("Evaluate image slice...");
        this.fireStatusChanged(this, "Evaluate image slice...");
        
        // create interpolation operators, using buffering of 3D image slices
        UInt8Array3D array2 = new SliceBufferedUInt8Array3D(array, 10);
        ScalarFunction3D interp = new LinearInterpolator3D(array2);
        
        // Evaluate image slice
        for (int iy = 0; iy < height; iy++)
        {
            for (int ix = 0; ix < width; ix++)
            {
                double x = xCoords.getValue(ix, iy);
                double y = yCoords.getValue(ix, iy);
                double z = zCoords.getValue(ix, iy);
                res2d.setValue(ix, iy, interp.evaluate(x, y, z));
            }
        }
        Image sliceImage = new Image(res2d);
        
        System.out.println("Save slice image.");
        this.fireStatusChanged(this, "Save slice image.");
        File sliceFile = new File(outputFileName.getParent(), "slice.mhd"); 
        MetaImageWriter sliceWriter = new MetaImageWriter(sliceFile);
        try
        {
            sliceWriter.writeImage(sliceImage);
        }
        catch(IOException ex)
        {
            System.err.println(ex);
            return;
        }
        
        Array2D<Vector3D> normals = computeNormals(xCoords, yCoords, zCoords);
        
        // create result image
        System.out.println("Create result image...");
        this.fireStatusChanged(this, "Create result image.");
        for (int id = 0; id < depth; id++)
        {
            System.out.println(String.format("  z = %d / %d", id, depth));
            this.fireProgressChanged(this, id, depth);
            double d = id + minDepth;
            for (int iv = 0; iv < height; iv++)
            {
                for (int iu = 0; iu < width; iu++)
                {
                    Point3D pos = new Point3D(xCoords.getValue(iu,iv), yCoords.getValue(iu,iv), zCoords.getValue(iu,iv));
                    Vector3D normal = normals.get(iu, iv);
                    Point3D pos2 = pos.plus(normal.times(d));
                    
                    // retrieve coordinates as double values
                    double x2 = pos2.getX();
                    double y2 = pos2.getY();
                    double z2 = pos2.getZ();
                    
                    // evaluate within 3D array
                    double value = interp.evaluate(x2, y2, z2);
                    res3d.setValue(iu, iv, id, value);
                }
            }
        }
        this.fireProgressChanged(this, 1, 1);
        
        // convert 3D array to image
        Image image = new Image(res3d);
        
        // and save image
        System.out.println("Save 3D image...");
        MetaImageWriter writer = new MetaImageWriter(outputFileName);
        // use this Surface3D object to propagate algo events
        writer.addAlgoListener(this);
        
        try
        {
            writer.writeImage(image);
        }
        catch(IOException ex)
        {
            System.err.println(ex);
            return;
        }
        
        this.fireStatusChanged(this, "Unfolding 3D image complete!");
    }
    
    private Map<Integer, LineString3D> convert2DPolylinesTo3DPolylines(ImageSerialSectionsNode polyNode)
    {
        Map<Integer, LineString3D> map = new TreeMap<Integer, LineString3D>();
        int sliceNumber = this.imageHandle.getImage().getSize(2);
        for (ImageSliceNode sliceNode : polyNode.children())
        {
            int sliceIndex = sliceNode.getSliceIndex();
            this.fireProgressChanged(this, sliceIndex, sliceNumber);
            
            LineString2D poly2d = getPolyline(polyNode, sliceIndex);
            int nv = poly2d.vertexCount();
            ArrayList<Point3D> pts3d = new ArrayList<Point3D>(nv);
            for (Point2D point : poly2d.vertexPositions())
            {
                pts3d.add(new Point3D(point.getX(), point.getY(), sliceIndex));
            }
            
            // Create 3D polyline
            LineString3D poly3d = LineString3D.create(pts3d);
            
            // add to map
            map.put(sliceIndex, poly3d);
        }
        this.fireProgressChanged(this, 1, 1);
        
        return map;
    }
    
    private Float32Array2D[] computeCoordArrays(int width, int height, Map<Integer, LineString3D> polylines3d)
    {
        Float32Array2D xCoords = Float32Array2D.create(width, height); 
        Float32Array2D yCoords = Float32Array2D.create(width, height); 
        Float32Array2D zCoords = Float32Array2D.create(width, height);
        
        System.out.println("Compute coordinates of 3D mesh");
        this.fireStatusChanged(this, "Compute coordinates of 3D mesh");
        for (int iz = 0; iz < height; iz++)
        {
//            System.out.println(String.format("  z = %d / %d", iz, height));
            this.fireProgressChanged(this, iz, height);
            
            LineString3D poly = polylines3d.get(iz);
            if (poly == null)
            {
                System.out.println(String.format("No 3D polyline for slice index %d, continue...", iz));
                continue;
            }
            
            // resample polyline to get inner points
            double len = poly.length();
            double step = len / (width + 2 * 10);
            LineString3D poly2 = poly.resampleBySpacing(step);
            
            for (int iv = 0; iv < width; iv++)
            {
                Point3D pt = poly2.vertexPosition(iv + 10);
                xCoords.setValue(iv, iz, pt.getX());
                yCoords.setValue(iv, iz, pt.getY());
                zCoords.setValue(iv, iz, pt.getZ());
            }
        }
        this.fireProgressChanged(this, 1, 1);
        
        return new Float32Array2D[] {xCoords, yCoords, zCoords};
    }
    
    private Array2D<Vector3D> computeNormals(Float32Array2D xCoords, Float32Array2D yCoords, Float32Array2D zCoords)
    {
        int width = xCoords.size(0);
        int height = xCoords.size(1);
        
        // create derivation operations
        FiniteDifferences derivU = new FiniteDifferences(0);
        FiniteDifferences derivV = new FiniteDifferences(1);
        
        // allocate memory
        Float32Array2D dxu = Float32Array2D.create(width, height);
        Float32Array2D dxv = Float32Array2D.create(width, height);
        Float32Array2D dyu = Float32Array2D.create(width, height);
        Float32Array2D dyv = Float32Array2D.create(width, height);
        Float32Array2D dzu = Float32Array2D.create(width, height);
        Float32Array2D dzv = Float32Array2D.create(width, height);
        
        // performs derivations
        derivU.processScalar(xCoords, dxu); 
        derivV.processScalar(xCoords, dxv); 
        derivU.processScalar(yCoords, dyu); 
        derivV.processScalar(yCoords, dyv); 
        derivU.processScalar(zCoords, dzu); 
        derivV.processScalar(zCoords, dzv); 
        
        // evaluate normal vector for each vertex of the mesh
        Array2D<Vector3D> normals = GenericArray2D.create(width, height, new Vector3D());
        for (int iv = 0; iv < height; iv++)
        {
            for (int iu = 0; iu < width; iu++)
            {
                Vector3D du = new Vector3D(dxu.getValue(iu,iv), dyu.getValue(iu,iv), dzu.getValue(iu,iv));
                Vector3D dv = new Vector3D(dxv.getValue(iu,iv), dyv.getValue(iu,iv), dzv.getValue(iu,iv));
                normals.set(iu, iv, du.crossProduct(dv).normalize());
            }
        }
        
        return normals;
    }

    
    // ===================================================================
    // Management of nodes
    
    /**
     * Resets the nodes associated to a Surface3D plugin. 
     * 
     * @param handle the ImageHandle containing the nodes to reset.
     */
    public void initializeNodes()
    {
        // get root node
        GroupNode rootNode = ((GroupNode) this.imageHandle.getRootNode());
        
        // remove old crop node if it exists
        if (rootNode.hasChildWithName("surface3d"))
        {
            rootNode.removeNode(rootNode.getChild("surface3d"));
        }
        
        // create new crop node
        GroupNode cropNode = new GroupNode("surface3d");
        rootNode.addNode(cropNode);
    
        // add child nodes
        cropNode.addNode(new ImageSerialSectionsNode("polylines"));
        cropNode.addNode(new ImageSerialSectionsNode("smooth"));
        cropNode.addNode(new ImageSerialSectionsNode("interp"));
    }


    /**
     * @param handle
     *            the ImageHandle containing the nodes.
     * @return true if the image handle contains the necessary scene nodes for
     *         performing Crop3D.
     */
    public boolean hasSurface3dNodes()
    {
        // get root node
        GroupNode rootNode = ((GroupNode) this.imageHandle.getRootNode());
        
        // remove old crop node if it exists
        if (!rootNode.hasChildWithName("surface3d"))
        {
            return false;
        }
        
        // create new crop node
        GroupNode cropNode = (GroupNode) rootNode.getChild("surface3d");

        // add child nodes
        if (!(cropNode.hasChildWithName("polylines"))) return false;
        if (!(cropNode.hasChildWithName("smooth"))) return false;
        if (!(cropNode.hasChildWithName("interp"))) return false;
        
        // if all conditions are checked, return true
        return true;
    }
    
    /**
     * @return the group node that contains the polygons node, the smooth node,
     *         and the interp node.
     */
    public GroupNode getSurface3dNode()
    {
        // get root node
        GroupNode rootNode = ((GroupNode) this.imageHandle.getRootNode());

        if (rootNode.hasChildWithName("surface3d"))
        {
            return (GroupNode) rootNode.getChild("surface3d");
        }

        GroupNode cropNode = new GroupNode("surface3d");
        rootNode.addNode(cropNode);
        return cropNode;
    }
    
    
    public ImageSerialSectionsNode getPolylinesNode()
    {
        GroupNode cropNode = getSurface3dNode();
        
        if (cropNode.hasChildWithName("polylines"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("polylines");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("polylines");
        cropNode.addNode(polyNode);
        return polyNode;
    }
    
    public ImageSerialSectionsNode getSmoothPolylinesNode()
    {
        GroupNode cropNode = getSurface3dNode();
        
        if (cropNode.hasChildWithName("smooth"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("smooth");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("smooth");
        cropNode.addNode(polyNode);
        return polyNode;
    }
    
    public ImageSerialSectionsNode getInterpolatedPolylinesNode()
    {
        GroupNode cropNode = getSurface3dNode();
        
        if (cropNode.hasChildWithName("interp"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("interp");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("interp");
        cropNode.addNode(polyNode);
        return polyNode;
    }

    /**
     * Retrieve the polyline geometry at the specified index from the serial
     * sections node.
     * 
     * ImageSerialSectionsNode -> ImageSliceSection -> ShapeNode -> Geometry.
     * 
     * @param node
     *            the node mapping to ImageSliceSections
     * @param index
     *            the index of the slice
     * @return the polyline geometry contained in the specified slice.
     */
    private static final LineString2D getPolyline(ImageSerialSectionsNode node, int index)
    {
        ShapeNode shapeNode = (ShapeNode) node.getSliceNode(index).children().iterator().next();
        return (LineString2D) shapeNode.getGeometry();
    }


    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
        fireProgressChanged(evt);
    }
    
    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
        fireStatusChanged(evt);
    }

}
