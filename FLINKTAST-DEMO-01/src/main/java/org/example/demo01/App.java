package org.example.demo01;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SocketTextStreamFunction;
import org.apache.flink.util.Collector;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 设置为流处理模式
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);

        // 基本配置
        env.setParallelism(1); // 设置并行度为1
        env.disableOperatorChaining(); // 禁用算子链，使执行更清晰

        // 禁用检查点，因为是简单的演示程序
        env.getCheckpointConfig().disableCheckpointing();

        // 创建周期性的数据源
//        DataStream<String> dataStream = env
//                .socketTextStream("localhost", 9999) // 从socket读取数据
//                .name("source-strings")
//                .setParallelism(1);

        DataStream<String> dataStream = env.addSource(new SocketTextStreamFunction("192.168.0.39", 9999, "\n", 0))
                .name("socket-source");


        // 转换算子 keyBy: 按单词分组并计数
        dataStream.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String line, Collector<Tuple2<String, Integer>> out) {
                        for (String word : line.split(" ")) {
                            out.collect(new Tuple2<>(word, 1));
                        }
                    }
                }).name("flatmap-split-words")
                .setParallelism(1)
                .keyBy(tuple -> tuple.f0) // 按单词分组
                .sum(1) // 计算每个单词的出现次数
                .print()
                .name("printer-word-count");

        // 执行任务
        env.execute("Flink Streaming Java API Hello");
    }
}
