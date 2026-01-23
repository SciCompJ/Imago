/**
 * 
 */
package imago.image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import imago.app.ImagoApp;
import imago.app.ObjectHandle;
import imago.app.Workspace;
import imago.app.scene.GroupNode;
import imago.app.scene.Node;
import imago.app.shape.Shape;
import imago.util.imagej.ImagejRoi;
import imago.util.imagej.ImagejRoiDecoder;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.io.tiff.ImagejMetadata;


/**
 * An handle that contains an image, and eventually some annotations. Also
 * provides a management of changes to image.
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
     * @param parentHandle
     *            a parent handle, used to initialize handles fields.
     * @return the handle to manage the image.
     */
    public static final ImageHandle create(ImagoApp app, Image image, ImageHandle parentHandle)
    {
        Workspace workspace = app.getWorkspace();
        String tag = workspace.findNextFreeTag("img");
        String name = workspace.createHandleName(image.getName());
        ImageHandle handle = new ImageHandle(image, name, tag);
        if (parentHandle != null)
        {
            handle.copyDisplaySettings(parentHandle);
        }
        workspace.addHandle(handle);
        return handle;
    }

    public static final Collection<ImageHandle> getAll(ImagoApp app)
    {
        return app.getWorkspace().getHandles().stream()
                .filter(handle -> handle instanceof ImageHandle)
                .map(handle -> (ImageHandle) handle)
                .toList();
    }
    
    /**
     * Get the name of all image handles.
     * 
     * @return the list of names of handles containing images.
     */
    public static final Collection<String> getAllNames(ImagoApp app)
    {
        return app.getWorkspace().getHandles().stream()
                .filter(handle -> handle instanceof ImageHandle)
                .map(handle -> handle.getName())
                .toList();
    }
    
    /**
     * Retrieves all the ImageHandle instances within the specified app that
     * contain an image whose data array is a parent (from the
     * {@code Array.View} interface) of the specified array.
     * 
     * @param app
     *            the instance of ImagoApp containing the handles
     * @param array
     *            the array to consider.
     * @return the list of ImageHandle instances that refer to a parent of the
     *         array. If the array is not a view, the result is empty.
     */
    public static final Collection<ImageHandle> getAllParents(ImagoApp app, Array<?> array)
    {
        if (!(array instanceof Array.View<?>))
        {
            return new ArrayList<ImageHandle>();
        }

        Collection<Array<?>> parentArrays = getAllParents((Array.View<?>) array);

        return ImageHandle.getAll(app).stream()
                .filter(h -> parentArrays.contains(h.getImage().getData()))
                .collect(Collectors.toList());
    }
    
    private static final Collection<Array<?>> getAllParents(Array.View<?> array)
    {
        HashSet<Array<?>> res = new HashSet<>();
        for (Array<?> parent : array.parentArrays())
        {
            res.add(parent);
            if (parent instanceof Array.View)
            {
                res.addAll(getAllParents((Array.View<?>) parent));
            }
        }
        return res;
    }
    
    public static final ImageHandle findFromName(ImagoApp app, String handleName)
    {
        return app.getWorkspace().getHandles().stream()
                .filter(handle -> handle instanceof ImageHandle)
                .filter(handle -> handle.getName().equals(handleName))
                .map(handle -> (ImageHandle) handle)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Workspace does not contain any image handle with name: " + handleName));
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
		importImageMetaData();
		
		// setup default display
		if (image.getDimension() > 2) 
		{
			this.currentSliceIndex = (int) Math.round(image.getSize(2) / 2);
		}
	}
	
    /**
     * Updates the viewer to display specific meta data (e.g., ImageJ ROI)
     * stored within the file.
     * 
     * @param imageHandle
     *            the ImageHandle 
     */
    private void importImageMetaData()
    {
        if (image.metadata.containsKey("imagej"))
        {
            // populate shapes with imagej overlay
            ImagejMetadata metadata = (ImagejMetadata) image.metadata.get("imagej");
            if (metadata.overlayData != null)
            {
                // convert Image overlays as Shape instances within the ImageHandle
                int nOverlay = metadata.overlayData.length;
                for (int i = 0; i < nOverlay; i++)
                {
                    ImagejRoi roi = ImagejRoiDecoder.decode(metadata.overlayData[i]);
                    addShape(roi.asShape());
                }
            }
//            
//            if (metadata.roiData != null)
//            {
//                // Convert the current ROI as a Selection for the viewer
//                ImagejRoi roi = ImagejRoiDecoder.decode(metadata.roiData);
//                Shape shape = roi.asShape();
//                this.imageViewer.setSelection(shape.getGeometry());
//                this.imageViewer.refreshDisplay();
//            }
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
        // if the two images are 3D and have the same size, use same index of current slice
        if (this.image.getDimension() > 2 && doc.image.getDimension() > 2)
        {
            int size2 = this.image.getSize(2);
            if (doc.image.getSize(2) == size2)
            {
                this.currentSliceIndex = Math.min(Math.max(doc.getCurrentSliceIndex(), 0), size2);
            }
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
