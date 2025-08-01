import java.util.*;

/**
 * 容器类，表示装箱的3D容器空间
 */
public class Container {
    private double width, height, depth;
    private double maxWeight;
    private List<Box> boxes;
    private double currentWeight;
    
    public Container(double width, double height, double depth, double maxWeight) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.maxWeight = maxWeight;
        this.boxes = new ArrayList<>();
        this.currentWeight = 0;
    }
    
    public Container(Container other) {
        this.width = other.width;
        this.height = other.height;
        this.depth = other.depth;
        this.maxWeight = other.maxWeight;
        this.boxes = new ArrayList<>();
        this.currentWeight = 0;
        
        for (Box box : other.boxes) {
            Box newBox = new Box(box);
            this.boxes.add(newBox);
            this.currentWeight += newBox.getWeight();
        }
    }
    
    /**
     * 尝试在指定位置放置盒子
     */
    public boolean canPlaceBox(Box box, Point3D position, int orientation) {
        // 创建临时盒子用于测试
        Box tempBox = new Box(box);
        tempBox.setPosition(position);
        tempBox.setOrientation(orientation);
        tempBox.setPlaced(true);
        
        // 检查是否超出容器边界
        if (!isWithinBounds(tempBox)) {
            return false;
        }
        
        // 检查重量限制
        if (currentWeight + box.getWeight() > maxWeight) {
            return false;
        }
        
        // 检查与其他盒子的碰撞
        for (Box existingBox : boxes) {
            if (existingBox.isPlaced() && tempBox.overlaps(existingBox)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 在指定位置放置盒子
     */
    public boolean placeBox(Box box, Point3D position, int orientation) {
        if (canPlaceBox(box, position, orientation)) {
            box.setPosition(position);
            box.setOrientation(orientation);
            box.setPlaced(true);
            
            if (!boxes.contains(box)) {
                boxes.add(box);
                currentWeight += box.getWeight();
            }
            return true;
        }
        return false;
    }
    
    /**
     * 移除盒子
     */
    public boolean removeBox(Box box) {
        if (boxes.remove(box)) {
            currentWeight -= box.getWeight();
            box.setPlaced(false);
            return true;
        }
        return false;
    }
    
    /**
     * 检查盒子是否在容器边界内
     */
    private boolean isWithinBounds(Box box) {
        BoundingBox boxBB = box.getBoundingBox();
        return boxBB.min.x >= 0 && boxBB.min.y >= 0 && boxBB.min.z >= 0 &&
               boxBB.max.x <= width && boxBB.max.y <= height && boxBB.max.z <= depth;
    }
    
    /**
     * 获取所有可能的放置点
     */
    public List<Point3D> getPossiblePlacements() {
        Set<Point3D> placements = new HashSet<>();
        
        // 添加原点
        placements.add(new Point3D(0, 0, 0));
        
        // 为每个已放置的盒子添加可能的放置点
        for (Box box : boxes) {
            if (box.isPlaced()) {
                BoundingBox bb = box.getBoundingBox();
                
                // 在盒子的6个面上添加放置点
                placements.add(new Point3D(bb.max.x, bb.min.y, bb.min.z)); // 右面
                placements.add(new Point3D(bb.min.x, bb.max.y, bb.min.z)); // 上面
                placements.add(new Point3D(bb.min.x, bb.min.y, bb.max.z)); // 前面
            }
        }
        
        return new ArrayList<>(placements);
    }
    
    /**
     * 计算容器利用率
     */
    public double getUtilization() {
        double usedVolume = 0;
        for (Box box : boxes) {
            if (box.isPlaced()) {
                usedVolume += box.getVolume();
            }
        }
        return usedVolume / getVolume();
    }
    
    /**
     * 获取重量利用率
     */
    public double getWeightUtilization() {
        return currentWeight / maxWeight;
    }
    
    // Getters
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getDepth() { return depth; }
    public double getMaxWeight() { return maxWeight; }
    public double getCurrentWeight() { return currentWeight; }
    public double getVolume() { return width * height * depth; }
    public List<Box> getBoxes() { return new ArrayList<>(boxes); }
    public int getPlacedBoxCount() {
        return (int) boxes.stream().filter(Box::isPlaced).count();
    }
    
    @Override
    public String toString() {
        return String.format("Container[dims=(%.1f,%.1f,%.1f), boxes=%d, util=%.2f%%, weight=%.1f/%.1f]",
            width, height, depth, getPlacedBoxCount(), getUtilization() * 100, currentWeight, maxWeight);
    }
}