package org.example.demo01.bak;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理工具类
 * 提供配置监听、热重载等高级功能
 */
public class ConfigurationManager {
    
    private static final Map<String, ConfigChangeListener> listeners = new ConcurrentHashMap<>();
    
    /**
     * 配置变更监听器接口
     */
    public interface ConfigChangeListener {
        void onConfigChanged(String key, String oldValue, String newValue);
    }
    
    /**
     * 注册配置变更监听器
     */
    public static void addConfigChangeListener(String key, ConfigChangeListener listener) {
        listeners.put(key, listener);
    }
    
    /**
     * 移除配置变更监听器
     */
    public static void removeConfigChangeListener(String key) {
        listeners.remove(key);
    }
    
    /**
     * 触发配置变更事件
     */
    public static void fireConfigChanged(String key, String oldValue, String newValue) {
        ConfigChangeListener listener = listeners.get(key);
        if (listener != null) {
            try {
                listener.onConfigChanged(key, oldValue, newValue);
            } catch (Exception e) {
                System.err.println("配置变更监听器执行失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 安全更新系统属性并触发监听器
     */
    public static void updateSystemProperty(String key, String value) {
        String oldValue = System.getProperty(key);
        System.setProperty(key, value);
        fireConfigChanged(key, oldValue, value);
        System.out.println("系统属性已更新: " + key + " = " + value);
    }
    
    /**
     * 获取所有活跃的监听器
     */
    public static String[] getActiveListeners() {
        return listeners.keySet().toArray(new String[0]);
    }
    
    /**
     * 打印配置管理状态
     */
    public static void printStatus() {
        System.out.println("=== 配置管理器状态 ===");
        System.out.println("活跃监听器数量: " + listeners.size());
        for (String key : listeners.keySet()) {
            System.out.println("监听配置项: " + key);
        }
        System.out.println("===================");
    }
}