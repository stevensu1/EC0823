package org.example.demo01.bak;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 数据库配置管理类
 * 支持从配置文件、环境变量、系统属性中读取配置
 */
public class DatabaseConfig {
    
    private static final String CONFIG_FILE = "database.properties";
    
    // 配置键名常量
    public static final String DB_URL_KEY = "database.url";
    public static final String DB_USERNAME_KEY = "database.username";
    public static final String DB_PASSWORD_KEY = "database.password";
    public static final String DB_DRIVER_KEY = "database.driver";
    
    // 连接池配置键名
    public static final String POOL_MAX_SIZE_KEY = "database.pool.maxSize";
    public static final String POOL_MIN_IDLE_KEY = "database.pool.minIdle";
    public static final String POOL_CONNECTION_TIMEOUT_KEY = "database.pool.connectionTimeout";
    public static final String POOL_IDLE_TIMEOUT_KEY = "database.pool.idleTimeout";
    public static final String POOL_MAX_LIFETIME_KEY = "database.pool.maxLifetime";
    public static final String POOL_LEAK_DETECTION_KEY = "database.pool.leakDetection";
    
    // 默认配置值
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_DB_USERNAME = "root";
    private static final String DEFAULT_DB_PASSWORD = "root";
    private static final String DEFAULT_DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    private static final int DEFAULT_MAX_SIZE = 10;
    private static final int DEFAULT_MIN_IDLE = 2;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    private static final int DEFAULT_IDLE_TIMEOUT = 600000;
    private static final int DEFAULT_MAX_LIFETIME = 1800000;
    private static final int DEFAULT_LEAK_DETECTION = 60000;
    
    private Properties properties;
    
    public DatabaseConfig() {
        this.properties = loadConfiguration();
    }
    
    /**
     * 加载配置，优先级：系统属性 > 环境变量 > 配置文件 > 默认值
     */
    private Properties loadConfiguration() {
        Properties config = new Properties();
        
        // 1. 加载默认配置
        loadDefaultConfig(config);
        
        // 2. 从配置文件加载
        loadFromFile(config);
        
        // 3. 从环境变量加载
        loadFromEnvironment(config);
        
        // 4. 从系统属性加载（最高优先级）
        loadFromSystemProperties(config);
        
        return config;
    }
    
    /**
     * 加载默认配置
     */
    private void loadDefaultConfig(Properties config) {
        config.setProperty(DB_URL_KEY, DEFAULT_DB_URL);
        config.setProperty(DB_USERNAME_KEY, DEFAULT_DB_USERNAME);
        config.setProperty(DB_PASSWORD_KEY, DEFAULT_DB_PASSWORD);
        config.setProperty(DB_DRIVER_KEY, DEFAULT_DB_DRIVER);
        
        config.setProperty(POOL_MAX_SIZE_KEY, String.valueOf(DEFAULT_MAX_SIZE));
        config.setProperty(POOL_MIN_IDLE_KEY, String.valueOf(DEFAULT_MIN_IDLE));
        config.setProperty(POOL_CONNECTION_TIMEOUT_KEY, String.valueOf(DEFAULT_CONNECTION_TIMEOUT));
        config.setProperty(POOL_IDLE_TIMEOUT_KEY, String.valueOf(DEFAULT_IDLE_TIMEOUT));
        config.setProperty(POOL_MAX_LIFETIME_KEY, String.valueOf(DEFAULT_MAX_LIFETIME));
        config.setProperty(POOL_LEAK_DETECTION_KEY, String.valueOf(DEFAULT_LEAK_DETECTION));
    }
    
    /**
     * 从配置文件加载
     */
    private void loadFromFile(Properties config) {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                Properties fileProps = new Properties();
                fileProps.load(input);
                config.putAll(fileProps);
                System.out.println("已加载数据库配置文件: " + CONFIG_FILE);
            } else {
                System.out.println("配置文件不存在，使用默认配置: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("读取配置文件失败，使用默认配置: " + e.getMessage());
        }
    }
    
