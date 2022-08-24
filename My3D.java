
public class My3D {
    public double x;
    public double y;
    public double z;

    public My3D(double xx, double yy, double zz) {
         x = xx;
         y = yy;
         z = zz;
    }

    public void DebugPrint(String mystr) {
         System.out.println(mystr + x + "," + y + "," + z);
    }
}

