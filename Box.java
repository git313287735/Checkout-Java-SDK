/**
 * 3D盒子类，表示要装入容器的物品
 */
public class Box {
    private int id;
    private double width, height, depth;  // 原始尺寸
    private Point3D position;             // 当前位置
    private int orientation;              // 旋转方向 (0-5, 表示6种可能的旋转)
    private double weight;                // 重量
    private boolean placed;               // 是否已放置
    
    // 6种可能的旋转方向对应的尺寸
    private static final int[][] ORIENTATIONS = {
        {0, 1, 2}, // w, h, d
        {0, 2, 1}, // w, d, h  
        {1, 0, 2}, // h, w, d
        {1, 2, 0}, // h, d, w
        {2, 0, 1}, // d, w, h
        {2, 1, 0}  // d, h, w
    };
    
    public Box(int id, double width, double height, double depth, double weight) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.weight = weight;
        this.position = new Point3D();
        this.orientation = 0;
        this.placed = false;
    }
    
    public Box(Box other) {
        this.id = other.id;
        this.width = other.width;
        this.height = other.height;
        this.depth = other.depth;
        this.weight = other.weight;
        this.position = new Point3D(other.position);
        this.orientation = other.orientation;
        this.placed = other.placed;
    }
    
    // 获取当前旋转方向下的尺寸
    public double getCurrentWidth() {
        double[] dims = {width, height, depth};
        return dims[ORIENTATIONS[orientation][0]];
    }
    
    public double getCurrentHeight() {
        double[] dims = {width, height, depth};
        return dims[ORIENTATIONS[orientation][1]];
    }
    
    public double getCurrentDepth() {
        double[] dims = {width, height, depth};
        return dims[ORIENTATIONS[orientation][2]];
    }
    
    // 获取盒子的体积
    public double getVolume() {
        return width * height * depth;
    }
    
    // 获取盒子在当前位置和旋转下的边界框
    public BoundingBox getBoundingBox() {
        Point3D min = new Point3D(position);
        Point3D max = new Point3D(
            position.x + getCurrentWidth(),
            position.y + getCurrentHeight(), 
            position.z + getCurrentDepth()
        );
        return new BoundingBox(min, max);
    }
    
    // 检查是否与另一个盒子重叠
    public boolean overlaps(Box other) {
        if (!this.placed || !other.placed) return false;
        
        BoundingBox thisBB = this.getBoundingBox();
        BoundingBox otherBB = other.getBoundingBox();
        
        return thisBB.intersects(otherBB);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getDepth() { return depth; }
    public double getWeight() { return weight; }
    public Point3D getPosition() { return position; }
    public void setPosition(Point3D position) { this.position = new Point3D(position); }
    public int getOrientation() { return orientation; }
    public void setOrientation(int orientation) { 
        this.orientation = Math.max(0, Math.min(5, orientation)); 
    }
    public boolean isPlaced() { return placed; }
    public void setPlaced(boolean placed) { this.placed = placed; }
    
    @Override
    public String toString() {
        return String.format("Box[id=%d, dims=(%.1f,%.1f,%.1f), pos=%s, orient=%d, placed=%s]",
            id, width, height, depth, position, orientation, placed);
    }
}

/**
 * 边界框类，用于碰撞检测
 */
class BoundingBox {
    public Point3D min, max;
    
    public BoundingBox(Point3D min, Point3D max) {
        this.min = new Point3D(min);
        this.max = new Point3D(max);
    }
    
    public boolean intersects(BoundingBox other) {
        return !(max.x <= other.min.x || min.x >= other.max.x ||
                 max.y <= other.min.y || min.y >= other.max.y ||
                 max.z <= other.min.z || min.z >= other.max.z);
    }
    
    public boolean contains(Point3D point) {
        return point.x >= min.x && point.x <= max.x &&
               point.y >= min.y && point.y <= max.y &&
               point.z >= min.z && point.z <= max.z;
    }
    
    public double getVolume() {
        return (max.x - min.x) * (max.y - min.y) * (max.z - min.z);
    }
}