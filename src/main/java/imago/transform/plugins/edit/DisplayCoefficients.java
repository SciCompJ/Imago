/**
 * 
 */
package imago.transform.plugins.edit;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Locale;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImagoTextFrame;
import imago.transform.TransformHandle;
import imago.transform.TransformManager;
import imago.transform.plugins.TransformManagerPlugin;
import net.sci.geom.Transform;
import net.sci.geom.geom2d.AffineTransform2D;
import net.sci.geom.geom3d.AffineTransform3D;

/**
 * Displays the coefficient of the current affine transform, if selected.
 */
public class DisplayCoefficients implements TransformManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        TransformManager tm = (TransformManager) frame;
        
        TransformHandle handle = tm.getSelectedHandle();
        if (handle == null) return;
        
        ArrayList<String> textLines = new ArrayList<String>();
        textLines.add("Coefficients of transform: " + handle.getName() + " (id=" + handle.getTag() + ")");
        
        Transform transfo = handle.getTransform();
        String numberFormat = "%10.5f";
        switch (transfo)
        {
            case AffineTransform2D aff2d -> addAffineTransform2DMatrix(textLines, aff2d, numberFormat);
            case AffineTransform3D aff3d -> addAffineTransform3DMatrix(textLines, aff3d, numberFormat);
            default -> {
                ImagoGui.showErrorDialog(frame, "Requires an affine transform as input", "Wrong Transform Type");
                return;
            }
        }
        
        String title = String.format("%s - Transform Info", handle.getName());
        ImagoTextFrame newFrame = new ImagoTextFrame(frame, title, textLines);
        newFrame.getWidget().pack();
        newFrame.getWidget().setSize(new Dimension(600, 400));
        newFrame.setVisible(true);
    }
    
    private static final void addAffineTransform2DMatrix(ArrayList<String> textLines, AffineTransform2D aff,
            String numberFormat)
    {
        double[][] mat = aff.affineMatrix();
        String pattern = String.format("[%s, %s, %s]", numberFormat, numberFormat, numberFormat);
        for (int i = 0; i < 2; i++)
        {
            textLines.add(String.format(Locale.ENGLISH, pattern, mat[i][0], mat[i][1], mat[i][2]));
        }
        textLines.add(String.format(Locale.ENGLISH, pattern, 0.0, 0.0, 1.0));
    }
    
    private static final void addAffineTransform3DMatrix(ArrayList<String> textLines, AffineTransform3D aff,
            String numberFormat)
    {
        double[][] mat = aff.affineMatrix();
        String pattern = String.format("[%s, %s, %s, %s]", numberFormat, numberFormat, numberFormat, numberFormat);
        for (int i = 0; i < 3; i++)
        {
            textLines.add(String.format(Locale.ENGLISH, pattern, mat[i][0], mat[i][1], mat[i][2], mat[i][3]));
        }
        textLines.add(String.format(Locale.ENGLISH, pattern, 0.0, 0.0, 0.0, 1.0));
    }
}
