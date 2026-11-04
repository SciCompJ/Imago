/**
 * 
 */
package imago.view3d;

import net.sci.geom.geom3d.AffineTransform3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.geom3d.Vector3D;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.SimplePolygonalMesh3D;

/**
 * A cylinder in 3D space, defined by two extremity points and a radius.
 */
public class Cylinder3D
{
    Point3D p1;
    Point3D p2;
    double r;
    
    public Cylinder3D(Point3D p1, Point3D p2, double r)
    {
        this.p1 = p1;
        this.p2 = p2;
        this.r = r;
    }
    
    public Mesh3D createMesh(int nSubs)
    {
        Vector3D vec = Vector3D.of(p1, p2);
        
        double hxy     = Math.hypot(vec.x(), vec.y());
        double rho     = Math.hypot(hxy, vec.z());
        double theta   = Math.PI / 2 - Math.atan2(vec.z(), hxy);
        double phi     = Math.atan2(vec.y(), vec.x());
        
//        System.out.println(String.format("target = (%5.2f, %5.2f, %5.2f)", p2.x(), p2.y(), p2.z()));
//        System.out.println("Theta = " + Math.toDegrees(theta));
//        System.out.println("Phi = " + Math.toDegrees(phi));

        // co-latitude
        AffineTransform3D rot2 = AffineTransform3D.createRotationOy(theta);

        // longitude
        AffineTransform3D rot3 = AffineTransform3D.createRotationOz(phi);

        // shift center
        AffineTransform3D tr = AffineTransform3D.createTranslation(p1);

        // create local-to-global transform by concatenating transforms
        AffineTransform3D transfo = AffineTransform3D.compose(tr, rot3, rot2);
        
        SimplePolygonalMesh3D mesh = new SimplePolygonalMesh3D();
        
        
        // compute vertices for a vertical cylinder
        int nCirc = 12;
        Mesh3D.Vertex[][] vertexGrid = new Mesh3D.Vertex[nCirc][nSubs];
        for (int iSub = 0; iSub < nSubs; iSub++)
        {
            double t = iSub * 2 * Math.PI / nSubs; 
            double x = this.r * Math.cos(t);
            double y = this.r * Math.sin(t);
            for (int iCirc = 0; iCirc < nCirc; iCirc++)
            {
                double z = ((double) iCirc) * rho / (nCirc - 1);
                Point3D p = new Point3D(x, y, z);
                Point3D pt = p.transform(transfo);
                vertexGrid[iCirc][iSub] = mesh.addVertex(pt);
            }
        }
        
        for (int iSub = 0; iSub < nSubs; iSub++)
        {
            int iSub2 = (iSub + 1) % nSubs; 
            for (int iCirc = 0; iCirc < nCirc - 1; iCirc++)
            {
                int iCirc2 = iCirc + 1;
                Mesh3D.Vertex v00 = vertexGrid[iCirc][iSub];
                Mesh3D.Vertex v01 = vertexGrid[iCirc2][iSub];
                Mesh3D.Vertex v10 = vertexGrid[iCirc][iSub2];
                Mesh3D.Vertex v11 = vertexGrid[iCirc2][iSub2];
                mesh.addFace(v00, v10, v11, v01);
            }
        }
     
        return mesh;
    }
}
