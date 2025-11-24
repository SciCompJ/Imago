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
     * Creates a new handle for a geometry, adds it to the workspace, and
     * returns the created handle.
     * 
     * @param geom
     *            the geometry.
     * @return the handle to manage the geometry.
     */
    public static final TransformHandle create(ImagoApp app, Transform transfo)
    {
        String defaultName = createTag(transfo);
        return create(app, transfo, defaultName);
    }

    /**
     * Creates a new handle for a geometry, adds it to the workspace, and
     * returns the created handle.
     * 
     * @param geom
     *            the geometry.
     * @param parent
     *            a parent handle, used to initialize handles fields.
     * @param name
     *            the name of the transform
     * @return the handle to manage the geometry.
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
     * Returns all the geometry handles contained in the application.
     * 
     * @param app
     *            the application to explore
     * @return the list of all table handles within the application workspace
     */
    public static final Collection<TransformHandle> getAll(ImagoApp app)
    {
        return app.getWorkspace().getHandles().stream()
                .filter(h -> h instanceof TransformHandle)
                .map(h -> (TransformHandle) h)
                .toList();
    }
    
    /**
     * Get the name of all geometry handles.
     * 
     * @return the list of names of handles containing geometries.
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
     * Generates a default tag for a geometry based on geometry class. For
     * example, Point geometries will generate tag "pnt", polygon or polyline
     * geometries will generate tag "poly", and so on. Default tag is d"geom".
     * 
     * @param geom
     *            the geometry
     * @return a string that can be used as tag base for the geometry handle
     */
    public static final String createTag(Transform geom)
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
}
