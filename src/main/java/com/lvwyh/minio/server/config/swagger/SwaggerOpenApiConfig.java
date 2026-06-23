package com.lvwyh.minio.server.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 配置。
 */
@Configuration
public class SwaggerOpenApiConfig {

    /**
     * OpenAPI 基础信息。
     */
    @Bean
    public OpenAPI minioServerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MinIO File Center API")
                        .description("MinIO 文件中心接口文档")
                        .version("1.0.0"))
                .components(new Components());
    }
}
