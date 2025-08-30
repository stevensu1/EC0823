package org.example02.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.mybatisflex.core.MybatisFlexBootstrap;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 数据源配置类
 * 用于配置和初始化数据库连接池以及MyBatis-Flex
 */
@Slf4j
public class DataSourceConfig {
    // 在 DataSourceConfig 类中修改数据库连接URL
    static String jdbcUrl = "jdbc:mysql://localhost:3306/flink_demo?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";

    private static final String JDBC_URL = System.getProperty("db.url", jdbcUrl);
    private static final String USERNAME = System.getProperty("db.username", "root");
    private static final String PASSWORD = System.getProperty("db.password", "root");
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    
    private static DataSource dataSource;
    private static MybatisFlexBootstrap bootstrap;
    
    /**
     * 获取数据源实例（单例模式）
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DataSourceConfig.class) {
                if (dataSource == null) {
                    dataSource = createDataSource();
                }
            }
        }
        return dataSource;
    }
    
    /**
     * 创建数据源
     */
    private static DataSource createDataSource() {
        log.info("正在初始化数据源...");
        log.info("数据库URL: {}", JDBC_URL);
        log.info("用户名: {}", USERNAME);
        
        DruidDataSource druidDataSource = new DruidDataSource();
        
        // 基本配置
        druidDataSource.setUrl(JDBC_URL);
        druidDataSource.setUsername(USERNAME);
        druidDataSource.setPassword(PASSWORD);
        druidDataSource.setDriverClassName(DRIVER_CLASS);
        
        // 连接池配置
        druidDataSource.setInitialSize(2);           // 初始连接数（降低以减少启动压力）
        druidDataSource.setMinIdle(1);               // 最小空闲连接数
        druidDataSource.setMaxActive(10);            // 最大连接数（降低以节省资源）
        druidDataSource.setMaxWait(30000);           // 获取连接最大等待时间，单位毫秒（降低等待时间）
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000); // 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        druidDataSource.setMinEvictableIdleTimeMillis(300000);   // 配置一个连接在池中最小生存的时间，单位是毫秒
        
        // 验证配置
        druidDataSource.setValidationQuery("SELECT 1");
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        
        // 连接失败重试配置
        druidDataSource.setConnectionErrorRetryAttempts(3);
        druidDataSource.setBreakAfterAcquireFailure(false);
        
        try {
            log.info("尝试初始化数据源...");
            druidDataSource.init();
            
            // 测试连接
            try (var connection = druidDataSource.getConnection()) {
                log.info("数据库连接测试成功");
                try (var stmt = connection.createStatement()) {
                    var rs = stmt.executeQuery("SELECT 1 as test");
                    if (rs.next()) {
                        log.info("数据库查询测试成功，结果: {}", rs.getInt("test"));
                    }
                }
            }
            
            log.info("数据源初始化成功");
        } catch (SQLException e) {
            log.error("数据源初始化失败，错误信息: {}", e.getMessage());
            log.error("可能的原因:");
            log.error("1. MySQL服务未启动");
            log.error("2. 数据库连接参数错误");
            log.error("3. 防火墙阻止连接");
            log.error("4. 数据库flink_demo不存在");
            log.error("5. 用户权限不足");
            log.warn("将抛出运行时异常，应用可能会降级运行");
            throw new RuntimeException("数据源初始化失败: " + e.getMessage(), e);
        }
        
        return druidDataSource;
    }
    
    /**
     * 获取MyBatis-Flex Bootstrap实例
     */
    public static MybatisFlexBootstrap getBootstrap() {
        if (bootstrap == null) {
            synchronized (DataSourceConfig.class) {
                if (bootstrap == null) {
                    bootstrap = MybatisFlexBootstrap.getInstance()
                            .setDataSource(getDataSource())
                            .addMapper(org.example02.mapper.UserMapper.class)
                            .start();
                    log.info("MyBatis-Flex 初始化成功");
                }
            }
        }
        return bootstrap;
    }
    
    /**
     * 关闭数据源
     */
    public static void close() {
        if (dataSource instanceof DruidDataSource) {
            ((DruidDataSource) dataSource).close();
            log.info("数据源已关闭");
        }
    }
}