    /**
     * 从环境变量加载
     */
    private void loadFromEnvironment(Properties config) {
        // 数据库连接配置
        overrideFromEnv(config, DB_URL_KEY, "DATABASE_URL");
        overrideFromEnv(config, DB_USERNAME_KEY, "DATABASE_USERNAME");
        overrideFromEnv(config, DB_PASSWORD_KEY, "DATABASE_PASSWORD");
        overrideFromEnv(config, DB_DRIVER_KEY, "DATABASE_DRIVER");
        
        // 连接池配置
        overrideFromEnv(config, POOL_MAX_SIZE_KEY, "DATABASE_POOL_MAX_SIZE");
        overrideFromEnv(config, POOL_MIN_IDLE_KEY, "DATABASE_POOL_MIN_IDLE");
        overrideFromEnv(config, POOL_CONNECTION_TIMEOUT_KEY, "DATABASE_POOL_CONNECTION_TIMEOUT");
        overrideFromEnv(config, POOL_IDLE_TIMEOUT_KEY, "DATABASE_POOL_IDLE_TIMEOUT");
        overrideFromEnv(config, POOL_MAX_LIFETIME_KEY, "DATABASE_POOL_MAX_LIFETIME");
        overrideFromEnv(config, POOL_LEAK_DETECTION_KEY, "DATABASE_POOL_LEAK_DETECTION");
    }
    
    /**
     * 从系统属性加载
     */
    private void loadFromSystemProperties(Properties config) {
        for (String key : config.stringPropertyNames()) {
            String systemValue = System.getProperty(key);
            if (systemValue != null) {
                config.setProperty(key, systemValue);
                System.out.println("使用系统属性覆盖配置: " + key + " = " + systemValue);
            }
        }
    }
    
    /**
     * 从环境变量覆盖配置
     */
    private void overrideFromEnv(Properties config, String configKey, String envKey) {
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            config.setProperty(configKey, envValue);
            System.out.println("使用环境变量覆盖配置: " + configKey + " = " + envValue);
        }
    }
    
    // Getter 方法
    public String getDbUrl() {
        return properties.getProperty(DB_URL_KEY);
    }
    
    public String getDbUsername() {
        return properties.getProperty(DB_USERNAME_KEY);
    }
    
    public String getDbPassword() {
        return properties.getProperty(DB_PASSWORD_KEY);
    }
    
    public String getDbDriver() {
        return properties.getProperty(DB_DRIVER_KEY);
    }
    
    public int getMaxPoolSize() {
        return Integer.parseInt(properties.getProperty(POOL_MAX_SIZE_KEY));
    }
    
    public int getMinIdle() {
        return Integer.parseInt(properties.getProperty(POOL_MIN_IDLE_KEY));
    }
    
    public int getConnectionTimeout() {
        return Integer.parseInt(properties.getProperty(POOL_CONNECTION_TIMEOUT_KEY));
    }
    
    public int getIdleTimeout() {
        return Integer.parseInt(properties.getProperty(POOL_IDLE_TIMEOUT_KEY));
    }
    
    public int getMaxLifetime() {
        return Integer.parseInt(properties.getProperty(POOL_MAX_LIFETIME_KEY));
    }
    
    public int getLeakDetectionThreshold() {
        return Integer.parseInt(properties.getProperty(POOL_LEAK_DETECTION_KEY));
    }
    
    /**
     * 打印当前配置信息（不包含敏感信息）
     */
    public void printConfiguration() {
        System.out.println("=== 数据库配置信息 ===");
        System.out.println("数据库URL: " + getDbUrl());
        System.out.println("数据库用户名: " + getDbUsername());
        System.out.println("数据库密码: " + maskPassword(getDbPassword()));
        System.out.println("数据库驱动: " + getDbDriver());
        System.out.println("连接池最大连接数: " + getMaxPoolSize());
        System.out.println("连接池最小空闲连接数: " + getMinIdle());
        System.out.println("连接超时时间: " + getConnectionTimeout() + "ms");
        System.out.println("空闲超时时间: " + getIdleTimeout() + "ms");
        System.out.println("连接最大生命周期: " + getMaxLifetime() + "ms");
        System.out.println("连接泄漏检测阈值: " + getLeakDetectionThreshold() + "ms");
        System.out.println("======================");
    }
    
    /**
     * 掩码显示密码
     */
    private String maskPassword(String password) {
        if (password == null || password.length() <= 2) {
            return "***";
        }
        return password.substring(0, 1) + "***" + password.substring(password.length() - 1);
    }
}