/**
 * 
 */
package imago.scene;

import java.util.ArrayList;


/**
 * A bounded region of the plane or the 3D space that contains other entities
 * (images, geometric primitives, annotations...).
 * 
 * @author dlegland
 *
 */
public class Scene
{
    // =============================================================
    // class members

    /**
     * Meta-data for each of the scene axes.
     */
    SceneAxis[] axes;

    /**
     * Some display options
     */
    SceneDisplayOptions displayOptions = new SceneDisplayOptions();

    /**
     * A list of item, that can be geometries, images...
     */
    ArrayList<SceneItem> itemList = new ArrayList<SceneItem>();
    
    
    // =============================================================
    // Constructors

    /**
     * Creates a new empty scene.
     */
    public Scene()
    {
        this(2);
    }

    /**
     * Creates a new empty scene of the specified dimension.
     */
    public Scene(int nDims)
    {
        initAxes(nDims);
    }
    
    private void initAxes(int nDims)
    {
        this.axes = new SceneAxis[nDims];
        if (nDims > 0) this.axes[0] = new SceneAxis("X");
        if (nDims > 1) this.axes[1] = new SceneAxis("Y");
        if (nDims > 2) this.axes[2] = new SceneAxis("Z");
        for (int d = 3; d < nDims; d++)
        {
            this.axes[d] = new SceneAxis("D" + d);
        }
    }
    
    
    // =============================================================
    // Methods

    public int dimensionality()
    {
        return axes.length;
    }
    
    /**
     * @param dim
     *            the axis
     * @return the physical extent along the specified axis
     */
    public double getExtent(int dim)
    {
        return this.axes[dim].getExtent();
    }

    // =============================================================
    // Items management
    
    public int itemNumber()
    {
        return this.itemList.size();
    }

    public void addItem(SceneItem item)
    {
        this.itemList.add(item);
    }

    /**
     * Returns the first item with the specified name, or null if there is no
     * item with the specified name in the scene.
     * 
     * @param itemName
     *            the name of the item
     * @return the first item with the specified name
     */
    public SceneItem findItem(String itemName)
    {
        for (SceneItem item : itemList)
        {
            if (itemName.equals(item.getName()))
                return item;
        }
        return null;
    }
    
    // =============================================================
    // Axes management

    public void setAxis(int dim, SceneAxis axis)
    {
        this.axes[dim] = axis;
    }
    
    public SceneAxis getAxis(int dim)
    {
        return this.axes[dim];
    }
    
}
