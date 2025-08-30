package org.example.demo01.sourceDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 数据源路由器
 * 提供智能的数据源选择策略，确保连接类型安全
 */
public class DataSourceRouter {
    
    private final MultiDataSourceConnectionManager connectionManager;
    
    public DataSourceRouter() {
        this.connectionManager = MultiDataSourceConnectionManager.getInstance();
    }
    
    /**
     * 读写分离策略：写操作使用主库，读操作可使用只读库
     */
    public enum OperationType {
        READ,   // 读操作
        WRITE   // 写操作
    }
    
    /**
     * 根据操作类型自动选择合适的数据源
     */
    public Connection getConnection(OperationType operationType) throws SQLException {
        switch (operationType) {
            case READ:
                return getReadConnection();
            case WRITE:
                return getWriteConnection();
            default:
                throw new IllegalArgumentException("未支持的操作类型: " + operationType);
        }
    }
    
    /**
     * 获取读连接（支持读写分离）
     */
    public Connection getReadConnection() throws SQLException {
        // 优先使用只读数据源，如果不可用则使用主数据源
        if (connectionManager.isDataSourceAvailable(DataSourceType.MYSQL_READONLY)) {
            try {
                return connectionManager.getConnection(DataSourceType.MYSQL_READONLY);
            } catch (SQLException e) {
                System.err.println("只读数据源连接失败，降级到主数据源: " + e.getMessage());
            }
        }
        
        // 降级到主数据源
        return connectionManager.getConnection(DataSourceType.MYSQL_PRIMARY);
    }
    
    /**
     * 获取写连接（只能使用主数据源）
     */
    public Connection getWriteConnection() throws SQLException {
        return connectionManager.getConnection(DataSourceType.MYSQL_PRIMARY);
    }
    
//    /**
//     * 获取分析数据库连接
//     */
//    public Connection getAnalyticsConnection() throws SQLException {
//        if (connectionManager.isDataSourceAvailable(DataSourceType.ANALYTICS_DB)) {
//            return connectionManager.getConnection(DataSourceType.ANALYTICS_DB);
//        }
//        throw new SQLException("分析数据库不可用");
//    }
    
//    /**
//     * 获取日志数据库连接
//     */
//    public Connection getLogConnection() throws SQLException {
//        if (connectionManager.isDataSourceAvailable(DataSourceType.LOG_DB)) {
//            return connectionManager.getConnection(DataSourceType.LOG_DB);
//        }
//        throw new SQLException("日志数据库不可用");
//    }
    
    /**
     * 负载均衡策略：在多个相同类型的数据源之间分配负载
     */
    public Connection getConnectionWithLoadBalance(DataSourceType... candidateTypes) throws SQLException {
        if (candidateTypes == null || candidateTypes.length == 0) {
            throw new IllegalArgumentException("候选数据源类型不能为空");
        }
        
        // 过滤出可用的数据源
        DataSourceType[] availableTypes = java.util.Arrays.stream(candidateTypes)
                .filter(connectionManager::isDataSourceAvailable)
                .toArray(DataSourceType[]::new);
        
        if (availableTypes.length == 0) {
            throw new SQLException("没有可用的数据源: " + java.util.Arrays.toString(candidateTypes));
        }
        
        // 简单的随机负载均衡
        int index = ThreadLocalRandom.current().nextInt(availableTypes.length);
        DataSourceType selectedType = availableTypes[index];
        
        System.out.println("负载均衡选择数据源: " + selectedType);
        return connectionManager.getConnection(selectedType);
    }
    
    /**
     * 故障转移策略：按优先级尝试数据源
     */
    public Connection getConnectionWithFailover(DataSourceType... priorityTypes) throws SQLException {
        if (priorityTypes == null || priorityTypes.length == 0) {
            throw new IllegalArgumentException("优先级数据源类型不能为空");
        }
        
        SQLException lastException = null;
        
        for (DataSourceType type : priorityTypes) {
            if (connectionManager.isDataSourceAvailable(type)) {
                try {
                    Connection connection = connectionManager.getConnection(type);
                    System.out.println("故障转移选择数据源: " + type);
                    return connection;
                } catch (SQLException e) {
                    lastException = e;
                    System.err.println("数据源 " + type + " 连接失败，尝试下一个: " + e.getMessage());
                }
            }
        }
        
        throw new SQLException("所有数据源均不可用", lastException);
    }
    
    /**
     * 获取指定用途的连接
     */
    public Connection getConnectionForPurpose(String purpose) throws SQLException {
        switch (purpose.toLowerCase()) {
            case "analytics":
//            case "report":
//                return getAnalyticsConnection();
//            case "log":
//            case "audit":
//                return getLogConnection();
            case "read":
            case "query":
                return getReadConnection();
            case "write":
            case "update":
            case "insert":
                return getWriteConnection();
            default:
                // 默认使用主数据源
                return connectionManager.getConnection(DataSourceType.MYSQL_PRIMARY);
        }
    }
    
    /**
     * 根据表名智能选择数据源
     */
    public Connection getConnectionByTableName(String tableName) throws SQLException {
        // 根据表名前缀或命名规则选择数据源
//        if (tableName.startsWith("log_") || tableName.startsWith("audit_")) {
//            return getLogConnection();
//        } else if (tableName.startsWith("analytics_") || tableName.startsWith("report_")) {
//            return getAnalyticsConnection();
//        } else {
//            // 对于普通业务表，可以使用读写分离
//            return getReadConnection();
//        }
        return getReadConnection();
    }
    
    /**
     * 检查数据源路由健康状态
     */
    public void checkRouterHealth() {
        System.out.println("=== 数据源路由器健康检查 ===");
        
        System.out.println("主数据源: " + 
            (connectionManager.isDataSourceAvailable(DataSourceType.MYSQL_PRIMARY) ? "✅" : "❌"));
        
        System.out.println("只读数据源: " + 
            (connectionManager.isDataSourceAvailable(DataSourceType.MYSQL_READONLY) ? "✅" : "❌"));
            
//        System.out.println("分析数据库: " +
//            (connectionManager.isDataSourceAvailable(DataSourceType.ANALYTICS_DB) ? "✅" : "❌"));
//
//        System.out.println("日志数据库: " +
//            (connectionManager.isDataSourceAvailable(DataSourceType.LOG_DB) ? "✅" : "❌"));
            
        System.out.println("==========================");
    }
}