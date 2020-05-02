/**
 * 
 */
package imago.app;

import java.io.File;
import java.util.Collection;

import net.sci.image.Image;

/**
 * The main manager of the application. Contains the workspace, and 
 * various global settings.
 * 
 * @author David Legland
 *
 */
public class ImagoApp 
{
	// =============================================================
	// class variables
    
    /**
     * Contains handles to the different entities manipulated by the
     * application: Image, Table, Geometry...
     */
    Workspace workspace = new Workspace();

    /**
     * The path of the last file that was open (global for the application).
     */
	File lastOpenFile = new File(".");
	
	
	// =============================================================
	// Constructor

	/**
	 * Empty constructor by default.
	 */
	public ImagoApp()
	{
	}

	
    // =============================================================
    // Management of handles
	
    /**
     * Creates a new handle for an image, adds it to the app, and return the
     * handle.
     * 
     * @param image
     *            the image instance
     * @return the handle to manage the image.
     */
    public ImageHandle createImageHandle(Image image)
    {
        ImageHandle handle = workspace.createImageHandle(image);
        return handle;
    }
	

	// =============================================================
	// Management of documents

	public void removeImageHandle(ImageHandle handle)
	{
	    workspace.removeHandle(handle.tag);
	}

	public int imageHandleNumber()
	{
		return workspace.getImageHandles().size();
	}

	public Collection<ImageHandle> getImageHandles()
	{
	    return workspace.getImageHandles();
	}

	/**
	 * Get the names of all open image documents.
	 * 
	 * @return the list of names of documents containing images.
	 */
	public Collection<String> getImageHandleNames()
	{
	    return ObjectHandle.getNames(workspace.getHandles(Image.class));
	}
	
	public ImageHandle getImageHandleFromName(String handleName)
	{
		for (ImageHandle handle : workspace.getImageHandles())
		{
			if (handle.getName().equals(handleName))
				return handle;
		}
		
		throw new IllegalArgumentException("App does not contain any image handle with name: " + handleName);
	}
	
	/**
	 * Display the list of document lists on the console (for debugging).
	 */
	public void printImageHandleList()
	{
		System.out.println("Image Handle list in app:");
		for (ImageHandle handle : workspace.getImageHandles())
		{
			System.out.println(handle.getName());
		}
	}


    /**
     * Creates a unique name for a document, given a base name (file name). If
     * application already contains a document with same base name, an index is
     * added to make the name unique.
     * 
     * @param baseName
     *            a base name for the document, for example the file name
     * @return a unique name based on the filename.
     */
    public String createImageHandleName(String baseName)
    {
        // avoid empty document names
        if (baseName == null || baseName.isEmpty())
        {
            baseName = "NoName";
        }
        
        // if no document with such name exist, just keep it
        if (!hasImageHandleWithName(baseName))
        {
            return baseName;
        }
        
        // otherwise, we first check if name contains an "index"
        // here: the number(s) at the end of the name, before the extension, separated by a dash
        
        // extract the sting containing extension (with final dot)
        String extString = "";
        int len = baseName.length();
        int dotIndex = baseName.lastIndexOf(".");
        // use extension with up to four characters
        if (dotIndex !=-1 && (len - dotIndex) < 6) 
        {
            extString = baseName.substring(dotIndex, len);
            baseName = baseName.substring(0, dotIndex);
        }
        
        // identifies the set of digits at the end of name 
        String digits = new String("0123456789");
        int lastIndex = baseName.length() - 1;
        String currentChar = ""; 
        while (lastIndex > 0)
        {
            currentChar = baseName.substring(lastIndex, lastIndex + 1);
            // iterates until a non digit character is found
            if (!digits.contains(currentChar))
            {
                break;
            }
            lastIndex--;
        }
        
        // check end of name matches an indexed image name pattern
        if (lastIndex < baseName.length() - 1 && currentChar.equals("-"))
        {
            baseName = baseName.substring(0, lastIndex);
        }
        
        // create names with the pattern until we found a non existing one
        int index = 1;
        while (true)
        {
            String newName = baseName + "-" + index + extString;
            if (!hasImageHandleWithName(newName))
            {
                return newName;
            }
            index++;
        }
    }
    
    private boolean hasImageHandleWithName(String name)
    {
        for (ImageHandle handle : workspace.getImageHandles())
        {
            if (handle.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }


    // =============================================================
    // Management of workspace

    /**
     * @return the workspace
     */
    public Workspace getWorkspace()
    {
        return workspace;
    }


    // =============================================================
    // Management of global settings

	public File getLastOpenFile()
	{
		return lastOpenFile;
	}


	public void setLastOpenFile(File lastOpenFile)
	{
		this.lastOpenFile = lastOpenFile;
	}
}
