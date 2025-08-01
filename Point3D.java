/**
 * 3D点类，表示三维空间中的一个点
 */
public class Point3D {
    public double x, y, z;
    
    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Point3D() {
        this(0, 0, 0);
    }
    
    public Point3D(Point3D other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }
    
    public Point3D add(Point3D other) {
        return new Point3D(x + other.x, y + other.y, z + other.z);
    }
    
    public Point3D subtract(Point3D other) {
        return new Point3D(x - other.x, y - other.y, z - other.z);
    }
    
    public double distance(Point3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point3D point3D = (Point3D) obj;
        return Double.compare(point3D.x, x) == 0 &&
               Double.compare(point3D.y, y) == 0 &&
               Double.compare(point3D.z, z) == 0;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y, z);
    }
}