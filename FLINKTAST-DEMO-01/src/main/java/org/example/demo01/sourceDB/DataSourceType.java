package org.example.demo01.sourceDB;

/**
 * 数据源类型枚举
 * 定义系统支持的各种数据源类型
 */
public enum DataSourceType {
    
    /**
     * 默认MySQL数据源 - 主业务数据库
     */
    MYSQL_PRIMARY("mysql_primary", "主MySQL数据库"),
    
    /**
     * MySQL只读数据源 - 读写分离
     */
    MYSQL_READONLY("mysql_readonly", "MySQL只读数据库"),
    
     /**
      * PostgreSQL数据源
      */
     POSTGRESQL("postgresql", "PostgreSQL数据库"),
    
     /**
      * Oracle数据源
      */
     ORACLE("oracle", "Oracle数据库");
    
    // /**
    //  * 分析数据库 - 用于数据分析
    //  */
    // ANALYTICS_DB("analytics", "分析数据库"),
    
    // /**
    //  * 缓存数据库 - Redis等
    //  */
    // CACHE_DB("cache", "缓存数据库"),
    
    // /**
    //  * 日志数据库
    //  */
    // LOG_DB("log", "日志数据库");
    
    private final String code;
    private final String description;
    
    DataSourceType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取数据源类型
     */
    public static DataSourceType fromCode(String code) {
        for (DataSourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的数据源类型: " + code);
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", description, code);
    }
}