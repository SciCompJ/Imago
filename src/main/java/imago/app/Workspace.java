/**
 * 
 */
package imago.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import net.sci.geom.Geometry;
import net.sci.image.Image;
import net.sci.table.Table;

/**
 * @author dlegland
 *
 */
public class Workspace
{
    // =============================================================
    // class variables

    /**
     * The list of handles managed by the application.
     */
    TreeMap<String, ObjectHandle> handles = new TreeMap<String, ObjectHandle>();
    

    // =============================================================
    // Creation of new handles

    /**
     * Creates a new handle to the input argument, creates the appropriate tag,
     * and adds it to the list of handles in the workspace.
     * 
     * @param object
     *            the object to add.
     * @param name
     *            the name associated to this object.
     * @param baseTag
     *            the string pattern used to build handle tag.
     * @return the handle created for this object.
     */
    public ObjectHandle createHandle(Object object, String name, String baseTag)
    {
        if (name == null)
        {
            name = "NoName";
        }
        
        ObjectHandle handle;
        String tag = findNextFreeTag(baseTag);
        
        if (object instanceof Image)
        {
            handle = new ImageHandle((Image) object, name, tag);
        }
        else if (object instanceof Table)
        {
            handle = new TableHandle((Table) object, name, tag);
        }
        else if (object instanceof Geometry)
        {
            handle = new GeometryHandle((Geometry) object, name, tag);
        }
        else if (object instanceof String)
        {
            handle = new StringHandle((String) object, name, tag);
        }
        else
        {
            handle  = new GenericHandle(object, name, tag);
        }
        
        this.handles.put(tag, handle);
        
        return handle;
    }
    
    public TableHandle createTableHandle(Table table)
    {
        String tag = findNextFreeTag("tab");
        TableHandle handle = new TableHandle(table, table.getName(), tag);
        this.handles.put(handle.tag, handle);
        return handle;
    }
    
    /**
     * Creates a new handle for an image, adds it to the workspace, and return the
     * handle.
     * 
     * @param image
     *            the image instance
     * @return the handle to manage the image.
     */
    public ImageHandle createImageHandle(Image image)
    {
        String tag = findNextFreeTag("img");
        ImageHandle handle = new ImageHandle(image, image.getName(), tag);
        this.handles.put(handle.tag, handle);
        return handle;
    }

    
    // =============================================================
    // Query handles
    
    /**
     * Returns all the instances of ObjectHandle stored in this workspace whose
     * object match the specified class.
     * 
     * @param objectClass
     *            the class to match.
     * @return the list of handles that contains an object of the specified
     *         class.
     */
    public Collection<ObjectHandle> getHandles(Class<?> objectClass)
    {
        ArrayList<ObjectHandle> res = new ArrayList<ObjectHandle>();
        for (ObjectHandle handle : handles.values())
        {
            if (objectClass.isInstance(handle.getObject()))
            {
                res.add(handle);
            }
        }
        return res;
    }
    
    /**
     * Returns a list of names corresponding to the instances of ObjectHandle
     * stored in this workspace whose object match the specified class.
     * 
     * @param objectClass
     *            the class to match.
     * @return the list of names of the handles that contain an object of the
     *         specified class.
     */
    public Collection<String> getHandleNames(Class<?> objectClass)
    {
        return ObjectHandle.getNames(getHandles(objectClass));
    }
    
    public Collection<ImageHandle> getImageHandles()
    {
        ArrayList<ImageHandle> res = new ArrayList<ImageHandle>();
        for (ObjectHandle handle : handles.values())
        {
            if (handle instanceof ImageHandle)
            {
                res.add((ImageHandle) handle);
            }
        }
        return res;
    }
    
    public Collection<TableHandle> getTableHandles()
    {
        ArrayList<TableHandle> res = new ArrayList<TableHandle>();
        for (ObjectHandle handle : handles.values())
        {
            if (handle instanceof TableHandle)
            {
                res.add((TableHandle) handle);
            }
        }
        return res;
    }
    

    // =============================================================
    // Management of handles

    /**
     * Returns the item associated with the specified tag.
     * 
     * @param tag
     *            the tag.
     * @return the tag associated to the item.
     */
    public ObjectHandle getHandle(String tag)
    {
        if (this.handles.containsKey(tag))
            return this.handles.get(tag);
        else
            throw new RuntimeException("Workspace does not contain any item with tag: " + tag);
    }
    
    public void removeHandle(String tag)
    {
        if (this.handles.containsKey(tag))
            this.handles.remove(tag);
        else
            throw new RuntimeException("Workspace does not contain any item with tag: " + tag);
    }
    
    public boolean hasHandle(String tag)
    {
        return this.handles.containsKey(tag);
    }

    public Collection<String> getTags()
    {
        return this.handles.keySet();
    }

    public Collection<ObjectHandle> getHandles()
    {
        return this.handles.values();
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
    private String findNextFreeTag(String baseTag)
    {
        // avoid empty tags
        if (baseTag == null || baseTag.isEmpty())
        {
            baseTag = "obj";
        }
        
        // create names with the pattern until we found a non existing one
        String tag;
        int index = 0;
        do
        {
            tag = String.format(baseTag + "%02d", index++);
        } while (hasItemWithTag(tag));
        
        return tag;
    }
    
    
    private boolean hasItemWithTag(String tag)
    {
        for (String t : this.handles.keySet())
        {
            if (t.equals(tag))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes all handles within workspace.
     */
    public void clear()
    {
        this.handles.clear();
    }

}
