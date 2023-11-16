/**
 * 
 */
package imago.app;

import java.util.ArrayList;
import java.util.Collection;

import imago.app.scene.GroupNode;
import imago.app.scene.Node;
import imago.app.shape.Shape;
import net.sci.image.Image;


/**
 * An Imago document, that contains an image, and eventually some annotations.
 * 
 * @author David Legland
 *
 */
public class ImageHandle extends ObjectHandle
{
    // =============================================================
    // Static utility methods
    
    /**
     * Creates a new handle for an image, adds it to the workspace, and returns
     * the created handle.
     * 
     * @param image
     *            the image instance.
     * @return the handle to manage the image.
     */
    public static final ImageHandle create(ImagoApp app, Image image)
    {
        return create(app, image, null);
    }

    /**
     * Creates a new handle for an image, adds it to the workspace, and returns
     * the created handle.
     * 
     * @param image
     *            the image instance.
     * @param parent
     *            a parent handle, used to initialize handles fields.
     * @return the handle to manage the image.
     */
    public static final ImageHandle create(ImagoApp app, Image image, ImageHandle parent)
    {
        Workspace workspace = app.getWorkspace();
        String tag = workspace.findNextFreeTag("img");
        String name = app.createHandleName(image.getName());
        ImageHandle handle = new ImageHandle(image, name, tag);
        if (parent != null)
        {
            handle.copyDisplaySettings(parent);
        }
        workspace.addHandle(handle);
        return handle;
    }

    public static final Collection<ImageHandle> getAll(ImagoApp app)
    {
        ArrayList<ImageHandle> res = new ArrayList<ImageHandle>();
        for (ObjectHandle handle : app.getWorkspace().getHandles())
        {
            if (handle instanceof ImageHandle)
            {
                res.add((ImageHandle) handle);
            }
        }
        return res;
    }
    
    /**
     * Get the name of all image handles.
     * 
     * @return the list of names of handles containing images.
     */
    public static final Collection<String> getAllNames(ImagoApp app)
    {
        ArrayList<String> res = new ArrayList<String>();
        for (ObjectHandle handle : app.getWorkspace().getHandles())
        {
            if (handle instanceof ImageHandle)
            {
                res.add(handle.getName());
            }
        }
        return res;
    }
    
    public static final ImageHandle findFromName(ImagoApp app, String handleName)
    {
        for (ObjectHandle handle : app.getWorkspace().getHandles())
        {
            if (handle instanceof ImageHandle)
            {
                if (handle.getName().equals(handleName))
                    return (ImageHandle) handle;
            }
        }
        
        throw new IllegalArgumentException("App does not contain any image handle with name: " + handleName);
    }


	// =============================================================
	// Class variables
	
	/**
	 * The image displayed by this document.
	 */
	Image image;

	/**
	 * A set of shapes. 
	 */
	Collection<Shape> shapes = new ArrayList<Shape>();
	
	GroupNode rootNode = new GroupNode("root");
	
	/**
	 * For 3D images, the index of the slice visible in document.
	 */
	int currentSliceIndex = 0;
	
	
	ArrayList<Listener> listeners = new ArrayList<Listener>();
	
	
	// =============================================================
	// Constructors
	
	public ImageHandle(Image image, String name, String tag) 
	{
        super(tag);

        // initialize name
		this.name = name;
		if (name == null || name.length() == 0)
		{
			this.name = "NoName";
		}

		// setup image data
		this.image = image;
		if (image.getDimension() > 2) 
		{
			this.currentSliceIndex = (int) Math.round(image.getSize(2) / 2);
		}
	}
	

	// =============================================================
	// Accessors
	
	public Image getImage() 
	{
		return this.image;
	}

	public int getCurrentSliceIndex() 
	{
		return currentSliceIndex;
	}

	public void setCurrentSliceIndex(int currentSliceIndex) 
	{
		this.currentSliceIndex = currentSliceIndex;
	}


    // =============================================================
    // Management of shapes

	public Collection<Shape> getShapes()
	{
	    return this.shapes;
	}
	
    public void addShape(Shape shape)
    {
        this.shapes.add(shape);
    }
    
    public void clearShapes()
    {
        this.shapes.clear();
    }
    
	
    // =============================================================
    // Management of scene graph

    public Node getRootNode()
    {
    	return this.rootNode;
    }
	
    
    // =============================================================
    // Management of ImageHandleListeners
    
    public void addImageHandleListener(Listener lst)
    {
        this.listeners.add(lst);
    }
    
    public void removeImageHandleListener(Listener lst)
    {
        this.listeners.remove(lst);
    }
    
    public void notifyImageHandleChange()
    {
        Event evt = new Event(this);
        for (Listener lst : listeners)
        {
            lst.imageHandleModified(evt);
        }
    }
    
    public void notifyImageHandleChange(int code)
    {
        Event evt = new Event(this, code);
        for (Listener lst : listeners)
        {
            lst.imageHandleModified(evt);
        }
    }
    
    
	// =============================================================
	// General methods
	
	public void copyDisplaySettings(ImageHandle doc) 
	{
		if (this.image.getDimension() > 2) 
		{
			this.currentSliceIndex = Math.min(Math.max(doc.getCurrentSliceIndex(), 0), this.image.getSize(2));
		}	
	}
	
    public Image getObject()
    {
        return this.image;
    }

    public interface Listener
    {
        public void imageHandleModified(Event evt);
    }
    
    public static class Event
    {
        /**
         * The event mask for selecting item creation.
         */
        public static final int CREATE_MASK = 1;
        
        /**
         * The event mask for selecting item removal.
         */
        public static final int REMOVE_MASK = 2;
        
        /**
         * The event mask for selecting item change.
         */
        public static final int CHANGE_MASK = 4;
        
        /**
         * The event mask for selecting image update.
         */
        public static final int IMAGE_MASK = 8;
        
        /**
         * The event mask for selecting Look-Up Table(LUT) update.
         */
        public static final int DISPLAY_RANGE_MASK = 16;
        
        /**
         * The event mask for selecting Look-Up Table(LUT) update.
         */
        public static final int LUT_MASK = 32;
        
        /**
         * The event mask for selecting shape update.
         */
        public static final int SHAPES_MASK = 256;
        
        /**
         * The source of the event.
         */
        ImageHandle source;
        
        /**
         * The int code of the event, summarizing the change.
         */
        int code; 
        
        /**
         * Build a new event.
         * 
         * @param source
         *            the ImageHandle that generated the event.
         */
        public Event(ImageHandle source)
        {
            this(source, 0);
        }
        
        public Event(ImageHandle source, int code)
        {
            this.source = source;
            this.code = code;
        }
        
        public ImageHandle getSource()
        {
            return source;
        }
        
        /**
         * Returns the code of The event/
         * 
         * @return the code of the event.
         */
        public int getCode()
        {
            return code;
        }
    }
}
