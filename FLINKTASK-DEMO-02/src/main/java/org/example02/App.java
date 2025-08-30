package org.example02;

import java.util.List;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.example02.config.DataSourceConfig;
import org.example02.entity.User;
import org.example02.service.UserService;


import lombok.extern.slf4j.Slf4j;

/**
 * Flink集成MyBatis-Flex示例应用
 */
@Slf4j
public class App {


    public static void main(String[] args) {
        try {
            // 初始化Flink环境
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            DataSourceConfig.getBootstrap();
            log.info("MyBatis-Flex初始化成功");

            // 创建用户服务
           UserService userService = new UserService();
            // 设置为流处理模式
            env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
            // 基本配置
            env.setParallelism(4); // 设置并行度为1
            env.disableOperatorChaining(); // 禁用算子链，使执行更清晰
            List<User> list = userService.getAllActiveUsers();

            env.fromCollection(list).print("source-data");

            env.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
