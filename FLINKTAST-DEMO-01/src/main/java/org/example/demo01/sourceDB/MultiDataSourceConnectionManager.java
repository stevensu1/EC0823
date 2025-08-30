package org.example.demo01.sourceDB;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多数据源连接管理器
 * 支持管理多种数据库的连接池，确保类型安全的连接获取
 */
public class MultiDataSourceConnectionManager {
    
    private static MultiDataSourceConnectionManager instance;
    private final Map<DataSourceType, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    private MultiDataSourceConfig config;
    
    private MultiDataSourceConnectionManager() {
        this.config = new MultiDataSourceConfig();
        initializeDataSources();
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized MultiDataSourceConnectionManager getInstance() {
        if (instance == null) {
            instance = new MultiDataSourceConnectionManager();
        }
        return instance;
    }
    
    /**
     * 初始化所有配置的数据源
     */
    private void initializeDataSources() {
        config.printConfigurations();
        
        for (DataSourceType type : config.getConfiguredDataSourceTypes()) {
            try {
                HikariDataSource dataSource = createDataSource(type);
                dataSources.put(type, dataSource);
                System.out.println("数据源初始化成功: " + type);
            } catch (Exception e) {
                System.err.println("数据源初始化失败: " + type + ", 错误: " + e.getMessage());
            }
        }
        
        System.out.println("多数据源连接管理器初始化完成，共 " + dataSources.size() + " 个数据源");
    }
    
    /**
     * 创建指定类型的数据源
     */
    private HikariDataSource createDataSource(DataSourceType type) {
        MultiDataSourceConfig.DataSourceConfiguration dsConfig = config.getConfiguration(type);
        if (dsConfig == null) {
            throw new IllegalArgumentException("数据源配置不存在: " + type);
        }
        
        HikariConfig hikariConfig = new HikariConfig();
        
        // 基本连接配置
        hikariConfig.setJdbcUrl(dsConfig.getUrl());
        hikariConfig.setUsername(dsConfig.getUsername());
        hikariConfig.setPassword(dsConfig.getPassword());
        hikariConfig.setDriverClassName(dsConfig.getDriver());
        
        // 连接池配置
        hikariConfig.setMaximumPoolSize(dsConfig.getMaxPoolSize());
        hikariConfig.setMinimumIdle(dsConfig.getMinIdle());
        hikariConfig.setConnectionTimeout(dsConfig.getConnectionTimeout());
        hikariConfig.setIdleTimeout(dsConfig.getIdleTimeout());
        hikariConfig.setMaxLifetime(dsConfig.getMaxLifetime());
        hikariConfig.setLeakDetectionThreshold(dsConfig.getLeakDetectionThreshold());
        
        // 连接池名称，便于监控
        hikariConfig.setPoolName("HikariCP-" + type.getCode());
        
        // 连接测试配置
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setValidationTimeout(3000);
        
        return new HikariDataSource(hikariConfig);
    }
    
    /**
     * 获取指定类型的数据库连接
     * 确保连接类型安全，不会获取到错误类型的连接
     */
    public Connection getConnection(DataSourceType type) throws SQLException {
        HikariDataSource dataSource = dataSources.get(type);
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在或未初始化: " + type);
        }
        
        if (dataSource.isClosed()) {
            throw new SQLException("数据源已关闭: " + type);
        }
        
        try {
            Connection connection = dataSource.getConnection();
            // 为连接添加标识，便于追踪
            connection.setClientInfo("dataSourceType", type.getCode());
            return connection;
        } catch (SQLException e) {
            throw new SQLException("获取数据库连接失败: " + type + ", 错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取默认数据源连接（主MySQL）
     */
    public Connection getConnection() throws SQLException {
        return getConnection(DataSourceType.MYSQL_PRIMARY);
    }
    
    /**
     * 测试指定数据源的连接
     */
    public boolean testConnection(DataSourceType type) {
        try (Connection connection = getConnection(type)) {
            return connection != null && !connection.isClosed();
        } catch (Exception e) {
            System.err.println("数据源连接测试失败: " + type + ", 错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试所有数据源的连接
     */
    public Map<DataSourceType, Boolean> testAllConnections() {
        Map<DataSourceType, Boolean> results = new ConcurrentHashMap<>();
        
        for (DataSourceType type : dataSources.keySet()) {
            results.put(type, testConnection(type));
        }
        
        return results;
    }
    
    /**
     * 获取指定数据源的连接池状态
     */
    public String getPoolStatus(DataSourceType type) {
        HikariDataSource dataSource = dataSources.get(type);
        if (dataSource == null) {
            return "数据源不存在: " + type;
        }
        
        try {
            return String.format(
                "%s - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                type.getDescription(),
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        } catch (Exception e) {
            return type + " - 状态获取失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取所有数据源的连接池状态
     */
    public String getAllPoolStatus() {
        StringBuilder status = new StringBuilder("=== 所有数据源连接池状态 ===\\n");
        
        for (DataSourceType type : dataSources.keySet()) {
            status.append(getPoolStatus(type)).append("\\n");
        }
        
        status.append("=============================");
        return status.toString();
    }
    
    /**
     * 检查数据源是否可用
     */
    public boolean isDataSourceAvailable(DataSourceType type) {
        HikariDataSource dataSource = dataSources.get(type);
        return dataSource != null && !dataSource.isClosed();
    }
    
    /**
     * 获取所有可用的数据源类型
     */
    public DataSourceType[] getAvailableDataSourceTypes() {
        return dataSources.keySet().stream()
                .filter(this::isDataSourceAvailable)
                .toArray(DataSourceType[]::new);
    }
    
    /**
     * 关闭指定数据源
     */
    public void closeDataSource(DataSourceType type) {
        HikariDataSource dataSource = dataSources.get(type);
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("数据源已关闭: " + type);
        }
    }
    
    /**
     * 关闭所有数据源
     */
    public void closeAllDataSources() {
        for (Map.Entry<DataSourceType, HikariDataSource> entry : dataSources.entrySet()) {
            try {
                if (!entry.getValue().isClosed()) {
                    entry.getValue().close();
                    System.out.println("数据源已关闭: " + entry.getKey());
                }
            } catch (Exception e) {
                System.err.println("关闭数据源失败: " + entry.getKey() + ", 错误: " + e.getMessage());
            }
        }
        dataSources.clear();
    }
    
    /**
     * 重新初始化指定数据源
     */
    public synchronized void reinitializeDataSource(DataSourceType type) {
        // 关闭现有数据源
        closeDataSource(type);
        dataSources.remove(type);
        
        // 重新加载配置
        this.config = new MultiDataSourceConfig();
        
        // 重新创建数据源
        try {
            HikariDataSource dataSource = createDataSource(type);
            dataSources.put(type, dataSource);
            System.out.println("数据源重新初始化成功: " + type);
        } catch (Exception e) {
            System.err.println("数据源重新初始化失败: " + type + ", 错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取数据源配置信息
     */
    public MultiDataSourceConfig getConfig() {
        return config;
    }
}