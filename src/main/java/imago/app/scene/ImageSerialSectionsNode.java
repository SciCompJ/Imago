/**
 * 
 */
package imago.app.scene;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * A group node for items on planar sections of a 3D image, that indexes
 * children using their slice index. This grouping node only accepts children
 * with class ImageSliceNode.
 * 
 * <pre>
 * {@code
    // create the node containing the different slices 
    ImageSerialSectionsNode groupNode = new ImageSerialSectionsNode("slices");
    
    // create a node for a specific slice 
    ImageSliceNode slice05 = new ImageSliceNode("slice_05", 05);
    slice05.addNode(new ShapeNode("Shape", new Point2D(30, 20)));
    // add the slice node to the grouping node
    groupNode.addSliceNode(slice05);
    // add another slice with a different index
    groupNode.addSliceNode(new ImageSliceNode("slice_10", 10));
    
    // retrieve the shape node for the slice with index 5
    ShapeNode shapeNode = (ShapeNode) cropNode.getSliceNode(5).children().iterator().next(); 
 }
 * </pre>
 * 
 * @see ImageSliceNode
 * @see GroupNode
 * 
 * @author dlegland
 *
 */
public class ImageSerialSectionsNode extends Node
{
    /**
     * The list of slice nodes, indexed by the slice index.
     */
    Map<Integer, ImageSliceNode> children = new TreeMap<Integer, ImageSliceNode>();

    /**
     * @param name the name of the items
     */
    public ImageSerialSectionsNode(String name)
    {
        super(name);
    }
    
    /**
     * Adds a slice to this grouping node, using the index from the specified
     * instance of ImageSliceNode.
     * 
     * @param node
     *            an instance of ImageSliceNode
     */
    public void addSliceNode(ImageSliceNode node)
    {
        int index = node.sliceIndex;
        if (children.containsKey(index))
        {
            throw new RuntimeException("Already contains a slice with index " + index);
        }
        children.put(index, node);
    }
    
    /**
     * Removes a slice from this grouping node, using the index from the specified
     * instance of ImageSliceNode.
     * 
     * @param node
     *            an instance of ImageSliceNode
     */
    public void removeSliceNode(ImageSliceNode node)
    {
        int index = node.sliceIndex;
        if (children.containsKey(index))
        {
            children.remove(index);
        }
    }
    
    /**
     * Removes a slice from this grouping node, using the specified index.
     * 
     * @param index
     *            the index of the slice to remove.
     */
    public void removeSliceNode(int index)
    {
        if (children.containsKey(index))
        {
            children.remove(index);
        }
    }
    
    /**
     * Retrieves a slice node from the specified index.
     * 
     * @param index
     *            the index of the slice
     * @return the instance of ImageSliceNode for the given index, or null if no
     *         such slice exists.
     */
    public ImageSliceNode getSliceNode(int index)
    {
        return children.get(index);
    }

    /**
     * Returns the list of indices for which a slice is attached.
     * 
     * @return the list of indices for which a slice is attached.
     */
    public Collection<Integer> getSliceIndices()
    {
        return children.keySet();
    }
    
    /**
     * Returns true if this grouping node has a slice indexed with the given
     * index.
     * 
     * @param index
     *            the index of a slice node.
     * @return true if this grouping node has a slice indexed with the given
     *         index.
     */
    public boolean hasSliceNode(int index)
    {
        return children.containsKey(index);
    }
    
    /**
     * Removes all the children of this node.
     */
    public void clear()
    {
        this.children.clear();
    }

    @Override
    public Iterable<ImageSliceNode> children()
    {
        return Collections.unmodifiableMap(children).values();
    }

    @Override
    public boolean isLeaf()
    {
        return children.isEmpty();
    }

    @Override
    public void printTree(PrintStream stream, int nIndents)
    {
        String str = "";
        for (int i = 0; i < nIndents; i++)
        {
            str = str + "  ";
        }
        String nameString = (name != null && !name.isEmpty()) ? name : "(no name)";
        stream.println(str + "[GroupNode] " + nameString);
        
        for (Node node : children.values())
        {
            node.printTree(stream, nIndents + 1);
        }
    }

}
