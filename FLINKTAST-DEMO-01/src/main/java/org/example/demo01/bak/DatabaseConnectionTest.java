package org.example.demo01.bak;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 数据库连接管理器测试类
 * 测试动态配置功能
 */
public class DatabaseConnectionTest {
    
    public static void main(String[] args) {
        System.out.println("=== 数据库动态配置测试 ===");
        
        DatabaseConnectionManager dbManager = DatabaseConnectionManager.getInstance();
        
        // 测试连接
        testConnection(dbManager);
        
        // 测试数据查询
        testQuery(dbManager);
        
        // 测试配置重新加载功能
        testConfigReload(dbManager);
    }
    
    private static void testConnection(DatabaseConnectionManager dbManager) {
        System.out.println("\n--- 测试数据库连接 ---");
        boolean connected = dbManager.testConnection();
        System.out.println("数据库连接状态: " + (connected ? "成功" : "失败"));
        System.out.println("连接池状态: " + dbManager.getPoolStatus());
    }
    
    private static void testQuery(DatabaseConnectionManager dbManager) {
        System.out.println("\n--- 测试数据查询 ---");
        try {
            Connection connection = dbManager.getConnection();
            
            // 测试简单查询
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) as count FROM my_record");
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                System.out.println("my_record 表中的记录数: " + count);
            }
            
            // 关闭资源
            resultSet.close();
            statement.close();
            connection.close();
            
            System.out.println("连接池状态 (连接返回后): " + dbManager.getPoolStatus());
            
        } catch (Exception e) {
            System.err.println("数据查询失败: " + e.getMessage());
        }
    }
    
    private static void testConfigReload(DatabaseConnectionManager dbManager) {
        System.out.println("\n--- 测试配置重新加载 ---");
        
        // 打印当前配置
        DatabaseConfig currentConfig = dbManager.getConfig();
        System.out.println("当前数据库URL: " + currentConfig.getDbUrl());
        System.out.println("当前连接池最大连接数: " + currentConfig.getMaxPoolSize());
        
        // 模拟设置系统属性
        System.out.println("\n设置系统属性来覆盖配置...");
        System.setProperty("database.pool.maxSize", "20");
        System.setProperty("database.pool.minIdle", "5");
        
        // 重新加载配置
        dbManager.reloadConfiguration();
        
        // 验证配置是否生效
        DatabaseConfig newConfig = dbManager.getConfig();
        System.out.println("新的连接池最大连接数: " + newConfig.getMaxPoolSize());
        System.out.println("新的连接池最小空闲连接数: " + newConfig.getMinIdle());
        
        // 清理系统属性
        System.clearProperty("database.pool.maxSize");
        System.clearProperty("database.pool.minIdle");
        
        System.out.println("配置重新加载测试完成！");
    }
}