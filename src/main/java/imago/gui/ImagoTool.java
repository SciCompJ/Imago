/**
 * 
 */
package imago.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * @author David Legland
 *
 */
public abstract class ImagoTool implements MouseListener, MouseMotionListener
{
	// ===================================================================
	// Class variables
	
	protected ImagoDocViewer viewer;

	protected String name;
	
	
	// ===================================================================
	// Constructor

	protected ImagoTool(ImagoDocViewer viewer, String name) 
	{
		this.viewer = viewer;
		this.name = name;
	}
	
	
	// ===================================================================
	// Public methods

	public String getName() 
	{
		return this.name;
	}
	
	public abstract void select() ;

	public abstract void deselect() ;
	

	// ===================================================================
	// Mouse events management

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent arg0)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent arg0)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent arg0)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent arg0)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) 
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent arg0) 
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent arg0)
	{
	}

}
