import java.util.*;

/**
 * 优化的3D容器类，使用网格索引和剪枝策略
 * 将空间复杂度从O(n³)优化到O(n²log n)
 */
public class OptimizedContainer {
    private final double width, height, depth;
    private final double maxWeight;
    private final GridIndex gridIndex;
    private final List<Box> placedBoxes;
    private final PriorityQueue<Point3D> candidatePositions;
    private double currentWeight;
    
    // 剪枝参数
    private final double minBoxSize;
    private final int maxCandidates;
    
    public OptimizedContainer(double width, double height, double depth, double maxWeight) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.maxWeight = maxWeight;
        this.placedBoxes = new ArrayList<>();
        this.currentWeight = 0;
        
        // 估算最小盒子尺寸用于网格优化
        this.minBoxSize = Math.min(Math.min(width, height), depth) / 50.0;
        this.maxCandidates = 1000; // 限制候选位置数量
        
        this.gridIndex = new GridIndex(width, height, depth, minBoxSize);
        
        // 优先队列，按照位置优先级排序（底部优先，后部优先，左侧优先）
        this.candidatePositions = new PriorityQueue<>((p1, p2) -> {
            int cmp = Double.compare(p1.z, p2.z); // Z轴优先（底部）
            if (cmp != 0) return cmp;
            cmp = Double.compare(p1.y, p2.y);     // Y轴次之（后部）
            if (cmp != 0) return cmp;
            return Double.compare(p1.x, p2.x);    // X轴最后（左侧）
        });
        
