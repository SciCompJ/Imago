/**
 * 
 */
package imago.gui.frames;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.gui.ImagoFrame;
import net.sci.geom.Geometry;
import net.sci.geom.polygon2d.Polyline2D;

/**
 * Displays the content of a scene graph (given as its root node) using a JTree
 * widget. 
 * 
 * @author dlegland
 *
 */
public class SceneGraphDisplayFrame extends ImagoFrame
{
    JTree tree;
    
    public SceneGraphDisplayFrame(ImagoFrame parent, String name, Node rootNode)
    {
        super(parent, name);            
        
        DefaultMutableTreeNode top = createNodeTree(rootNode);
        
        // Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(200, 400));
        JFrame jFrame = this.getWidget();
        jFrame.add(treeView);
        jFrame.pack();
    }
    
    private DefaultMutableTreeNode createNodeTree(Node node)
    {
        DefaultMutableTreeNode treeNode = createNode(node);
        for (Node child : node.children())
        {
            treeNode.add(createNodeTree(child));
        }
        return treeNode;
    }
    
    private DefaultMutableTreeNode createNode(Node node)
    {
        DefaultMutableTreeNode treeNode = createNode(node.getName());
        treeNode.add(createNode("Type: " + node.getClass().getName()));
        
        // process special nodes
        if (node instanceof ShapeNode)
        {
            ShapeNode shapeNode = (ShapeNode) node;
            treeNode.add(createGeometryNode(shapeNode.getGeometry()));
        }
        
        return treeNode;
    }
    
    private DefaultMutableTreeNode createGeometryNode(Geometry geom)
    {
        DefaultMutableTreeNode treeNode = createNode("Geometry");
        treeNode.add(createNode("Type: " + geom.getClass().getName()));
        if (geom instanceof Polyline2D)
        {
            treeNode.add(createNode("VertexCount: " + ((Polyline2D) geom).vertexCount()));
        }
        return treeNode;
    }
    
    /**
     * Creates a simple node containing the given text as content.
     * 
     * @param title
     *            the display string of the node.
     * @return a simple node containing the given text as content.
     */
    private DefaultMutableTreeNode createNode(String title)
    {
        return new DefaultMutableTreeNode(title);
    }
    
}
