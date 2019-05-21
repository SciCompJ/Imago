/**
 * 
 */
package imago.scene;

import java.awt.Graphics2D;

/**
 * An item within a scene.
 * 
 * @author dlegland
 *
 */
public abstract class SceneItem
{
    String name;
    
    boolean visible;
    
    public SceneItem(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }
    
    public void getName(String newName)
    {
        this.name = newName;
    }
    
    public boolean isVisible()
    {
        return this.visible;
    }
    
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }
    
    public abstract void draw(Graphics2D g);
}
