import javax.vecmath.Point3d;
import java.lang.Math;

// http://bcr.musc.edu/manuals/MODELLER6v0/manual/node180.html

public class dihedraltest {

    public static Point3d crossProduct(Point3d u, Point3d v) {
        double x,y,z;

        x = u.y*v.z - u.z*v.y;
        y = u.z*v.x - u.x*v.z;
        z = u.x*v.y - u.y*v.x;

        return(new Point3d(x,y,z));
    }
    public static double dotProduct(Point3d u, Point3d v) {
        return(u.x*v.x + u.y*v.y + u.z*v.z);
    }
    public static Point3d getVector(Point3d u, Point3d v) {
        return(new Point3d(u.x-v.x, u.y-v.y, u.z-v.z));
    }
    public static double vectorNorm(Point3d p) {
        return(Math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z));
    }

    public static double dihedral(Point3d pi, Point3d pj, Point3d pk, Point3d pl) {
        double rad2deg = 180.0 / Math.PI;
        
        Point3d rij = getVector(pi,pj);
        Point3d rkj = getVector(pk,pj);
        Point3d rkl = getVector(pk,pl);

        Point3d rij_cross_rkj = crossProduct(rij,rkj);
        Point3d rkj_cross_rkl = crossProduct(rkj,rkl);

        double norm_ijkj = vectorNorm(rij_cross_rkj);
        double norm_kjkl = vectorNorm(rkj_cross_rkl);
        
        // get the sign first
        int dsign = 1;
        Point3d tmp1 = crossProduct(rij_cross_rkj, rkj_cross_rkl);
        double tmp2 = dotProduct(rkj, tmp1);
        if (tmp2 > 0) dsign = 1;
        else dsign = -1;

        double angle = dsign * Math.acos(dotProduct(rij_cross_rkj, rkj_cross_rkl)/ (norm_ijkj * norm_kjkl));
        return(angle * rad2deg);
    }

    public static void main(String[] args) {
        Point3d h1 = new Point3d(0.9131995, 0.0, -1.1130662);
        Point3d c2 = new Point3d(0.0,0.0,-0.5306605);
        Point3d o3 = new Point3d(0.0,0.0,0.6762620);
        Point3d h4 = new Point3d(-0.9131996,0.0,-1.1130662);

        double angle = dihedral(h1, c2,o3,h4);

        System.out.println("angle = "+angle);
    }
}
