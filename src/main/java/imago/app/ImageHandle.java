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

}
