package com.lvwyh.minio.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MinIO 文件中心服务启动入口。
 */
@SpringBootApplication
public class MinioWebApplication {

    /**
     * JVM 主入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MinioWebApplication.class, args);
    }
}
