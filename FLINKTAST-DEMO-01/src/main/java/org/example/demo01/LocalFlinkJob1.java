package org.example.demo01;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.example.demo01.sinkBD.MySQLSinkFunction;
import org.example.demo01.sourceDB.DataSourceType;
import org.example.demo01.sourceDB.MultiDataSourceConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LocalFlinkJob1 {

    public static void task(StreamExecutionEnvironment env) throws Exception {
        DataStreamSource<MyRecordOne> dataStreamSource = env.addSource(
                new SourceFunction<MyRecordOne>() {
                    private volatile boolean isRunning = true;
                    private transient MultiDataSourceConnectionManager dbManager; // 标记为 transient

                    @Override
                    public void run(SourceContext<MyRecordOne> ctx) throws Exception {
                        // 在运行时初始化，避免序列化问题
                        dbManager = MultiDataSourceConnectionManager.getInstance();

                        while (isRunning) {
                            Connection connection = null;
                            try {
                                // 明确指定使用主MySQL数据源，确保类型安全
                                connection = dbManager.getConnection(DataSourceType.MYSQL_PRIMARY);

                                PreparedStatement statement = connection.prepareStatement(
                                        "SELECT id, name, value FROM my_record"
                                );
                                ResultSet resultSet = statement.executeQuery();

                                while (resultSet.next() && isRunning) {
                                    MyRecordOne record = new MyRecordOne(
                                            resultSet.getInt("id"),
                                            resultSet.getString("name"),
                                            resultSet.getString("value")
                                    );
                                    ctx.collect(record);
                                }

                                resultSet.close();
                                statement.close();

                            } catch (Exception e) {
                                System.err.println("数据库查询异常: " + e.getMessage());
                                throw new RuntimeException(e);
                            } finally {
                                if (connection != null) {
                                    connection.close(); // HikariCP会自动归还连接到池中
                                }
                            }

                            // 等待 5 秒再次查询
                            //  TimeUnit.SECONDS.sleep(5);
                        }
                    }

                    @Override
                    public void cancel() {
                        isRunning = false;
                    }
                }
                , TypeInformation.of(MyRecordOne.class)
        );

        // ✅ 打印验证（可选）
        dataStreamSource.print();
        dataStreamSource.addSink(new MySQLSinkFunction<>(MyRecordOne.class)).setParallelism(8);

    }


}
