/**
 * 
 */
package imago.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.table.TableHandle;
import net.sci.array.color.ColorMap;
import net.sci.geom.Geometry;
import net.sci.image.Image;
import net.sci.table.Table;

/**
 * The main manager of the application. Contains the workspace, and 
 * various global settings.
 * 
 * @author David Legland
 *
 */
public class ImagoApp 
{
	// =============================================================
	// class variables
    
    /**
     * Contains handles to the different entities manipulated by the
     * application: Image, Table, Geometry...
     */
    Workspace workspace = new Workspace();

    /**
     * The list of colormap handles managed by the application, in the order of addition.
     */
    private ArrayList<ColorMapHandle> colorMapHandles = new ArrayList<ColorMapHandle>();
    /**
     * The colormap handles, indexed by their tag, for faster retrieval
     */
    private HashMap<String, ColorMapHandle> colorMapHandleMap = new HashMap<String, ColorMapHandle>();
    
    
	// =============================================================
	// Constructor

	/**
	 * Empty constructor by default.
	 */
	public ImagoApp()
	{
	}
	
	
    // =============================================================
    // Global management of handles

    /**
     * Creates a new handle for the input item, by creating the appropriate name
     * and tag, and adds the handle to the list of handles in the workspace.
     * <p>
     * 
     * This methods tries to create the appropriate sub-class of
     * {@code ObjectHandle}, depending on the class of the input item. For
     * example if {@code item} is an instance of the {@code Image} class, an
     * instance of {@code ImageHandle} will be created.
     * 
     * @param item
     *            the item to add.
     * @param name
     *            the name associated to this object.
     * @param baseTag
     *            the string pattern used to build handle tag.
     * @return the handle created for this object.
     */
    public ObjectHandle createHandle(Object item, String name, String baseTag)
    {
        // setup indexation variables
        name = workspace.createHandleName(name != null ? name : "NoName");
        String tag = workspace.findNextFreeTag(baseTag);
        
        // create handle based on class of item
        ObjectHandle handle = switch(item)
        {
            case Image image -> new ImageHandle(image, name, tag);
            case Table table -> new TableHandle(table, name, tag);
            case Geometry geom -> new GeometryHandle(geom, name, tag);
            case String string -> new StringHandle(string, name, tag);
            default -> new GenericHandle(item, name, tag);
        };
        
        workspace.addHandle(handle);
        
        return handle;
    }
    
    public void removeHandle(ObjectHandle handle)
    {
        workspace.removeHandle(handle.tag);
    }
    
    
    // =============================================================
    // Management of workspace
    
    /**
     * @return the workspace
     */
    public Workspace getWorkspace()
    {
        return workspace;
    }

    /**
     * Creates a unique name for a handle, given a base name (typically a file name). If
     * application already contains a document with same base name, an index is
     * added to make the name unique.
     * 
     * @param name
     *            a base name for the handle, for example the file name.
     * @return a unique name based on proposed name.
     */
    public String createHandleName(String name)
    {
        return workspace.createHandleName(name);
    }
    
    
    // =============================================================
    // Management of color maps
    
    
    public void addColorMap(String name, ColorMap colorMap)
    {
        String tag = findNextFreeColorMapTag();
        ColorMapHandle handle = new ColorMapHandle(tag, colorMap, name);
        this.colorMapHandles.add(handle);
        this.colorMapHandleMap.put(handle.tag, handle);
    }
    
    /**
     * Returns the colormap corresponding to the specified name, or {@code null}
     * if no such colormap exist.
     * 
     * @param name
     *            the name of the colormap
     * @eturn the corresponding colormap, or {@code null} if no such colormap
     *        exist.
     */
    public ColorMap getColorMap(String name)
    {
        return this.colorMapHandles.stream()
                .filter(handle -> handle.getName().equals(name))
                .findFirst()
                .map(ColorMapHandle::getObject).orElse(null);
    }
    
    /**
     * Returns an unmodifiable view on the list of colormap handles.
     * 
     * @return an unmodifiable view on the list of colormap handles.
     */
    public List<ColorMapHandle> colorMapHandles()
    {
        return Collections.unmodifiableList(this.colorMapHandles);
    }
    
    /**
     * Creates a unique tag for an item, given a base name (file name). If
     * application already contains an item with same base tag, an index is
     * added to make the tag unique.
     * 
     * @param baseTag
     *            a base tag for the item, for example based on its class.
     * @return a unique tag.
     */
    private String findNextFreeColorMapTag()
    {
        String baseTag = "cmap";
        
        // create names with the pattern until we found a non existing one
        String tag;
        int index = 0;
        do
        {
            tag = String.format(baseTag + "%02d", index++);
        } while (colorMapHandleMap.containsKey(tag));
        
        return tag;
    }
}
