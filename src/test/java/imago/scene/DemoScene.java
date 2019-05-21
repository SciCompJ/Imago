/**
 * 
 */
package imago.scene;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.DefaultPolygon2D;

/**
 * @author dlegland
 *
 */
public class DemoScene
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // Create a 2D scene
        Scene scene = new Scene(2);
        scene.setAxis(0, new SceneAxis("X", -150, 150));
        scene.setAxis(1, new SceneAxis("Y", -100, 100));
        
        // create some polygons for populating the scene
        DefaultPolygon2D poly1 = new DefaultPolygon2D();
        poly1.addVertex(new Point2D(10, 10));
        poly1.addVertex(new Point2D(50, 10));
        poly1.addVertex(new Point2D(50, 30));
        poly1.addVertex(new Point2D(10, 30));
        scene.addItem(new Geometry2DItem("poly1", poly1));
        DefaultPolygon2D poly2 = new DefaultPolygon2D();
        poly2.addVertex(new Point2D(-10, 10));
        poly2.addVertex(new Point2D(-10, 50));
        poly2.addVertex(new Point2D(-30, 50));
        poly2.addVertex(new Point2D(-30, 10));
        scene.addItem(new Geometry2DItem("poly2", poly2));

        JFrame frame = new JFrame("Scene Demo");
        frame.setPreferredSize(new Dimension(400, 300));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Setup layout
        Container panel = frame.getContentPane();
        
        JPanel scenePanel = new SceneDisplay(scene);
        
        JScrollPane scrollPane = new JScrollPane(scenePanel);
        panel.add(scrollPane, BorderLayout.CENTER);
//        panel.add(scrollPane, BorderLayout.CENTER);
        
        frame.pack();

        // show !
        frame.setVisible(true);
    }

}
