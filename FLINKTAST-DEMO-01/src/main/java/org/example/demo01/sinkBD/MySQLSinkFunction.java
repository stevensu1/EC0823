package org.example.demo01.sinkBD;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.example.demo01.MyRecord;
import org.example.demo01.bak.DatabaseConnectionManager;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MySQLSinkFunction<T extends MyRecord> extends RichSinkFunction<T> {

    private transient DatabaseConnectionManager dbManager; // 标记为 transient
    private PreparedStatement preparedStatement;
    private Connection connection;
    private int batchSize = 0;
    private final int FLUSH_SIZE = 100; // 每 100 条 flush 一次
    private final Class<T> recordClass;

    public MySQLSinkFunction(Class<T> recordClass) {
        this.recordClass = recordClass;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // 获取数据库连接管理器
        dbManager = DatabaseConnectionManager.getInstance();
        // 从连接池获取连接
        this.connection = dbManager.getConnection();
        this.connection.setAutoCommit(false); // 手动提交事务
        String sql = getSqlFromAnnotation(this.recordClass);
        this.preparedStatement = connection.prepareStatement(sql);
    }

    private String getSqlFromAnnotation(Class<T> clazz) {
        try {
            // 检查类上是否有 SQLParameterSqlStr 注解
            if (clazz.isAnnotationPresent(SQLParameterSqlStr.class)) {
                SQLParameterSqlStr annotation = clazz.getAnnotation(SQLParameterSqlStr.class);
                String sql = annotation.name();
                if (sql != null && !sql.isEmpty()) {
                    return sql;
                }
            }
            throw new RuntimeException("获取SQL为空");
        } catch (Exception e) {
            throw new RuntimeException("获取SQL失败", e);
        }
    }

    @Override
    public void invoke(T record, Context context) throws Exception {
        setParametersViaAnnotation(preparedStatement, record);

        preparedStatement.addBatch();
        batchSize++;
        if (batchSize >= FLUSH_SIZE) {
            flush();
        }
    }

    private void flush() throws Exception {
        try {
            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            connection.commit(); // 提交事务
            batchSize = 0;
        } catch (Exception e) {
            connection.rollback(); // 回滚事务
            throw e;
        }
    }

    @Override
    public void close() throws Exception {
        try {
            if (batchSize > 0) {
                flush(); // 处理剩余数据
            }
        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            // 关闭语句和连接，连接会自动返回到连接池
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close(); // HikariCP会自动归还连接到池中
            }
        }
    }

    //  基于注解的参数设置方法
    private void setParametersViaAnnotation(PreparedStatement ps, T record) throws Exception {
        Class<?> clazz = record.getClass();
        Field[] fields = clazz.getDeclaredFields();

        // 创建索引到字段的映射
        Map<Integer, Field> indexFieldMap = new HashMap<>();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            SQLParameter annotation = field.getAnnotation(SQLParameter.class);
            if (annotation != null && annotation.index() > 0) {
                indexFieldMap.put(annotation.index(), field);
            }
        }

        // 按索引顺序设置参数
        for (int i = 1; i <= indexFieldMap.size(); i++) {
            Field field = indexFieldMap.get(i);
            if (field != null) {
                Object value = getFieldValue(field, record);

                if (value == null) {
                    ps.setNull(i, getSQLType(field.getType()));
                } else {
                    setParameterByType(ps, i, value);
                }
            }
        }
    }

    // 安全的字段值获取方法
    private Object getFieldValue(Field field, T record) throws Exception {
        try {
            // 尝试使用标准的反射访问
            if (!field.canAccess(record)) {
                field.setAccessible(true);
            }
            return field.get(record);
        } catch (InaccessibleObjectException e) {
            // 如果反射被限制，尝试使用getter方法
            return getValueViaGetter(field, record);
        }
    }

    // 通过getter方法获取值（适用于Lombok生成的getter）
    private Object getValueViaGetter(Field field, T record) throws Exception {
        String fieldName = field.getName();
        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        
        try {
            java.lang.reflect.Method getter = record.getClass().getMethod(getterName);
            return getter.invoke(record);
        } catch (NoSuchMethodException e) {
            // 尝试boolean类型的is方法
            if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                String isGetterName = "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                java.lang.reflect.Method isGetter = record.getClass().getMethod(isGetterName);
                return isGetter.invoke(record);
            }
            throw new RuntimeException("Cannot access field: " + fieldName + " and no getter method found", e);
        }
    }


    // 根据Java类型获取对应的SQL类型
    private int getSQLType(Class<?> type) {
        if (type == String.class) {
            return Types.VARCHAR;
        } else if (type == Integer.class || type == int.class) {
            return Types.INTEGER;
        } else if (type == Long.class || type == long.class) {
            return Types.BIGINT;
        } else if (type == Double.class || type == double.class) {
            return Types.DOUBLE;
        } else if (type == Float.class || type == float.class) {
            return Types.FLOAT;
        } else if (type == Boolean.class || type == boolean.class) {
            return Types.BOOLEAN;
        } else if (type == java.util.Date.class) {
            return Types.TIMESTAMP;
        } else {
            return Types.VARCHAR;
        }
    }

    private void setParameterByType(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value instanceof String) {
            ps.setString(index, (String) value);
        } else if (value instanceof Integer) {
            ps.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            ps.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            ps.setDouble(index, (Double) value);
        } else if (value instanceof Float) {
            ps.setFloat(index, (Float) value);
        } else if (value instanceof Boolean) {
            ps.setBoolean(index, (Boolean) value);
        } else if (value instanceof java.util.Date) {
            ps.setTimestamp(index, new Timestamp(((java.util.Date) value).getTime()));
        } else {
            ps.setString(index, value.toString());
        }
    }
}
