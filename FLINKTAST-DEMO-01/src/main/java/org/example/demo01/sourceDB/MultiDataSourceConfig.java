package org.example.demo01.sourceDB;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 多数据源配置管理类
 * 支持多种数据库的连接配置管理
 */
public class MultiDataSourceConfig  implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String CONFIG_FILE = "multi-datasource.properties";
    private Map<DataSourceType, DataSourceConfiguration> configurations = new HashMap<>();
    
    public MultiDataSourceConfig() {
        loadConfigurations();
    }
    
    /**
     * 数据源配置信息类
     */
    public static class DataSourceConfiguration implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private String url;
        private String username;
        private String password;
        private String driver;
        private int maxPoolSize = 10;
        private int minIdle = 2;
        private int connectionTimeout = 30000;
        private int idleTimeout = 600000;
        private int maxLifetime = 1800000;
        private int leakDetectionThreshold = 60000;
        private boolean enabled = true;
        
        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getDriver() { return driver; }
        public void setDriver(String driver) { this.driver = driver; }
        
        public int getMaxPoolSize() { return maxPoolSize; }
        public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
        
        public int getMinIdle() { return minIdle; }
        public void setMinIdle(int minIdle) { this.minIdle = minIdle; }
        
        public int getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        
        public int getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(int idleTimeout) { this.idleTimeout = idleTimeout; }
        
        public int getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(int maxLifetime) { this.maxLifetime = maxLifetime; }
        
        public int getLeakDetectionThreshold() { return leakDetectionThreshold; }
        public void setLeakDetectionThreshold(int leakDetectionThreshold) { this.leakDetectionThreshold = leakDetectionThreshold; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        @Override
        public String toString() {
            return String.format("DataSourceConfig{url='%s', username='%s', driver='%s', enabled=%s}", 
                url, username, driver, enabled);
        }
    }
    
    /**
     * 加载所有数据源配置
     */
    private void loadConfigurations() {
        Properties props = loadProperties();
        
        for (DataSourceType type : DataSourceType.values()) {
            DataSourceConfiguration config = loadDataSourceConfig(props, type);
            if (config != null) {
                configurations.put(type, config);
            }
        }
        
        // 确保至少有默认数据源
        if (!configurations.containsKey(DataSourceType.MYSQL_PRIMARY)) {
            configurations.put(DataSourceType.MYSQL_PRIMARY, createDefaultConfig());
        }
        
        System.out.println("已加载 " + configurations.size() + " 个数据源配置");
    }
    
    /**
     * 加载配置文件
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
                System.out.println("已加载多数据源配置文件: " + CONFIG_FILE);
            } else {
                System.out.println("多数据源配置文件不存在，使用默认配置: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("读取多数据源配置文件失败: " + e.getMessage());
        }
        
        // 从环境变量和系统属性覆盖配置
        overrideFromEnvironment(props);
        
        return props;
    }
    
    /**
     * 加载指定数据源的配置
     */
    private DataSourceConfiguration loadDataSourceConfig(Properties props, DataSourceType type) {
        String prefix = "datasource." + type.getCode() + ".";
        
        // 检查是否启用此数据源
        String enabledStr = getProperty(props, prefix + "enabled", "true");
        if (!"true".equalsIgnoreCase(enabledStr)) {
            return null;
        }
        
        DataSourceConfiguration config = new DataSourceConfiguration();
        
        config.setUrl(getProperty(props, prefix + "url", getDefaultUrl(type)));
        config.setUsername(getProperty(props, prefix + "username", "root"));
        config.setPassword(getProperty(props, prefix + "password", "root"));
        config.setDriver(getProperty(props, prefix + "driver", getDefaultDriver(type)));
        
        config.setMaxPoolSize(getIntProperty(props, prefix + "pool.maxSize", 10));
        config.setMinIdle(getIntProperty(props, prefix + "pool.minIdle", 2));
        config.setConnectionTimeout(getIntProperty(props, prefix + "pool.connectionTimeout", 30000));
        config.setIdleTimeout(getIntProperty(props, prefix + "pool.idleTimeout", 600000));
        config.setMaxLifetime(getIntProperty(props, prefix + "pool.maxLifetime", 1800000));
        config.setLeakDetectionThreshold(getIntProperty(props, prefix + "pool.leakDetection", 60000));
        
        return config;
    }
    
    /**
     * 从环境变量覆盖配置
     */
    private void overrideFromEnvironment(Properties props) {
        for (DataSourceType type : DataSourceType.values()) {
            String envPrefix = "DATASOURCE_" + type.getCode().toUpperCase() + "_";
            
            overrideFromEnv(props, "datasource." + type.getCode() + ".url", envPrefix + "URL");
            overrideFromEnv(props, "datasource." + type.getCode() + ".username", envPrefix + "USERNAME");
            overrideFromEnv(props, "datasource." + type.getCode() + ".password", envPrefix + "PASSWORD");
            overrideFromEnv(props, "datasource." + type.getCode() + ".driver", envPrefix + "DRIVER");
            overrideFromEnv(props, "datasource." + type.getCode() + ".enabled", envPrefix + "ENABLED");
        }
    }
    
    private void overrideFromEnv(Properties props, String propKey, String envKey) {
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            props.setProperty(propKey, envValue);
        }
    }
    
    private String getProperty(Properties props, String key, String defaultValue) {
        return System.getProperty(key, props.getProperty(key, defaultValue));
    }
    
    private int getIntProperty(Properties props, String key, int defaultValue) {
        try {
            String value = getProperty(props, key, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取默认数据库URL
     */
    private String getDefaultUrl(DataSourceType type) {
        switch (type) {
            case MYSQL_PRIMARY:
                return "jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC";
            case MYSQL_READONLY:
                return "jdbc:mysql://localhost:3307/test?useSSL=false&serverTimezone=UTC";
             case POSTGRESQL:
                 return "jdbc:postgresql://localhost:5432/test";
            case ORACLE:
                return "jdbc:oracle:thin:@localhost:1521:XE";
            // case ANALYTICS_DB:
            //     return "jdbc:mysql://localhost:3308/analytics?useSSL=false&serverTimezone=UTC";
            // case LOG_DB:
            //     return "jdbc:mysql://localhost:3309/logs?useSSL=false&serverTimezone=UTC";
            default:
                return "jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC";
        }
    }
    
    /**
     * 获取默认驱动
     */
    private String getDefaultDriver(DataSourceType type) {
        switch (type) {
            case MYSQL_PRIMARY:
            case MYSQL_READONLY:
            // case ANALYTICS_DB:
            // case LOG_DB:
            //     return "com.mysql.cj.jdbc.Driver";
             case POSTGRESQL:
                 return "org.postgresql.Driver";
            case ORACLE:
                return "oracle.jdbc.driver.OracleDriver";
            default:
                return "com.mysql.cj.jdbc.Driver";
        }
    }
    
    /**
     * 创建默认配置
     */
    private DataSourceConfiguration createDefaultConfig() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setUrl("jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC");
        config.setUsername("root");
        config.setPassword("root");
        config.setDriver("com.mysql.cj.jdbc.Driver");
        return config;
    }
    
    /**
     * 获取指定数据源的配置
     */
    public DataSourceConfiguration getConfiguration(DataSourceType type) {
        return configurations.get(type);
    }
    
    /**
     * 获取所有配置的数据源类型
     */
    public DataSourceType[] getConfiguredDataSourceTypes() {
        return configurations.keySet().toArray(new DataSourceType[0]);
    }
    
    /**
     * 检查数据源是否已配置
     */
    public boolean isConfigured(DataSourceType type) {
        return configurations.containsKey(type);
    }
    
    /**
     * 打印所有数据源配置
     */
    public void printConfigurations() {
        System.out.println("=== 多数据源配置信息 ===");
        configurations.forEach((type, config) -> {
            System.out.println(type + ": " + config);
        });
        System.out.println("=====================");
    }
}