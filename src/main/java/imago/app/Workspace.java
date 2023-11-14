/**
 * 
 */
package imago.app;

import java.util.Collection;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Contains a set of <code>ObjectHandle</code> instances, indexed by their tag.
 * 
 * @see ObjectHandle
 * 
 * @author dlegland
 */
public class Workspace
{
    // =============================================================
    // class variables

    /**
     * The list of handles managed by the application, indexed by their tag.
     */
    private TreeMap<String, ObjectHandle> handles = new TreeMap<String, ObjectHandle>();
    
    
    // =============================================================
    // Query handles from name or class
    
    public Collection<ObjectHandle> getHandles(Predicate<ObjectHandle> filter)
    {
        return handles.values().stream()
                .filter(filter)
                .toList();
    }
    
    public Collection<String> getHandleNames(Predicate<ObjectHandle> filter)
    {
        return handles.values().stream()
                .filter(filter)
                .map(h -> h.getName())
                .toList();
    }
    
    /**
     * Returns the first handle whose name matches the specified name.
     * 
     * @param name
     *            the name of the handle.
     * @return the first handle whose name matches the specified name.
     */
    public ObjectHandle findHandleWithName(String name)
    {
        for (ObjectHandle handle : handles.values())
        {
            if (handle.getName().equals(name))
            {
                return handle;
            }
        }

        throw new RuntimeException("Workspace does not contain any item with name: " + name);
    }
    
    public boolean hasHandleWithName(String name)
    {
        for (ObjectHandle handle : handles.values())
        {
            if (handle.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
    

    // =============================================================
    // get handles from the workspace

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
    
    public Collection<ObjectHandle> getHandles()
    {
        return this.handles.values();
    }
    
    public boolean hasHandle(String tag)
    {
        return this.handles.containsKey(tag);
    }


    // =============================================================
    // Management of tags

    public Collection<String> getTags()
    {
        return this.handles.keySet();
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
    public String findNextFreeTag(String baseTag)
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
        } while (handles.containsKey(tag));
        
        return tag;
    }
    
    
    // =============================================================
    // update list of handles in the workspace

    /**
     * Adds the specified handle to the workspace. If another handle with the
     * same tag was stored in the workspace, it is replaced.
     * 
     * @param handle
     *            the handle to add.
     */
    public void addHandle(ObjectHandle handle)
    {
        String tag = handle.tag;
        this.handles.put(tag, handle);
    }

    /**
     * Removes the specified handle from the workspace, or throws an exception.
     * 
     * @param handle
     *            the handle to remove
     */
    public void removeHandle(ObjectHandle handle)
    {
        removeHandle(handle.tag);
    }
    
    /**
     * Removes the handle identified by its tag from the workspace, or throws an
     * exception if there is no handle with the specified tag.
     * 
     * @param tag
     *            the tag of the handle to remove
     */
    public void removeHandle(String tag)
    {
        if (this.handles.containsKey(tag))
            this.handles.remove(tag);
        else
            throw new RuntimeException("Workspace does not contain any item with tag: " + tag);
    }

    /**
     * Removes all handles within workspace.
     */
    public void clear()
    {
        this.handles.clear();
    }

}
