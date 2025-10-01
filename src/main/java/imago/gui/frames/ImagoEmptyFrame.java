/**
 * 
 */
package imago.gui.frames;


import java.awt.Dimension;

import imago.gui.GuiBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;


/**
 * @author David Legland
 *
 */
public class ImagoEmptyFrame extends ImagoFrame
{
    // ===================================================================
    // Static class variables

    // ===================================================================
    // Constructor

    public ImagoEmptyFrame(ImagoGui gui)
    {
        super(gui, "Imago");

        GuiBuilder builder = new GuiBuilder(this);
        builder.createMenuBar();

        initializePosition();
    }

    /**
     * Set up frame position on top-left of screen, with a position depending on
     * screen size.
     */
    private void initializePosition()
    {
        this.jFrame.setMinimumSize(new Dimension(300, 0));
        this.jFrame.pack();

        // set up frame position in the upper left corner
        int posX = 300;
        int posY = 50;
        this.jFrame.setLocation(posX, posY);
    }
}
