import java.util.*;

/**
 * 3D装箱性能测试类
 * 对比网格索引+剪枝优化 vs 暴力搜索的性能差异
 */
public class BinPackingPerformanceTest {
    
    public static void main(String[] args) {
        System.out.println("=== 3D装箱算法性能测试 ===\n");
        
        // 测试不同规模的数据
        int[] boxCounts = {10, 20, 50, 100};
        
        for (int boxCount : boxCounts) {
            System.out.println("测试盒子数量: " + boxCount);
            runPerformanceTest(boxCount);
            System.out.println();
        }
    }
    
    /**
     * 运行性能测试
     */
    public static void runPerformanceTest(int boxCount) {
        // 创建测试数据
        List<Box> boxes = generateTestBoxes(boxCount);
        double containerWidth = 100.0;
        double containerHeight = 80.0;
        double containerDepth = 60.0;
        
        System.out.println("容器尺寸: " + containerWidth + "x" + containerHeight + "x" + containerDepth);
        
        // 测试优化算法
        testOptimizedAlgorithm(boxes, containerWidth, containerHeight, containerDepth);
        
        // 测试暴力算法（仅在盒子数量较少时）
        if (boxCount <= 50) {
            testBruteForceAlgorithm(boxes, containerWidth, containerHeight, containerDepth);
        } else {
            System.out.println("暴力算法: 跳过测试（数据量过大）");
        }
        
        // 测试基础算法
        testBasicAlgorithm(boxes, containerWidth, containerHeight, containerDepth);
    }
    
    /**
     * 测试优化算法
     */
    private static void testOptimizedAlgorithm(List<Box> boxes, double width, double height, double depth) {
        System.out.println("--- 优化算法 (网格索引+剪枝) ---");
        
        OptimizedContainer container = new OptimizedContainer(width, height, depth);
        
        long startTime = System.currentTimeMillis();
        int placedCount = 0;
        
        for (Box box : boxes) {
            Box testBox = new Box(box);
            if (container.placeBoxOptimized(testBox)) {
                placedCount++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("放置成功: " + placedCount + "/" + boxes.size());
        System.out.println("空间利用率: " + String.format("%.2f%%", container.getUtilization() * 100));
        System.out.println("执行时间: " + duration + "ms");
        System.out.println("平均每个盒子: " + String.format("%.2f", (double)duration / boxes.size()) + "ms");
        System.out.println(container.getPerformanceStats());
        System.out.println();
    }
    
    /**
     * 测试暴力算法
     */
    private static void testBruteForceAlgorithm(List<Box> boxes, double width, double height, double depth) {
        System.out.println("--- 暴力算法 (O(n³)搜索) ---");
        
        OptimizedContainer container = new OptimizedContainer(width, height, depth);
        
        long startTime = System.currentTimeMillis();
        int placedCount = 0;
        
        for (Box box : boxes) {
            Box testBox = new Box(box);
            if (container.placeBoxBruteForce(testBox)) {
                placedCount++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("放置成功: " + placedCount + "/" + boxes.size());
        System.out.println("空间利用率: " + String.format("%.2f%%", container.getUtilization() * 100));
        System.out.println("执行时间: " + duration + "ms");
        System.out.println("平均每个盒子: " + String.format("%.2f", (double)duration / boxes.size()) + "ms");
        System.out.println();
    }
    
    /**
     * 测试基础算法
     */
    private static void testBasicAlgorithm(List<Box> boxes, double width, double height, double depth) {
        System.out.println("--- 基础算法 (简单放置) ---");
        
        Container container = new Container(width, height, depth);
        
        long startTime = System.currentTimeMillis();
        int placedCount = 0;
        
        for (Box box : boxes) {
            Box testBox = new Box(box);
            List<Point3D> candidates = container.getPossiblePlacements();
            
            boolean placed = false;
            for (int orientation = 0; orientation < 6 && !placed; orientation++) {
                testBox.setOrientation(orientation);
                for (Point3D pos : candidates) {
                    if (container.placeBox(testBox, pos, orientation)) {
                        placedCount++;
                        placed = true;
                        break;
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("放置成功: " + placedCount + "/" + boxes.size());
        System.out.println("空间利用率: " + String.format("%.2f%%", container.getUtilization() * 100));
        System.out.println("执行时间: " + duration + "ms");
        System.out.println("平均每个盒子: " + String.format("%.2f", (double)duration / boxes.size()) + "ms");
        System.out.println();
    }
    
    /**
     * 生成测试盒子
     */
    private static List<Box> generateTestBoxes(int count) {
        List<Box> boxes = new ArrayList<>();
        Random random = new Random(42); // 固定种子确保可重复性
        
        for (int i = 0; i < count; i++) {
            // 生成不同尺寸的盒子
            double width = 5 + random.nextDouble() * 15;   // 5-20
            double height = 5 + random.nextDouble() * 15;  // 5-20
            double depth = 5 + random.nextDouble() * 15;   // 5-20
            
            boxes.add(new Box(i, width, height, depth));
        }
        
        // 按体积降序排序（大盒子优先）
        boxes.sort((b1, b2) -> Double.compare(b2.getVolume(), b1.getVolume()));
        
        return boxes;
    }
    
    /**
     * 详细的性能分析
     */
    public static void detailedPerformanceAnalysis() {
        System.out.println("=== 详细性能分析 ===\n");
        
        // 测试不同容器大小对性能的影响
        double[] containerSizes = {50, 100, 200};
        int boxCount = 30;
        
        for (double size : containerSizes) {
            System.out.println("容器尺寸: " + size + "³");
            List<Box> boxes = generateTestBoxes(boxCount);
            
            OptimizedContainer container = new OptimizedContainer(size, size, size);
            
            long startTime = System.nanoTime();
            int placedCount = 0;
            
            for (Box box : boxes) {
                Box testBox = new Box(box);
                if (container.placeBoxOptimized(testBox)) {
                    placedCount++;
                }
            }
            
            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;
            
            System.out.println("放置成功: " + placedCount + "/" + boxes.size());
            System.out.println("执行时间: " + String.format("%.2f", durationMs) + "ms");
            System.out.println("空间利用率: " + String.format("%.2f%%", container.getUtilization() * 100));
            System.out.println();
        }
    }
    
    /**
     * 算法复杂度验证
     */
    public static void complexityVerification() {
        System.out.println("=== 算法复杂度验证 ===\n");
        
        int[] testSizes = {10, 20, 40, 80};
        double containerSize = 100;
        
        System.out.println("盒子数量\t优化算法(ms)\t理论复杂度O(n²logn)");
        System.out.println("------------------------------------------------");
        
        for (int size : testSizes) {
            List<Box> boxes = generateTestBoxes(size);
            OptimizedContainer container = new OptimizedContainer(containerSize, containerSize, containerSize);
            
            long startTime = System.nanoTime();
            
            for (Box box : boxes) {
                Box testBox = new Box(box);
                container.placeBoxOptimized(testBox);
            }
            
            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;
            
            // 理论复杂度 O(n²log n)
            double theoreticalComplexity = size * size * Math.log(size);
            
            System.out.println(size + "\t\t" + String.format("%.2f", durationMs) + "\t\t" + 
                             String.format("%.2f", theoreticalComplexity));
        }
        
        System.out.println("\n注: 理论复杂度为相对值，用于展示增长趋势");
    }
}