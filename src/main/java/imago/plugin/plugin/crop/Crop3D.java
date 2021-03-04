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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
 * Performs 3D Crop on a 3D stack. Contains the process methods, the GUI is managed by the Crop3DPlugin class.
 * 
 * @see Crop3DPlugin
 * 
 * @author dlegland
 *
 */
public class Crop3D extends AlgoStub
{
    // ===================================================================
    // Old static methods, will be deprecated
    
    /**
     * Reset the nodes associated to a Crop3D plugin. 
     * 
     * @param handle the ImageHandle containing the nodes to reset.
     */
    public static final void initializeCrop3dNodes(ImageHandle handle)
    {
        // get root node
        GroupNode rootNode = ((GroupNode) handle.getRootNode());
        
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
    
    /**
     * @param handle
     *            the ImageHandle containing the nodes.
     * @return true if the image handle contains the necessary scene nodes for
     *         performing Crop3D.
     */
    public static final boolean hasCrop3dNodes(ImageHandle handle)
    {
        // get root node
        GroupNode rootNode = ((GroupNode) handle.getRootNode());
        
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
    
    
    public static final GroupNode getCrop3dNode(ImageHandle handle)
    {
        // get root node
        GroupNode rootNode = ((GroupNode) handle.getRootNode());

        if (rootNode.hasChildWithName("crop3d"))
        {
            return (GroupNode) rootNode.getChild("crop3d");
        }

        GroupNode cropNode = new GroupNode("crop3d");
        rootNode.addNode(cropNode);
        return cropNode;
    }
    
    
    public static final ImageSerialSectionsNode getPolygonsNode(ImageHandle handle)
    {
        GroupNode cropNode = getCrop3dNode(handle);
        
        if (cropNode.hasChildWithName("polygons"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("polygons");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("polygons");
        cropNode.addNode(polyNode);
        return polyNode;
    }
    
    public static final ImageSerialSectionsNode getSmoothPolygonsNode(ImageHandle handle)
    {
        GroupNode cropNode = getCrop3dNode(handle);
        
        if (cropNode.hasChildWithName("smooth"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("smooth");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("smooth");
        cropNode.addNode(polyNode);
        return polyNode;
    }
    
    public static final ImageSerialSectionsNode getInterpolatedPolygonsNode(ImageHandle handle)
    {
        GroupNode cropNode = getCrop3dNode(handle);
        
        if (cropNode.hasChildWithName("interp"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("interp");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("interp");
        cropNode.addNode(polyNode);
        return polyNode;
    }
 
    
    // ===================================================================
    // Class members
    
    ImageFrame image3dFrame;
    ImageHandle imageHandle;
    
    
    // ===================================================================
    // Constructor

    public Crop3D(ImageFrame image3dFrame)
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
        ImageSerialSectionsNode polyNode = Crop3D.getPolygonsNode(this.imageHandle);
        
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
    
    public void interpolatePolygons()
    {
        System.out.println("crop3d - interpolate polygons");

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
        
        if (array.dimensionality() != 3)
        {
            throw new RuntimeException("Requires an image containing 3D Array");
        }

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

            ShapeNode shapeNode = (ShapeNode) sliceNode.children().iterator().next();
            LinearRing2D ring = (LinearRing2D) shapeNode.getGeometry();
            LinearRing2D ring2 = ring.resampleBySpacing(2.0); // every 2 pixels
            ring2 = ring2.smooth(7);

            String sliceName = String.format(Locale.US, "smooth%0" + nDigits + "d", sliceIndex);
            ShapeNode shapeNode2 = new ShapeNode(sliceName, ring2);
            shapeNode2.getStyle().setColor(Color.GREEN);
            shapeNode2.getStyle().setLineWidth(0.5);
            
            // create the slice for smooth version
            ImageSliceNode sliceNode2 = new ImageSliceNode(sliceName, sliceIndex);
            sliceNode2.addNode(shapeNode2);
            
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
        
        ShapeNode shapeNode = (ShapeNode) smoothNode.getSliceNode(currentSliceIndex).children().iterator().next();
        LinearRing2D currentPoly = (LinearRing2D) shapeNode.getGeometry();
//        // resample and smooth the polygon
//        currentPoly = currentPoly.resampleBySpacing(2.0).smooth(7);
        
        // iterate over pairs of indices
        while (sliceIndexIter.hasNext())
        {
            int nextSliceIndex = sliceIndexIter.next();
            System.out.println("process slice range " + currentSliceIndex + " - " + nextSliceIndex);
            this.fireStatusChanged(new AlgoEvent(this, "process slice range " + currentSliceIndex + " - " + nextSliceIndex));
            
            // Extract polygon of upper slice
            shapeNode = (ShapeNode) smoothNode.getSliceNode(nextSliceIndex).children().iterator().next();
            LinearRing2D nextPolyRef = (LinearRing2D) shapeNode.getGeometry();
            
            // compute projection points of current poly over next poly
            LinearRing2D nextPoly = projectRingVerticesNormal(currentPoly, nextPolyRef);
            // smooth and re-project to have vertices distributed more regularly along target polygon
            nextPoly = projectRingVerticesNormal(nextPoly.smooth(15), nextPolyRef);

            // create shape for interpolated polygon
            interpNode.addSliceNode(createInterpNode(currentPoly, currentSliceIndex, nDigits));

            // iterate over slices in-between bottom and upper
            double dz = nextSliceIndex - currentSliceIndex;
            for (int sliceIndex = currentSliceIndex + 1; sliceIndex < nextSliceIndex; sliceIndex++)
            {
                System.out.println("  interpolate slice " + sliceIndex);
                this.fireProgressChanged(new AlgoEvent(this, sliceIndex - minSliceIndex, indexCount));
                
                double t0 = ((double) (sliceIndex - currentSliceIndex)) / dz;
                LinearRing2D interpPoly = interpolateRings(currentPoly, nextPoly, t0);
                
                // create shape for interpolated polygon
                interpNode.addSliceNode(createInterpNode(interpPoly, sliceIndex, nDigits));
            }
            
            // prepare for next pair of indices
            currentSliceIndex = nextSliceIndex;
            currentPoly = nextPoly;
        }

        this.fireProgressChanged(new AlgoEvent(this, 1, 1));
        
        // create shape for interpolated polygon
        interpNode.addSliceNode(createInterpNode(currentPoly, currentSliceIndex, nDigits));
    }
    
    private static final ImageSliceNode createInterpNode(LinearRing2D ring, int sliceIndex, int nDigits)
    {
        // create a node for the shape
        String sliceName = String.format(Locale.US, "interp%0" + nDigits + "d", sliceIndex);
        ShapeNode shapeNode = new ShapeNode(sliceName, ring);
        shapeNode.getStyle().setColor(Color.MAGENTA);
        shapeNode.getStyle().setLineWidth(1.0);

        // create the slice for interpolated version
        ImageSliceNode sliceNode = new ImageSliceNode(sliceName, sliceIndex);
        sliceNode.addNode(shapeNode);
        
        return sliceNode;
    }

    
    private static final LinearRing2D projectRingVerticesNormal(LinearRing2D sourceRing, LinearRing2D targetRing)
    {
        int nv = sourceRing.vertexNumber();

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
        ArrayList<Vector2D> edgeNormals = new ArrayList<Vector2D>(targetRing.vertexNumber());
        for (Polyline2D.Edge edge : targetRing.edges())
        {
            Vector2D tangent = new Vector2D(edge.source().position(), edge.target().position());
            edgeNormals.add(tangent.rotate90(-1));
        }
                    
        // compute projection points of current poly over next poly
        LinearRing2D nextPoly = new LinearRing2D(nv);
        for (int iv = 0; iv < nv; iv++)
        {
            Point2D point = sourceRing.vertexPosition(iv);
            double x = point.getX();
            double y = point.getY();
            
            double dist, minDist = Double.POSITIVE_INFINITY;
            Point2D proj = targetRing.vertexPosition(0);
            
            for (int iEdge = 0; iEdge < targetRing.vertexNumber(); iEdge++)
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
    
    private static final LinearRing2D interpolateRings(LinearRing2D ring0, LinearRing2D ring1, double t)
    {
        double t0 = t;
        double t1 = 1 - t0;
        
        int nv = ring0.vertexNumber();
        LinearRing2D interpPoly = new LinearRing2D(nv);
        for (int iv = 0; iv < nv; iv++)
        {
            Point2D p1 = ring0.vertexPosition(iv);
            Point2D p2 = ring1.vertexPosition(iv);
            
            double x = p1.getX() * t1 + p2.getX() * t0;
            double y = p1.getY() * t1 + p2.getY() * t0;
            interpPoly.addVertex(new Point2D(x, y));
        }

        return interpPoly;
    }
    
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

        // TODO: add optional info fields

        ps.printf(Locale.US, "%s = %s\n", "ElementDataFile", info.elementDataFile);
    }

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
    
    private Collection<Point2D> computeIntersectionsWithHorizontalLine(LinearRing2D ring, int yLine)
    {
        StraightLine2D line = new StraightLine2D(new Point2D(0, yLine), new Vector2D(1, 0));
        Collection<Point2D> points = ring.intersections(line);
        points = sortPointsByX(points);
        return points;
    }

    private ArrayList<Point2D> sortPointsByX(Collection<Point2D> points)
    {
        ArrayList<Point2D> res = new ArrayList<Point2D>(points.size());
        res.addAll(points);
        Collections.sort(res, new Comparator<Point2D>()
        {
            @Override
            public int compare(Point2D p0, Point2D p1)
            {
                // compare X first
                double dx = p0.getX() - p1.getX();
                if (dx < 0) return -1;
                if (dx > 0) return +1;
                // add y comparison
                double dy = p0.getY() - p1.getY();
                if (dy < 0) return -1;
                if (dy > 0) return +1;
                // same point
                return 0;
            }
        });
        return res;
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
    
    
    public ImageSerialSectionsNode getPolygonsNode()
    {
        GroupNode cropNode = getCrop3dNode(this.imageHandle);
        
        if (cropNode.hasChildWithName("polygons"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("polygons");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("polygons");
        cropNode.addNode(polyNode);
        return polyNode;
    }
    
    public ImageSerialSectionsNode getSmoothPolygonsNode()
    {
        GroupNode cropNode = getCrop3dNode(this.imageHandle);
        
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
        GroupNode cropNode = getCrop3dNode(this.imageHandle);
        
        if (cropNode.hasChildWithName("interp"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("interp");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("interp");
        cropNode.addNode(polyNode);
        return polyNode;
    }
}
