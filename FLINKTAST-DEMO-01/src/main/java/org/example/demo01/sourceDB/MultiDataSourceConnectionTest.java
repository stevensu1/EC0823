package org.example.demo01.sourceDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * 多数据源连接管理器测试类
 * 演示如何安全地使用多种数据库连接
 */
public class MultiDataSourceConnectionTest {
    
    public static void main(String[] args) {
        System.out.println("=== 多数据源连接管理器测试 ===");
        
        MultiDataSourceConnectionManager manager = MultiDataSourceConnectionManager.getInstance();
        DataSourceRouter router = new DataSourceRouter();
        
        // 测试所有数据源连接
        testAllConnections(manager);
        
        // 测试类型安全的连接获取
        testTypeSafeConnections(manager);
        
        // 测试数据源路由功能
        testDataSourceRouter(router);
        
        // 测试连接池状态监控
        testPoolStatusMonitoring(manager);
        
        // 测试错误处理
        testErrorHandling(manager);
        
        // 测试连接类型验证
        testConnectionTypeValidation(manager);
        
        System.out.println("\\n=== 测试完成 ===");
    }
    
    /**
     * 测试所有数据源连接
     */
    private static void testAllConnections(MultiDataSourceConnectionManager manager) {
        System.out.println("\\n--- 测试所有数据源连接 ---");
        
        Map<DataSourceType, Boolean> results = manager.testAllConnections();
        
        for (Map.Entry<DataSourceType, Boolean> entry : results.entrySet()) {
            String status = entry.getValue() ? "✅ 连接成功" : "❌ 连接失败";
            System.out.println(entry.getKey() + ": " + status);
        }
        
        System.out.println("可用数据源: " + manager.getAvailableDataSourceTypes().length + " 个");
    }
    
    /**
     * 测试类型安全的连接获取
     */
    private static void testTypeSafeConnections(MultiDataSourceConnectionManager manager) {
        System.out.println("\\n--- 测试类型安全的连接获取 ---");
        
        // 测试主数据源连接
        testSpecificDataSource(manager, DataSourceType.MYSQL_PRIMARY, "my_record");
        
        // 如果配置了其他数据源，也可以测试
        for (DataSourceType type : manager.getAvailableDataSourceTypes()) {
            if (type != DataSourceType.MYSQL_PRIMARY) {
                testSpecificDataSource(manager, type, "test_table");
            }
        }
    }
    
    /**
     * 测试指定数据源的连接
     */
    private static void testSpecificDataSource(MultiDataSourceConnectionManager manager, DataSourceType type, String tableName) {
        try {
            System.out.println("\\n测试数据源: " + type);
            
            // 获取指定类型的连接
            Connection connection = manager.getConnection(type);
            
            // 验证连接类型
            String connectionType = connection.getClientInfo("dataSourceType");
            System.out.println("连接类型标识: " + connectionType);
            
            // 执行简单查询（如果表存在）
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 as test_column"
                );
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    System.out.println("查询测试成功: " + resultSet.getInt("test_column"));
                }
                
                resultSet.close();
                statement.close();
                
            } catch (Exception e) {
                System.out.println("查询测试跳过（表可能不存在）: " + e.getMessage());
            }
            
            connection.close();
            System.out.println("连接已正确关闭");
            
        } catch (Exception e) {
            System.err.println("数据源测试失败: " + type + ", 错误: " + e.getMessage());
        }
    }
    
    /**
     * 测试连接池状态监控
     */
    private static void testPoolStatusMonitoring(MultiDataSourceConnectionManager manager) {
        System.out.println("\\n--- 测试连接池状态监控 ---");
        
        // 显示所有连接池状态
        System.out.println(manager.getAllPoolStatus());
        
        // 显示单个连接池状态
        for (DataSourceType type : manager.getAvailableDataSourceTypes()) {
            System.out.println(manager.getPoolStatus(type));
        }
    }
    
    /**
     * 测试错误处理
     */
    private static void testErrorHandling(MultiDataSourceConnectionManager manager) {
        System.out.println("\\n--- 测试错误处理 ---");

        // 测试获取不存在的数据源连接
        try {
            // 假设这个数据源没有配置
            DataSourceType unavailableType = DataSourceType.ORACLE;
            if (!manager.isDataSourceAvailable(unavailableType)) {
                Connection conn = manager.getConnection(unavailableType);
                System.out.println("不应该到达这里");
            } else {
                System.out.println(unavailableType + " 数据源可用，跳过错误测试");
            }
        } catch (Exception e) {
            System.out.println("✅ 正确捕获错误: " + e.getMessage());
        }

        // 测试数据源可用性检查
        for (DataSourceType type : DataSourceType.values()) {
            boolean available = manager.isDataSourceAvailable(type);
            System.out.println(type + " 可用性: " + (available ? "✅" : "❌"));
        }
    }
    
    /**
     * 测试数据源路由功能
     */
    private static void testDataSourceRouter(DataSourceRouter router) {
        System.out.println("\n--- 测试数据源路由功能 ---");
        
        // 检查路由器健康状态
        router.checkRouterHealth();
        
        // 测试读写分离
        try {
            System.out.println("\n测试读写分离:");
            
            Connection readConn = router.getConnection(DataSourceRouter.OperationType.READ);
            String readType = readConn.getClientInfo("dataSourceType");
            System.out.println("读连接类型: " + readType);
            readConn.close();
            
            Connection writeConn = router.getConnection(DataSourceRouter.OperationType.WRITE);
            String writeType = writeConn.getClientInfo("dataSourceType");
            System.out.println("写连接类型: " + writeType);
            writeConn.close();
            
        } catch (Exception e) {
            System.err.println("读写分离测试失败: " + e.getMessage());
        }
        
        // 测试按用途获取连接
        testConnectionByPurpose(router, "read");
        testConnectionByPurpose(router, "write");
        testConnectionByPurpose(router, "analytics");
    }
    
    private static void testConnectionByPurpose(DataSourceRouter router, String purpose) {
        try {
            Connection conn = router.getConnectionForPurpose(purpose);
            String type = conn.getClientInfo("dataSourceType");
            System.out.println(purpose + " 用途连接类型: " + type);
            conn.close();
        } catch (Exception e) {
            System.out.println(purpose + " 用途连接不可用: " + e.getMessage());
        }
    }
    
    /**
     * 测试连接类型验证 - 确保不会获取到错误类型的连接
     */
    private static void testConnectionTypeValidation(MultiDataSourceConnectionManager manager) {
        System.out.println("\n--- 测试连接类型验证 ---");
        
        for (DataSourceType type : manager.getAvailableDataSourceTypes()) {
            try {
                // 获取连接
                Connection connection = manager.getConnection(type);
                
                // 验证连接标识
                String actualType = connection.getClientInfo("dataSourceType");
                String expectedType = type.getCode();
                
                if (expectedType.equals(actualType)) {
                    System.out.println("✅ " + type + " 连接类型验证通过");
                } else {
                    System.err.println("❌ " + type + " 连接类型验证失败! 期望: " + expectedType + ", 实际: " + actualType);
                }
                
                connection.close();
                
            } catch (Exception e) {
                System.err.println("❌ " + type + " 连接类型验证异常: " + e.getMessage());
            }
        }
    }
}