/**
 * 
 */
package imago.app;

import java.util.ArrayList;
import java.util.Collection;

import imago.app.shape.ImagoShape;
import net.sci.image.Image;


/**
 * An Imago document, that contains an image, and eventually some annotations.
 * 
 * @author David Legland
 *
 */
public class ImagoDoc 
{

	// =============================================================
	// Class variables
	
	/**
	 * The name of the document (should be unique among all open documents).
	 * Usually initialized with image name.
	 */
	String name;
	
	/**
	 * The image displayed by this document.
	 */
	Image image;
	
	Collection<ImagoShape> shapes = new ArrayList<ImagoShape>();
	
	/**
	 * For 3D images, the index of the slice visible in document.
	 */
	int currentSliceIndex = 0;
	
	
	// =============================================================
	// Constructors
	
	/**
	 * Initializes a new document for the given image
	 * 
	 * @param image
	 *            the image that will be contained in the document
	 */
	public ImagoDoc(Image image) 
	{
		this(image.getName(), image);
	}
	
	/**
	 * Initializes a new document for the given image, keeping some settings from
	 * the parent document.
	 * 
	 * @param image
	 *            the image contained in the document
	 * @param parent
	 *            the parent document providing settings
	 */
	public ImagoDoc(Image image, ImagoDoc parent) 
	{
		this(image.getName(), image);
		
		copyDisplaySettings(parent);
	}
	
	public ImagoDoc(String name, Image image) 
	{
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
	
	public String getName() 
	{
		return this.name;
	}
	
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

	public Collection<ImagoShape> getShapes()
	{
	    return this.shapes;
	}
	
    public void addShape(ImagoShape shape)
    {
        this.shapes.add(shape);
    }
    
    public void clearShapes()
    {
        this.shapes.clear();
    }
    
	
	    

	// =============================================================
	// General methods
	
	public void copyDisplaySettings(ImagoDoc doc) 
	{
		if (this.image.getDimension() > 2) 
		{
			this.currentSliceIndex = Math.min(Math.max(doc.getCurrentSliceIndex(), 0), this.image.getSize(2));
		}	
	}
	
}
