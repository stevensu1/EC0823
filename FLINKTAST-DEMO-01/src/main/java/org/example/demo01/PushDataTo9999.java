package org.example.demo01;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PushDataTo9999 {
    private static final String HOST = "192.168.0.39";
    private static final int PORT = 9999;
    private static final String DATA = "test flink window hallo word";

    public static void main(String[] args) {
        try {
            System.out.println("Connecting to " + HOST + ":" + PORT);

            // 创建到WSL的连接
            try (Socket socket = new Socket(HOST, PORT);
                 OutputStream outputStream = socket.getOutputStream()) {

                System.out.println("Connected to " + HOST + ":" + PORT);

                // 持续发送数据
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    // 获取当前系统时间
                    String currentTime = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    // 每秒发送一次带时间戳的数据
                    String dataToSend = DATA + " " + currentTime + "\n";
                    outputStream.write(dataToSend.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    System.out.println("Sent: " + dataToSend.trim());

                    // 等待1秒
                    Thread.sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
