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
import java.util.ArrayList;
import java.util.Collection;
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
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.viewer.StackSliceViewer;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.Vector2D;
import net.sci.geom.geom2d.polygon.LineString2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.image.Image;

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
public class Surface3D extends AlgoStub
{
    
    // ===================================================================
    // Class members
    
    ImageFrame image3dFrame;
    ImageHandle imageHandle;
    
    
    // ===================================================================
    // Constructor

    public Surface3D(ImageFrame image3dFrame)
    {
        this.image3dFrame = image3dFrame;
        this.imageHandle = image3dFrame.getImageHandle();
    }
    

    // ===================================================================
    // Processing methods

    
    // ===================================================================
    // Management of nodes
    
    /**
     * Reset the nodes associated to a Crop3D plugin. 
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

        try 
        {
            // expect a group node...
            Node node = sceneReader.readNode();
            if (!(node instanceof ImageSerialSectionsNode))
            {
                throw new RuntimeException("JSON file should contains a single ImageSerialSectionsNode instance.");
            }
            
            ImageSerialSectionsNode polyNode = getPolylinesNode();
            for(ImageSliceNode child : ((ImageSerialSectionsNode) node).children())
            {
                polyNode.addSliceNode(child);
            }

        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        System.out.println("reading polylines terminated.");
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
            System.err.println("Current image does not contain Surface polylines information");
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
        
        System.out.println("Saving polylines terminated.");
    }
    
    public void addPolyline(int sliceIndex, Polyline2D poly)
    {
        System.out.println("surface3d - add polyline");
        
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
        ImageSerialSectionsNode polyNode = getPolylinesNode();
        
        // compute slice and polygon name 
        int nDigits = (int) Math.ceil(Math.log10(nSlices));
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

        // Check type is image frame
        ImageViewer viewer = image3dFrame.getImageView();
        if (!(viewer instanceof StackSliceViewer))
        {
            System.out.println("requires an instance of stack slice viewer");
            return;
        }

        // get current image data
        Image image = this.imageHandle.getImage();
        Array<?> array = image.getData();
        
        // number of digits for creating slice names
        int nDigits = (int) Math.ceil(Math.log10(array.size(2)));
        String sliceNamePattern = "smooth%0" + nDigits + "d";
        
        if (array.dimensionality() != 3)
        {
            throw new RuntimeException("Requires an image containing 3D Array");
        }

        ImageSerialSectionsNode polyNode = getPolylinesNode();
        if (polyNode.isLeaf())
        {
            JOptionPane.showMessageDialog(image3dFrame.getWidget(),
                    "Requires the frame to contains valid Surface3D Polylines",
                    "Surface3D Error", JOptionPane.INFORMATION_MESSAGE);
            return;
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
        int indexCount = lastIndex - minSliceIndex + 1;
        
        LineString2D currentPoly = getPolyline(smoothNode, currentSliceIndex);
        
        // iterate over pairs of indices
        while (sliceIndexIter.hasNext())
        {
            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            this.fireStatusChanged(new AlgoEvent(this, "process slice range " + currentSliceIndex + " - " + nextSliceIndex));
            
            // Extract polyline of upper slice
            LineString2D nextPolyRef = getPolyline(smoothNode, nextSliceIndex);
            
            // compute projection points of current poly over next poly
            LineString2D nextPoly = projectLineStringVertices(currentPoly, nextPolyRef);
            
            // smooth and re-project to have vertices distributed more regularly along target polyline
            nextPoly = projectLineStringVertices(nextPoly.smooth(15), nextPolyRef);

            // create shape for interpolated polygon
            interpNode.addSliceNode(createInterpNode(currentPoly, currentSliceIndex, nDigits));

            // iterate over slices in-between bottom and upper
            double dz = nextSliceIndex - currentSliceIndex;
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex < nextSliceIndex; sliceIndex++)
            {
                System.out.println("  interpolate slice " + sliceIndex);
                this.fireProgressChanged(new AlgoEvent(this, sliceIndex - minSliceIndex, indexCount));
                
                double t0 = ((double) (sliceIndex - currentSliceIndex)) / dz;
                LineString2D interpPoly = LineString2D.interpolate(currentPoly, nextPoly, t0);
                
                // create shape for interpolated polygon
                interpNode.addSliceNode(createInterpNode(interpPoly, sliceIndex, nDigits));
            }
            
            // prepare for next pair of indices
            currentSliceIndex = nextSliceIndex;
            currentPoly = nextPoly;
        }

        this.fireProgressChanged(new AlgoEvent(this, 1, 1));
        
        // create shape for interpolated polyline
        interpNode.addSliceNode(createInterpNode(currentPoly, currentSliceIndex, nDigits));
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
    private LineString2D getPolyline(ImageSerialSectionsNode node, int index)
    {
        ShapeNode shapeNode = (ShapeNode) node.getSliceNode(index).children().iterator().next();
        return (LineString2D) shapeNode.getGeometry();
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
    
    private static final LineString2D projectLineStringVertices(LineString2D sourcePoly, LineString2D targetPoly)
    {
        int nv = sourcePoly.vertexCount();

        // compute normals to edges of source ring
        ArrayList<Vector2D> sourceEdgeNormals = new ArrayList<Vector2D>(nv);
        Vector2D tangent = new Vector2D();
        for (Polyline2D.Edge edge : sourcePoly.edges())
        {
            tangent = new Vector2D(edge.source().position(), edge.target().position());
            sourceEdgeNormals.add(tangent.normalize().rotate90(-1));
        }
        // for the last vertex (index = nv-1), use the tangent of the last edge (index=nv-2)
        sourceEdgeNormals.add(tangent.normalize().rotate90(-1));

        // compute vertex normals of source ring
        ArrayList<Vector2D> vertexNormals = new ArrayList<Vector2D>(nv);
        for (int i = 0; i < nv; i++)
        {
            Vector2D normal1 = sourceEdgeNormals.get(Math.max(i - 1, 0));
            Vector2D normal2 = sourceEdgeNormals.get(i);
            vertexNormals.add(normal1.plus(normal2).times(0.5));
        }
        
        // pre-compute normals to edges of target ring
        ArrayList<Vector2D> edgeNormals = new ArrayList<Vector2D>(targetPoly.vertexCount());
        for (Polyline2D.Edge edge : targetPoly.edges())
        {
            tangent = new Vector2D(edge.source().position(), edge.target().position());
            edgeNormals.add(tangent.rotate90(-1));
        }
        // for the last vertex (index = nv-1), use the tangent of the last edge (index=nv-2)
        edgeNormals.add(tangent.normalize().rotate90(-1));
                    
        // compute projection points of current poly over next poly
        LineString2D nextPoly = new LineString2D(nv);
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
    
    
    // ===================================================================
    // Management of nodes

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
}