        // 初始化候选位置
        candidatePositions.add(new Point3D(0, 0, 0));
    }
    
    /**
     * 优化的装箱方法，使用网格索引和剪枝
     * 复杂度：O(n²log n)
     */
    public boolean placeBoxOptimized(Box box) {
        if (currentWeight + box.getWeight() > maxWeight) {
            return false;
        }
        
        // 尝试所有6种旋转方向
        for (int orientation = 0; orientation < 6; orientation++) {
            Box tempBox = new Box(box);
            tempBox.setOrientation(orientation);
            
            double boxWidth = tempBox.getCurrentWidth();
            double boxHeight = tempBox.getCurrentHeight();
            double boxDepth = tempBox.getCurrentDepth();
            
            // 使用剪枝优化的位置搜索
            if (tryPlaceWithPruning(tempBox, boxWidth, boxHeight, boxDepth)) {
                // 成功放置
                placedBoxes.add(tempBox);
                currentWeight += tempBox.getWeight();
                gridIndex.placeBox(tempBox);
                updateCandidatePositions(tempBox);
                
                // 复制状态到原始盒子
                box.setPosition(tempBox.getPosition());
                box.setOrientation(tempBox.getOrientation());
                box.setPlaced(true);
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 使用剪枝策略尝试放置盒子
     */
    private boolean tryPlaceWithPruning(Box box, double boxWidth, double boxHeight, double boxDepth) {
        // 获取排序后的候选位置
        List<Point3D> candidates = getSortedCandidates(boxWidth, boxHeight, boxDepth);
        
        // 限制搜索范围以提高性能
        int searchLimit = Math.min(candidates.size(), maxCandidates);
        
        for (int i = 0; i < searchLimit; i++) {
            Point3D pos = candidates.get(i);
            
            // 早期剪枝：检查基本约束
            if (!isValidPosition(pos, boxWidth, boxHeight, boxDepth)) {
                continue;
            }
            
            // 使用网格索引进行快速碰撞检测
            if (gridIndex.canPlace(pos.x, pos.y, pos.z, boxWidth, boxHeight, boxDepth)) {
                box.setPosition(pos);
                box.setPlaced(true);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取排序后的候选位置
     */
    private List<Point3D> getSortedCandidates(double boxWidth, double boxHeight, double boxDepth) {
        Set<Point3D> allCandidates = new TreeSet<>((p1, p2) -> {
            // 综合评分排序
            double score1 = calculatePositionScore(p1, boxWidth, boxHeight, boxDepth);
            double score2 = calculatePositionScore(p2, boxWidth, boxHeight, boxDepth);
            int cmp = Double.compare(score1, score2);
            if (cmp != 0) return cmp;
            
            // 如果评分相同，按坐标排序
            cmp = Double.compare(p1.z, p2.z);
            if (cmp != 0) return cmp;
            cmp = Double.compare(p1.y, p2.y);
            if (cmp != 0) return cmp;
            return Double.compare(p1.x, p2.x);
        });
        
        // 从网格索引获取候选位置
        allCandidates.addAll(gridIndex.getPossiblePlacements(boxWidth, boxHeight, boxDepth));
        
        // 添加基于已放置盒子的候选位置
        for (Box placedBox : placedBoxes) {
            addBoxBasedCandidates(allCandidates, placedBox, boxWidth, boxHeight, boxDepth);
        }
        
        return new ArrayList<>(allCandidates);
    }
    
    /**
     * 计算位置评分（越小越好）
     */
    private double calculatePositionScore(Point3D pos, double boxWidth, double boxHeight, double boxDepth) {
        // 优先考虑：底部、后部、左侧、紧凑性
        double score = 0;
        
        // 高度惩罚（越高越不好）
        score += pos.z * 1.0;
        
        // 深度惩罚（越靠前越不好）
        score += pos.y * 0.8;
        
        // 宽度惩罚（越靠右越不好）
        score += pos.x * 0.6;
        
        // 空间利用率奖励
        double spaceUtilization = calculateSpaceUtilization(pos, boxWidth, boxHeight, boxDepth);
        score -= spaceUtilization * 10.0;
        
        return score;
    }
    
    /**
     * 计算空间利用率
     */
    private double calculateSpaceUtilization(Point3D pos, double boxWidth, double boxHeight, double boxDepth) {
        // 简化的空间利用率计算
        double totalVolume = width * height * depth;
        double usedVolume = 0;
        
        for (Box box : placedBoxes) {
            usedVolume += box.getVolume();
        }
        
        usedVolume += boxWidth * boxHeight * boxDepth;
        return usedVolume / totalVolume;
    }
    
    /**
     * 基于已放置盒子添加候选位置
     */
    private void addBoxBasedCandidates(Set<Point3D> candidates, Box placedBox, 
                                     double boxWidth, double boxHeight, double boxDepth) {
        Point3D pos = placedBox.getPosition();
        double pw = placedBox.getCurrentWidth();
        double ph = placedBox.getCurrentHeight();
        double pd = placedBox.getCurrentDepth();
        
        // 在已放置盒子的6个面上添加候选位置
        Point3D[] newCandidates = {
            new Point3D(pos.x + pw, pos.y, pos.z),      // 右侧
            new Point3D(pos.x, pos.y + ph, pos.z),      // 上方
            new Point3D(pos.x, pos.y, pos.z + pd),      // 前方
            new Point3D(pos.x - boxWidth, pos.y, pos.z), // 左侧
            new Point3D(pos.x, pos.y - boxHeight, pos.z), // 下方
            new Point3D(pos.x, pos.y, pos.z - boxDepth)  // 后方
        };
        
        for (Point3D candidate : newCandidates) {
            if (isValidPosition(candidate, boxWidth, boxHeight, boxDepth)) {
                candidates.add(candidate);
            }
        }
    }
    
    /**
     * 检查位置是否有效
     */
    private boolean isValidPosition(Point3D pos, double boxWidth, double boxHeight, double boxDepth) {
        return pos.x >= 0 && pos.y >= 0 && pos.z >= 0 &&
               pos.x + boxWidth <= width &&
               pos.y + boxHeight <= height &&
               pos.z + boxDepth <= depth;
    }
    
    /**
     * 更新候选位置
     */
    private void updateCandidatePositions(Box newBox) {
        Point3D pos = newBox.getPosition();
        double bw = newBox.getCurrentWidth();
        double bh = newBox.getCurrentHeight();
        double bd = newBox.getCurrentDepth();
        
        // 添加新的候选位置
        candidatePositions.add(new Point3D(pos.x + bw, pos.y, pos.z));
        candidatePositions.add(new Point3D(pos.x, pos.y + bh, pos.z));
        candidatePositions.add(new Point3D(pos.x, pos.y, pos.z + bd));
    }
    
    /**
     * 传统的暴力搜索方法（用于对比）
     * 复杂度：O(n³)
     */
    public boolean placeBoxBruteForce(Box box) {
        if (currentWeight + box.getWeight() > maxWeight) {
            return false;
        }
        
        for (int orientation = 0; orientation < 6; orientation++) {
            Box tempBox = new Box(box);
            tempBox.setOrientation(orientation);
            
            double boxWidth = tempBox.getCurrentWidth();
            double boxHeight = tempBox.getCurrentHeight();
            double boxDepth = tempBox.getCurrentDepth();
            
            // 暴力搜索所有可能位置
            for (double z = 0; z <= depth - boxDepth; z += minBoxSize) {
                for (double y = 0; y <= height - boxHeight; y += minBoxSize) {
                    for (double x = 0; x <= width - boxWidth; x += minBoxSize) {
                        if (gridIndex.canPlace(x, y, z, boxWidth, boxHeight, boxDepth)) {
                            tempBox.setPosition(new Point3D(x, y, z));
                            tempBox.setPlaced(true);
                            
                            placedBoxes.add(tempBox);
                            currentWeight += tempBox.getWeight();
                            gridIndex.placeBox(tempBox);
                            
                            // 复制状态到原始盒子
                            box.setPosition(tempBox.getPosition());
                            box.setOrientation(tempBox.getOrientation());
                            box.setPlaced(true);
                            
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 移除盒子
     */
    public boolean removeBox(Box box) {
        if (placedBoxes.remove(box)) {
            currentWeight -= box.getWeight();
            gridIndex.removeBox(box);
            box.setPlaced(false);
            return true;
        }
        return false;
    }
    
    /**
     * 获取性能统计
     */
    public String getPerformanceStats() {
        return String.format("Container: %s\nGrid: %s\nCandidates: %d",
            toString(), gridIndex.getStatistics(), candidatePositions.size());
    }
    
    // Getters
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getDepth() { return depth; }
    public double getMaxWeight() { return maxWeight; }
    public double getCurrentWeight() { return currentWeight; }
    public double getVolume() { return width * height * depth; }
    public List<Box> getPlacedBoxes() { return new ArrayList<>(placedBoxes); }
    public int getPlacedBoxCount() { return placedBoxes.size(); }
    
    public double getUtilization() {
        double usedVolume = placedBoxes.stream().mapToDouble(Box::getVolume).sum();
        return usedVolume / getVolume();
    }
    
    public double getWeightUtilization() {
        return currentWeight / maxWeight;
    }
    
    @Override
    public String toString() {
        return String.format("OptimizedContainer[dims=(%.1f,%.1f,%.1f), boxes=%d, util=%.2f%%, weight=%.1f/%.1f]",
            width, height, depth, getPlacedBoxCount(), getUtilization() * 100, currentWeight, maxWeight);
    }
}