/**
 * 
 */
package imago.app.scene;

import java.io.PrintStream;

/**
 * A node that encapsulates 2D nodes.
 * 
 * @author dlegland
 *
 */
public class ImageSliceNode extends GroupNode
{
	// ===================================================================
	// Class members

	int sliceIndex = 0;

	
	// ===================================================================
	// Constructor

	/**
	 * @param sliceIndex the sliceIndex of this slice node.
	 */
	public ImageSliceNode(int sliceIndex)
	{
		this.sliceIndex = sliceIndex;
	}
	
	/**
	 * @param sliceIndex the sliceIndex of this slice node.
	 */
	public ImageSliceNode(String name, int sliceIndex)
	{
		super(name);
		this.sliceIndex = sliceIndex;
	}
	
	
	// ===================================================================
	// Specific methods
	
	 
	/**
	 * @return the sliceIndex
	 */
	public int getSliceIndex()
	{
		return sliceIndex;
	}

	/**
	 * @param sliceIndex the sliceIndex to set
	 */
	public void setSliceIndex(int sliceIndex)
	{
		this.sliceIndex = sliceIndex;
	}

	
	// ===================================================================
	// Methods overriding Node methods

	@Override
	public void printTree(PrintStream stream, int nIndents)
	{
		super.printTree(stream, nIndents);
	}

}
