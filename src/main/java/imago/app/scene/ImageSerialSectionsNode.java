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
 * children using their slice index.
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
    Map<Integer, ImageSliceNode> children =  new TreeMap<Integer, ImageSliceNode>();

    /**
     * @param name the name of the items
     */
    public ImageSerialSectionsNode(String name)
    {
        super(name);
    }
    
    public void addSliceNode(ImageSliceNode node)
    {
        int index = node.sliceIndex;
        if (children.containsKey(index))
        {
            throw new RuntimeException("Already contains a slice with index " + index);
        }
        children.put(index, node);
    }
    
    public void removeSliceNode(ImageSliceNode node)
    {
        int index = node.sliceIndex;
        if (children.containsKey(index))
        {
            children.remove(index);
        }
    }
    
    public void removeSliceNode(int index)
    {
        if (children.containsKey(index))
        {
            children.remove(index);
        }
    }
    
    public ImageSliceNode getSliceNode(int index)
    {
        return children.get(index);
    }

    public Collection<Integer> getSliceIndices()
    {
        return children.keySet();
    }
    
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
