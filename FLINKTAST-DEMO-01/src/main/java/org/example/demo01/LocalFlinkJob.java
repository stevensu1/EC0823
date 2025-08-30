package org.example.demo01;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


/**
 * 这使用一个flink执行环境，用于在一个执行环境中加载多个flink任务（使用不同的数据源
 *         LocalFlinkJob1.task(env);
 *         LocalFlinkJob2.task(env);
 *         ）
 */
public class LocalFlinkJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(8);

        // ✅ 开启 Checkpointing，支持 Exactly-Once
        env.enableCheckpointing(5000); // 每 5 秒做一次 checkpoint
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000); // 避免频繁 checkpoint

        // 不再在主类中初始化 dbManager，避免序列化问题
        LocalFlinkJob1.task(env);
        LocalFlinkJob2.task(env);

        // ✅ 启动执行
        env.execute("Production JDBC Write Job");
    }

}