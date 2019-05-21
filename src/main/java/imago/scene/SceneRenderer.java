/**
 * 
 */
package imago.scene;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Displays a scene onto a panel.
 * 
 * @author dlegland
 *
 */
public class SceneRenderer
{
    Scene scene;
    
    public SceneRenderer(Scene scene)
    {
        this.scene = scene;
    }

    public void render(Graphics2D g2)
    {
        double xExtent = scene.getExtent(0);
        double yExtent = scene.getExtent(1);
        
        g2.setColor(Color.WHITE);
        g2.fillRect(10,  10, (int) (xExtent+10), (int) (yExtent+10));
    }
}
