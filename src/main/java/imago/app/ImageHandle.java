/**
 * 
 */
package imago.app;

import imago.app.shape.ImagoShape;

import java.util.ArrayList;
import java.util.Collection;

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
	Collection<ImagoShape> shapes = new ArrayList<ImagoShape>();
	
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
	
	public void copyDisplaySettings(ImageHandle doc) 
	{
		if (this.image.getDimension() > 2) 
		{
			this.currentSliceIndex = Math.min(Math.max(doc.getCurrentSliceIndex(), 0), this.image.getSize(2));
		}	
	}
	
    public Object getObject()
    {
        return this.image;
    }

}
