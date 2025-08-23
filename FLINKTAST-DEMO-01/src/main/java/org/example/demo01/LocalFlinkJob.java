package org.example.demo01;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class LocalFlinkJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(8);

        // ✅ 开启 Checkpointing，支持 Exactly-Once
        env.enableCheckpointing(5000); // 每 5 秒做一次 checkpoint
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000); // 避免频繁 checkpoint


        // ✅ 数据源（测试用）
        DataStreamSource<MyRecordOne> dataStreamSource = env.fromElements(
                new MyRecordOne(1, "Alice", 100),
                new MyRecordOne(2, "Bob", 200),
                new MyRecordOne(3, "Charlie", 300)
        );

        // ✅ 打印验证（可选）
        dataStreamSource.print();
        dataStreamSource.addSink(new MySQLSinkFunction<>(MyRecordOne.class)).setParallelism(8);


        // ✅ 添加 JDBC Sink
//        dataStreamSource.addSink(
//                JdbcSink.sink(
//                        // SQL 语句（注意字段顺序）
//                        "INSERT INTO my_table (id, name, value) VALUES (?, ?, ?)",
//
//                        // 设置 PreparedStatement 参数
//                        (PreparedStatement ps, MyRecord record) -> {
//                            ps.setInt(1, record.getId());
//                            ps.setString(2, record.getName());
//                            ps.setInt(3, record.getValue());
//                        },
//
//                        // 执行选项：批处理配置
//                        JdbcExecutionOptions.builder()
//                                .withBatchSize(1000)               // 每批最多 1000 条
//                                .withBatchIntervalMs(2000)         // 每 2 秒 flush
//                                .withMaxRetries(3)                 // 重试 3 次
//                                .build(),
//
//                        // 连接选项
//                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
//                                .withUrl("jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC")
//                                .withDriverName("com.mysql.cj.jdbc.Driver")
//                                .withUsername("root")
//                                .withPassword("root")
//                                .withConnectionCheckTimeoutSeconds(30)
//                                .build()
//                )
//        );

        // ✅ 启动执行
        env.execute("Production JDBC Write Job");
    }

}