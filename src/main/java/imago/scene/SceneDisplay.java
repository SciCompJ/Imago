/**
 * 
 */
package imago.scene;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

/**
 * @author dlegland
 *
 */
public class SceneDisplay extends JPanel
{
    // ===================================================================
    // Static memebers

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // ===================================================================
    // Class variables

    Scene scene;
    
    double[] axisScalings = new double[] { 2.0, 2.0 };
    
    // ===================================================================
    // Constructor

    public SceneDisplay(Scene scene)
    {
        this.scene = scene;
    }
    

    // ===================================================================
    // Dimensioning

    public Dimension getPreferredSize()
    {
        return getDisplaySize();
    }
    
    public Dimension getDisplaySize() 
    {
        double xExtent = scene.getExtent(0);
        double yExtent = scene.getExtent(1);

        int width = (int) Math.ceil(xExtent * axisScalings[0]);
        int height = (int) Math.ceil(yExtent * axisScalings[1]);
        return new Dimension(width, height);
    }
    

    // ===================================================================
    // Painting methods

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        
        setupGraphics(g2);
        
        drawBackground(g2);
        drawItems(g2);
    }
    
    private void setupGraphics(Graphics2D g2)
    {
        double xmin = scene.getAxis(0).mini;
        double ymin = scene.getAxis(1).mini;
        double sx = this.axisScalings[0];
        double sy = this.axisScalings[1];

        g2.translate(-xmin * sx, -ymin * sy);
        g2.scale(sx, -sy);
    }
    
    private void drawBackground(Graphics2D g2)
    {
        double xmin = scene.getAxis(0).mini;
        double xmax = scene.getAxis(0).maxi;
        double ymin = scene.getAxis(1).mini;
        double ymax = scene.getAxis(1).maxi;
        
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin));
        
        if (scene.displayOptions.axisLinesisible)
        {
            g2.setColor(Color.BLACK);
            g2.draw(new Line2D.Double(xmin, 0, xmax, 0));
            g2.draw(new Line2D.Double(0, ymin, 0, ymax));
        }
    }

    private void drawItems(Graphics2D g2)
    {
        for (SceneItem item : scene.itemList)
        {
            g2.setColor(Color.BLUE);
           
            item.draw(g2);
        }
    }
}
