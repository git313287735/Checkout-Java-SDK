import java.util.*;

/**
 * 3D网格索引，用于优化空间查询和碰撞检测
 * 将3D空间划分为网格单元，每个单元记录占用状态
 */
public class GridIndex {
    private final int gridSizeX, gridSizeY, gridSizeZ;
    private final double cellSizeX, cellSizeY, cellSizeZ;
    private final boolean[][][] occupied;
    private final List<Box>[][][] boxGrid;
    private final double containerWidth, containerHeight, containerDepth;
    
    @SuppressWarnings("unchecked")
    public GridIndex(double containerWidth, double containerHeight, double containerDepth, 
                     double minBoxSize) {
        this.containerWidth = containerWidth;
        this.containerHeight = containerHeight;
        this.containerDepth = containerDepth;
        
        // 根据最小盒子尺寸确定网格大小
        this.cellSizeX = Math.max(1.0, minBoxSize / 2);
        this.cellSizeY = Math.max(1.0, minBoxSize / 2);
        this.cellSizeZ = Math.max(1.0, minBoxSize / 2);
        
        this.gridSizeX = (int) Math.ceil(containerWidth / cellSizeX);
        this.gridSizeY = (int) Math.ceil(containerHeight / cellSizeY);
        this.gridSizeZ = (int) Math.ceil(containerDepth / cellSizeZ);
        
        this.occupied = new boolean[gridSizeX][gridSizeY][gridSizeZ];
        this.boxGrid = new List[gridSizeX][gridSizeY][gridSizeZ];
        
        // 初始化网格
        for (int x = 0; x < gridSizeX; x++) {
            for (int y = 0; y < gridSizeY; y++) {
                for (int z = 0; z < gridSizeZ; z++) {
                    boxGrid[x][y][z] = new ArrayList<>();
                }
            }
        }
    }
    
    /**
     * 将世界坐标转换为网格坐标
     */
    private int[] worldToGrid(double x, double y, double z) {
        return new int[] {
            Math.min((int)(x / cellSizeX), gridSizeX - 1),
            Math.min((int)(y / cellSizeY), gridSizeY - 1),
            Math.min((int)(z / cellSizeZ), gridSizeZ - 1)
        };
    }
    
    /**
     * 获取盒子占用的网格范围
     */
    private int[][] getBoxGridBounds(double x, double y, double z, 
                                   double width, double height, double depth) {
        int[] minGrid = worldToGrid(x, y, z);
        int[] maxGrid = worldToGrid(x + width - 0.001, y + height - 0.001, z + depth - 0.001);
        
        return new int[][] {minGrid, maxGrid};
    }
    
