/**
 * 
 */
package imago.transform;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.app.ObjectHandle;
import imago.app.Workspace;
import net.sci.geom.Transform;


/**
 * A handle to a geometric transform.
 * 
 * @author dlegland
 *
 */
public class TransformHandle extends ObjectHandle
{
    // =============================================================
    // Static utility methods
    
    /**
     * Creates a new handle for a transform, adds it to the workspace, and
     * returns the created handle.
     * 
     * @param transfo
     *            the transform.
     * @return the handle to manage the transform.
     */
    public static final TransformHandle create(ImagoApp app, Transform transfo)
    {
        String defaultName = createTag(transfo);
        return create(app, transfo, defaultName);
    }

    /**
     * Creates a new handle for a transform, adds it to the workspace, and
     * returns the created handle.
     * 
     * @param transfo
     *            the transform.
     * @param parent
     *            a parent handle, used to initialize handles fields.
     * @param name
     *            the name of the transform
     * @return the handle to manage the transform.
     */
    public static final TransformHandle create(ImagoApp app, Transform transfo, String name)
    {
        String baseTag = createTag(transfo);
        
        Workspace workspace = app.getWorkspace();
        String tag = workspace.findNextFreeTag(baseTag);

        TransformHandle handle = new TransformHandle(transfo, name, tag);
        workspace.addHandle(handle);
        return handle;
    }

    /**
     * Returns all the transform handles contained in the application.
     * 
     * @param app
     *            the application to explore
     * @return the list of all transform handles within the application workspace
     */
    public static final Collection<TransformHandle> getAll(ImagoApp app)
    {
        return app.getWorkspace().getHandles().stream()
                .filter(h -> h instanceof TransformHandle)
                .map(h -> (TransformHandle) h)
                .toList();
    }
    
    /**
     * Get the name of all transform handles.
     * 
     * @return the list of names of handles containing transforms.
     */
    public static final Collection<String> getAllNames(ImagoApp app)
    {
        return app.getWorkspace().getHandles().stream()
                .filter(h -> h instanceof TransformHandle)
                .map(h -> h.getName())
                .toList();
    }
    
    public static final TransformHandle findFromName(ImagoApp app, String handleName)
    {
        return (TransformHandle) app.getWorkspace().getHandles().stream()
                .filter(h -> h instanceof TransformHandle)
                .map(h -> (TransformHandle) h)
                .filter(h -> h.getName().equals(handleName))
                .findFirst().orElseThrow();
    }

    /**
     * Generates a default tag for a transform, based on its class. Currently
     * returns the same tag for all transform classes, but more specialized tags
     * could be returned in the future.
     * 
     * @param transfo
     *            the transform
     * @return a string that can be used as tag base for the geometry handle
     */
    public static final String createTag(Transform transfo)
    {
        return "transfo";
    }
    
    
    // =============================================================
    // Class members
    
    Transform transfo;
    
    
    // =============================================================
    // Constructor
    
    public TransformHandle(Transform transfo, String name, String tag)
    {
        super(tag);
        this.transfo = transfo;
        this.name = name;
    }
    
    
    // =============================================================
    // Data access methods
    
    public Transform getTransform()
    {
        return transfo;
    }

    public Transform getObject()
    {
        return this.transfo;
    }
    
    public String getItemClassName()
    {
        return "Transform";
    }
    
}
