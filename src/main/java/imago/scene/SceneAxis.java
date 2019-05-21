/**
 * 
 */
package imago.scene;

/**
 * @author dlegland
 *
 */
public class SceneAxis
{
    // =============================================================
    // Methods

    String label = "";
    
    double mini = 0;
    
    double maxi = 0;

    boolean reverse = false;
    

    // =============================================================
    // Constructors

    /**
     * Creates a new axis with only the label. 
     */
    public SceneAxis(String label)
    {
        this(label, 0, 1, false);
    }

    /**
     * Creates a new axis that specifies the label and the bounds. 
     */
    public SceneAxis(String label, double mini, double maxi)
    {
        this(label, mini, maxi, false);
    }

    /**
     * Constructor with all parameters
     */
    public SceneAxis(String label, double mini, double maxi, boolean reverse)
    {
        this.label = label;
        this.mini = mini;
        this.maxi = maxi;
        this.reverse = reverse;
    }

    // =============================================================
    // Methods

    public double getExtent()
    {
        return this.maxi - this.mini;
    }
}
