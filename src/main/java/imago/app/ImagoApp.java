/**
 * 
 */
package imago.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.sci.image.Image;

/**
 * The main manager of the application. Contains a set of documents, and 
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
	 * The list of documents managed by the application.
	 */
	ArrayList<ImagoDoc> docs;

	File lastOpenFile = new File(".");
	
	
	// =============================================================
	// Constructor

	public ImagoApp()
	{
		this.docs = new ArrayList<ImagoDoc>();
	}

	
	// =============================================================
	// Management of documents

	/**
	 * Creates a new document from an image, and adds it to this app.
	 */
	public ImagoDoc addNewDocument(Image image)
	{
		// add the image document to GUI
		ImagoDoc doc = new ImagoDoc(image);
		this.addDocument(doc);
		return doc;
	}

	public void addDocument(ImagoDoc doc)
	{
		// ensure document has unique name
		String baseName = doc.getName();
		if (hasDocumentWithName(baseName))
		{
			String docName = createDocumentName(baseName);
			doc.name = docName;
		}
		
		// add document to the list
		this.docs.add(doc);
	}

	public void removeDocument(ImagoDoc doc)
	{
		this.docs.remove(doc);
	}

	public int documentNumber()
	{
		return this.docs.size();
	}

	public Collection<ImagoDoc> getDocuments()
	{
		return Collections.unmodifiableList(this.docs);
	}

	/**
	 * Get the names of all open image documents.
	 * 
	 * @return the list of names of documents containing images.
	 */
	public Collection<String> getImageDocumentNames()
	{
		ArrayList<String> nameList = new ArrayList<String>(this.docs.size()); 
		for (ImagoDoc doc : this.docs)
		{
			if (doc.getImage() != null)
			{
				nameList.add(doc.getName());
			}
		}
		return nameList;
	}
	
	public ImagoDoc getDocumentFromName(String docName)
	{
		for (ImagoDoc doc : this.docs)
		{
			if (doc.getName().equals(docName))
				return doc;
		}
		
		throw new IllegalArgumentException("App does not contain any document with name: " + docName);
	}
	
	/**
	 * Display the list of document lists on the console (for debugging).
	 */
	public void printDocumentList()
	{
		System.out.println("Document list in app:");
		for (ImagoDoc doc : this.docs)
		{
			System.out.println(doc.getName());
		}
	}
	
	public File getLastOpenFile()
	{
		return lastOpenFile;
	}


	public void setLastOpenFile(File lastOpenFile)
	{
		this.lastOpenFile = lastOpenFile;
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
	public String createDocumentName(String baseName)
	{
		// avoid empty document names
		if (baseName == null || baseName.isEmpty())
		{
			baseName = "NoName";
		}
		
		// if no document with such name exist, just keep it
		if (!hasDocumentWithName(baseName))
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
	        if (!hasDocumentWithName(newName))
	        {
	        	return newName;
	        }
	        index++;
	    }
	}
	
	private boolean hasDocumentWithName(String name)
	{
		for (ImagoDoc doc : this.docs)
		{
			if (doc.getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}
}
