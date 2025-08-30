package org.example.demo01.bak;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接管理器
 * 使用HikariCP连接池管理MySQL数据库连接
 * 支持动态配置管理：配置文件、环境变量、系统属性
 */
public class DatabaseConnectionManager {
    
    private static DatabaseConnectionManager instance;
    private HikariDataSource dataSource;
    private DatabaseConfig config;
    
    private DatabaseConnectionManager() {
        this.config = new DatabaseConfig();
        config.printConfiguration(); // 打印配置信息
        initializeDataSource();
        setupConfigurationListeners(); // 设置配置监听器
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }
    
    /**
     * 初始化HikariCP数据源
     */
    private void initializeDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        
        // 数据库连接配置
        hikariConfig.setJdbcUrl(config.getDbUrl());
        hikariConfig.setUsername(config.getDbUsername());
        hikariConfig.setPassword(config.getDbPassword());
        hikariConfig.setDriverClassName(config.getDbDriver());
        
        // 连接池配置
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getMinIdle());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());
        hikariConfig.setLeakDetectionThreshold(config.getLeakDetectionThreshold());
        
        // 连接测试配置
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setValidationTimeout(3000);
        
        this.dataSource = new HikariDataSource(hikariConfig);
        
        System.out.println("数据库连接池初始化成功！");
    }
    
    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * 关闭数据源
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    /**
     * 获取连接池状态信息
     */
    public String getPoolStatus() {
        if (dataSource != null) {
            return String.format(
                "Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "DataSource not initialized";
    }
    
    /**
     * 获取当前配置信息
     */
    public DatabaseConfig getConfig() {
        return config;
    }
    
    /**
     * 重新加载配置并重启数据源
     * 注意：这会关闭现有连接并重新创建连接池
     */
    public synchronized void reloadConfiguration() {
        System.out.println("正在重新加载数据库配置...");
        
        // 关闭现有数据源
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        
        // 重新加载配置
        this.config = new DatabaseConfig();
        config.printConfiguration();
        
        // 重新初始化数据源
        initializeDataSource();
        
        System.out.println("数据库配置重新加载完成！");
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (Exception e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 设置配置监听器
     */
    private void setupConfigurationListeners() {
        // 监听数据库URL变更
        ConfigurationManager.addConfigChangeListener(DatabaseConfig.DB_URL_KEY, 
            (key, oldValue, newValue) -> {
                System.out.println("数据库URL发生变更: " + oldValue + " -> " + newValue);
                System.out.println("建议重启应用以使配置生效");
            });
            
        // 监听连接池大小变更
        ConfigurationManager.addConfigChangeListener(DatabaseConfig.POOL_MAX_SIZE_KEY,
            (key, oldValue, newValue) -> {
                System.out.println("连接池最大连接数发生变更: " + oldValue + " -> " + newValue);
                // 可以在这里实现热重载逻辑
            });
    }
}