import java.util.*;

/**
 * 3D装箱算法演示程序
 * 展示网格索引+剪枝优化如何将复杂度从O(n³)降低到O(n²log n)
 */
public class BinPacking3DDemo {
    
    public static void main(String[] args) {
        System.out.println("=== 3D装箱算法演示 ===");
        System.out.println("使用网格索引+剪枝策略优化空间搜索");
        System.out.println("复杂度从O(n³)优化到O(n²log n)\n");
        
        // 基础演示
        basicDemo();
        
        // 性能对比演示
        performanceComparison();
        
        // 复杂度验证
        complexityAnalysis();
        
        // 实际应用场景演示
        realWorldScenario();
    }
    
    /**
     * 基础功能演示
     */
    private static void basicDemo() {
        System.out.println("=== 基础功能演示 ===\n");
        
        // 创建容器
        OptimizedContainer container = new OptimizedContainer(50, 40, 30);
        System.out.println("创建容器: " + container);
        
        // 创建一些测试盒子
        List<Box> boxes = Arrays.asList(
            new Box(1, 10, 8, 6),   // 小盒子
            new Box(2, 15, 12, 10), // 中盒子
            new Box(3, 20, 15, 12), // 大盒子
            new Box(4, 8, 8, 8),    // 正方体
            new Box(5, 25, 5, 8)    // 长条形
        );
        
        System.out.println("待装箱盒子:");
        for (Box box : boxes) {
            System.out.println("  " + box);
        }
        System.out.println();
        
        // 使用优化算法装箱
        System.out.println("开始装箱...");
        int placedCount = 0;
        long startTime = System.currentTimeMillis();
        
        for (Box box : boxes) {
            Box testBox = new Box(box);
            if (container.placeBoxOptimized(testBox)) {
                placedCount++;
                System.out.println("✓ 成功放置: " + testBox);
            } else {
                System.out.println("✗ 无法放置: " + box);
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("\n装箱结果:");
        System.out.println("成功放置: " + placedCount + "/" + boxes.size());
        System.out.println("空间利用率: " + String.format("%.2f%%", container.getUtilization() * 100));
        System.out.println("执行时间: " + (endTime - startTime) + "ms");
        System.out.println();
        
        // 显示详细统计
        System.out.println("详细统计:");
        System.out.println(container.getPerformanceStats());
        System.out.println();
    }
    
    /**
     * 性能对比演示
     */
    private static void performanceComparison() {
        System.out.println("=== 性能对比演示 ===\n");
        
        int[] testSizes = {10, 20, 30};
        
        for (int size : testSizes) {
            System.out.println("测试规模: " + size + " 个盒子");
            List<Box> boxes = generateRandomBoxes(size);
            
            // 优化算法测试
            OptimizedContainer optimizedContainer = new OptimizedContainer(100, 80, 60);
            long startTime = System.currentTimeMillis();
            int optimizedPlaced = 0;
            
            for (Box box : boxes) {
                Box testBox = new Box(box);
                if (optimizedContainer.placeBoxOptimized(testBox)) {
                    optimizedPlaced++;
                }
            }
            
            long optimizedTime = System.currentTimeMillis() - startTime;
            
            // 暴力算法测试（仅小规模）
            long bruteForceTime = -1;
            int bruteForcePlaced = 0;
            
            if (size <= 20) {
                OptimizedContainer bruteContainer = new OptimizedContainer(100, 80, 60);
                startTime = System.currentTimeMillis();
                
                for (Box box : boxes) {
                    Box testBox = new Box(box);
                    if (bruteContainer.placeBoxBruteForce(testBox)) {
                        bruteForcePlaced++;
                    }
                }
                
                bruteForceTime = System.currentTimeMillis() - startTime;
            }
            
            // 输出结果
            System.out.println("优化算法: " + optimizedPlaced + " 个盒子, " + optimizedTime + "ms");
            if (bruteForceTime >= 0) {
                System.out.println("暴力算法: " + bruteForcePlaced + " 个盒子, " + bruteForceTime + "ms");
                if (bruteForceTime > 0) {
                    System.out.println("性能提升: " + String.format("%.1f", (double)bruteForceTime / optimizedTime) + "x");
                }
            } else {
                System.out.println("暴力算法: 跳过（规模过大）");
            }
            System.out.println();
        }
    }
    
    /**
     * 复杂度分析演示
     */
    private static void complexityAnalysis() {
        System.out.println("=== 算法复杂度分析 ===\n");
        
        int[] sizes = {5, 10, 20, 40};
        
        System.out.println("数据规模\t执行时间(ms)\t理论复杂度");
        System.out.println("----------------------------------------");
        
        for (int size : sizes) {
            List<Box> boxes = generateRandomBoxes(size);
            OptimizedContainer container = new OptimizedContainer(80, 80, 80);
            
            long startTime = System.nanoTime();
            
            for (Box box : boxes) {
                Box testBox = new Box(box);
                container.placeBoxOptimized(testBox);
            }
            
            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1_000_000.0;
            
            // 理论复杂度 O(n²log n)
            double theoretical = size * size * Math.log(size);
            
            System.out.println(size + "\t\t" + String.format("%.2f", timeMs) + 
                             "\t\t" + String.format("%.1f", theoretical));
        }
        
        System.out.println("\n说明: 理论复杂度为相对值，展示算法增长趋势");
        System.out.println("实际性能还受到常数因子、缓存效应等因素影响\n");
    }
    
    /**
     * 实际应用场景演示
     */
    private static void realWorldScenario() {
        System.out.println("=== 实际应用场景演示 ===\n");
        System.out.println("场景: 物流仓储 - 将不同规格的包裹装入标准集装箱");
        
        // 标准20英尺集装箱内部尺寸 (单位: 分米)
        double containerLength = 589;  // 58.9米 = 589分米
        double containerWidth = 235;   // 23.5米 = 235分米  
        double containerHeight = 239;  // 23.9米 = 239分米
        
        OptimizedContainer container = new OptimizedContainer(
            containerLength, containerWidth, containerHeight);
        
        System.out.println("集装箱规格: " + containerLength + "×" + containerWidth + "×" + containerHeight + " (分米)\n");
        
        // 生成真实的包裹数据
        List<Box> packages = generateRealisticPackages(100);
        
        System.out.println("生成 " + packages.size() + " 个包裹，开始装箱...");
        
        long startTime = System.currentTimeMillis();
        int placedCount = 0;
        double totalVolume = 0;
        
        for (Box pkg : packages) {
            Box testPkg = new Box(pkg);
            if (container.placeBoxOptimized(testPkg)) {
                placedCount++;
                totalVolume += pkg.getVolume();
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // 输出结果
        System.out.println("\n=== 装箱结果 ===");
        System.out.println("成功装入: " + placedCount + "/" + packages.size() + " 个包裹");
        System.out.println("空间利用率: " + String.format("%.2f%%", container.getUtilization() * 100));
        System.out.println("装箱时间: " + (endTime - startTime) + "ms");
        System.out.println("平均每包裹: " + String.format("%.2f", (double)(endTime - startTime) / packages.size()) + "ms");
        
        // 经济效益分析
        double containerCost = 2000; // 集装箱运输成本
        double costPerPackage = placedCount > 0 ? containerCost / placedCount : 0;
        System.out.println("\n=== 经济效益 ===");
        System.out.println("集装箱运输成本: ¥" + containerCost);
        System.out.println("平均每包裹成本: ¥" + String.format("%.2f", costPerPackage));
        System.out.println("未装入的包裹数: " + (packages.size() - placedCount));
        
        System.out.println("\n" + container.getPerformanceStats());
    }
    
    /**
     * 生成随机测试盒子
     */
    private static List<Box> generateRandomBoxes(int count) {
        List<Box> boxes = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        
        for (int i = 0; i < count; i++) {
            double width = 3 + random.nextDouble() * 17;   // 3-20
            double height = 3 + random.nextDouble() * 17;  // 3-20
            double depth = 3 + random.nextDouble() * 17;   // 3-20
            
            boxes.add(new Box(i, width, height, depth));
        }
        
        // 按体积降序排序
        boxes.sort((b1, b2) -> Double.compare(b2.getVolume(), b1.getVolume()));
        
        return boxes;
    }
    
    /**
     * 生成真实的包裹数据
     */
    private static List<Box> generateRealisticPackages(int count) {
        List<Box> packages = new ArrayList<>();
        Random random = new Random(42); // 固定种子确保可重复
        
        // 定义常见的包裹类型
        double[][] packageTypes = {
            {30, 20, 10},   // 小包裹
            {40, 30, 20},   // 中包裹  
            {60, 40, 30},   // 大包裹
            {80, 60, 40},   // 特大包裹
            {100, 20, 15},  // 长条包裹
            {25, 25, 25}    // 正方体包裹
        };
        
        for (int i = 0; i < count; i++) {
            // 随机选择包裹类型
            double[] type = packageTypes[random.nextInt(packageTypes.length)];
            
            // 添加一些随机变化
            double width = type[0] * (0.8 + random.nextDouble() * 0.4);
            double height = type[1] * (0.8 + random.nextDouble() * 0.4);
            double depth = type[2] * (0.8 + random.nextDouble() * 0.4);
            
            packages.add(new Box(i, width, height, depth));
        }
        
        // 按体积降序排序（大的先装）
        packages.sort((p1, p2) -> Double.compare(p2.getVolume(), p1.getVolume()));
        
        return packages;
    }
}