    /**
     * 检查指定区域是否可以放置盒子
     */
    public boolean canPlace(double x, double y, double z, double width, double height, double depth) {
        // 边界检查
        if (x < 0 || y < 0 || z < 0 || 
            x + width > containerWidth || 
            y + height > containerHeight || 
            z + depth > containerDepth) {
            return false;
        }
        
        int[][] bounds = getBoxGridBounds(x, y, z, width, height, depth);
        int[] minGrid = bounds[0];
        int[] maxGrid = bounds[1];
        
        // 检查网格占用状态
        for (int gx = minGrid[0]; gx <= maxGrid[0]; gx++) {
            for (int gy = minGrid[1]; gy <= maxGrid[1]; gy++) {
                for (int gz = minGrid[2]; gz <= maxGrid[2]; gz++) {
                    if (occupied[gx][gy][gz]) {
                        // 进行精确碰撞检测
                        for (Box existingBox : boxGrid[gx][gy][gz]) {
                            if (boxOverlaps(x, y, z, width, height, depth, existingBox)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * 精确的盒子重叠检测
     */
    private boolean boxOverlaps(double x1, double y1, double z1, 
                              double w1, double h1, double d1, Box box2) {
        Point3D pos2 = box2.getPosition();
        double x2 = pos2.x, y2 = pos2.y, z2 = pos2.z;
        double w2 = box2.getCurrentWidth();
        double h2 = box2.getCurrentHeight();
        double d2 = box2.getCurrentDepth();
        
        return !(x1 + w1 <= x2 || x2 + w2 <= x1 ||
                 y1 + h1 <= y2 || y2 + h2 <= y1 ||
                 z1 + d1 <= z2 || z2 + d2 <= z1);
    }
    
    /**
     * 在网格中放置盒子
     */
    public void placeBox(Box box) {
        Point3D pos = box.getPosition();
        double width = box.getCurrentWidth();
        double height = box.getCurrentHeight();
        double depth = box.getCurrentDepth();
        
        int[][] bounds = getBoxGridBounds(pos.x, pos.y, pos.z, width, height, depth);
        int[] minGrid = bounds[0];
        int[] maxGrid = bounds[1];
        
        // 标记网格为占用状态
        for (int gx = minGrid[0]; gx <= maxGrid[0]; gx++) {
            for (int gy = minGrid[1]; gy <= maxGrid[1]; gy++) {
                for (int gz = minGrid[2]; gz <= maxGrid[2]; gz++) {
                    occupied[gx][gy][gz] = true;
                    boxGrid[gx][gy][gz].add(box);
                }
            }
        }
    }
    
    /**
     * 从网格中移除盒子
     */
    public void removeBox(Box box) {
        Point3D pos = box.getPosition();
        double width = box.getCurrentWidth();
        double height = box.getCurrentHeight();
        double depth = box.getCurrentDepth();
        
        int[][] bounds = getBoxGridBounds(pos.x, pos.y, pos.z, width, height, depth);
        int[] minGrid = bounds[0];
        int[] maxGrid = bounds[1];
        
        // 从网格中移除盒子
        for (int gx = minGrid[0]; gx <= maxGrid[0]; gx++) {
            for (int gy = minGrid[1]; gy <= maxGrid[1]; gy++) {
                for (int gz = minGrid[2]; gz <= maxGrid[2]; gz++) {
                    boxGrid[gx][gy][gz].remove(box);
                    // 如果网格单元为空，标记为未占用
                    if (boxGrid[gx][gy][gz].isEmpty()) {
                        occupied[gx][gy][gz] = false;
                    }
                }
            }
        }
    }
    
    /**
     * 获取可能的放置位置（使用剪枝优化）
     */
    public List<Point3D> getPossiblePlacements(double boxWidth, double boxHeight, double boxDepth) {
        Set<Point3D> placements = new TreeSet<>((p1, p2) -> {
            // 优先考虑底部、后部、左侧的位置
            int cmp = Double.compare(p1.z, p2.z);
            if (cmp != 0) return cmp;
            cmp = Double.compare(p1.y, p2.y);
            if (cmp != 0) return cmp;
            return Double.compare(p1.x, p2.x);
        });
        
        // 添加原点
        placements.add(new Point3D(0, 0, 0));
        
        // 基于网格的候选位置生成
        for (int gx = 0; gx < gridSizeX; gx++) {
            for (int gy = 0; gy < gridSizeY; gy++) {
                for (int gz = 0; gz < gridSizeZ; gz++) {
                    if (occupied[gx][gy][gz]) {
                        // 在占用网格的边界添加候选位置
                        addCandidatePositions(placements, gx, gy, gz, boxWidth, boxHeight, boxDepth);
                    }
                }
            }
        }
        
        return new ArrayList<>(placements);
    }
    
    /**
     * 添加候选位置
     */
    private void addCandidatePositions(Set<Point3D> placements, int gx, int gy, int gz,
                                     double boxWidth, double boxHeight, double boxDepth) {
        double baseX = gx * cellSizeX;
        double baseY = gy * cellSizeY;
        double baseZ = gz * cellSizeZ;
        
        // 在网格单元的6个面上添加候选位置
        Point3D[] candidates = {
            new Point3D(baseX + cellSizeX, baseY, baseZ),           // 右侧
            new Point3D(baseX, baseY + cellSizeY, baseZ),           // 上方
            new Point3D(baseX, baseY, baseZ + cellSizeZ),           // 前方
            new Point3D(baseX - boxWidth, baseY, baseZ),            // 左侧
            new Point3D(baseX, baseY - boxHeight, baseZ),           // 下方
            new Point3D(baseX, baseY, baseZ - boxDepth)             // 后方
        };
        
        for (Point3D candidate : candidates) {
            if (candidate.x >= 0 && candidate.y >= 0 && candidate.z >= 0 &&
                candidate.x + boxWidth <= containerWidth &&
                candidate.y + boxHeight <= containerHeight &&
                candidate.z + boxDepth <= containerDepth) {
                placements.add(candidate);
            }
        }
    }
    
    /**
     * 清空网格
     */
    public void clear() {
        for (int x = 0; x < gridSizeX; x++) {
            for (int y = 0; y < gridSizeY; y++) {
                for (int z = 0; z < gridSizeZ; z++) {
                    occupied[x][y][z] = false;
                    boxGrid[x][y][z].clear();
                }
            }
        }
    }
    
    /**
     * 获取网格统计信息
     */
    public String getStatistics() {
        int occupiedCells = 0;
        int totalBoxes = 0;
        
        for (int x = 0; x < gridSizeX; x++) {
            for (int y = 0; y < gridSizeY; y++) {
                for (int z = 0; z < gridSizeZ; z++) {
                    if (occupied[x][y][z]) {
                        occupiedCells++;
                        totalBoxes += boxGrid[x][y][z].size();
                    }
                }
            }
        }
        
        int totalCells = gridSizeX * gridSizeY * gridSizeZ;
        double occupancyRate = (double) occupiedCells / totalCells * 100;
        
        return String.format("Grid[%dx%dx%d], Occupied: %d/%d (%.1f%%), Boxes: %d",
            gridSizeX, gridSizeY, gridSizeZ, occupiedCells, totalCells, occupancyRate, totalBoxes);
    }
}