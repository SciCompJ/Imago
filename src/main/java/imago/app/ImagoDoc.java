/**
 * 
 */
package imago.app;

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
	 * The name of the document (should be unique among all opened documents).
	 * Usually initialized with image name.
	 */
	String name;
	
	/**
	 * The image displayed by this document.
	 */
	Image image;
	
	int currentSliceIndex = 0;
	
	
	// =============================================================
	// Constructors
	
	public ImagoDoc(Image image) 
	{
		this(image.getName(), image);
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

	public int getCurrentSliceIndex() {
		return currentSliceIndex;
	}

	public void setCurrentSliceIndex(int currentSliceIndex) {
		this.currentSliceIndex = currentSliceIndex;
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
