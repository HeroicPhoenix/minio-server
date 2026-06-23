# =========================
# Build stage
# =========================
FROM crpi-v2fmzydhnzmlpzjc.cn-shanghai.personal.cr.aliyuncs.com/machenkai/maven:3.8.8-eclipse-temurin-8 AS builder

WORKDIR /app

# 写入 Maven 镜像配置，提升国内环境依赖下载稳定性。
RUN set -eux; \
    mkdir -p /root/.m2; \
    printf '%s\n' \
    '<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"' \
    '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' \
    '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">' \
    '  <mirrors>' \
    '    <mirror>' \
    '      <id>aliyun-public</id>' \
    '      <mirrorOf>*</mirrorOf>' \
    '      <name>Aliyun Public Mirror</name>' \
    '      <url>https://maven.aliyun.com/repository/public</url>' \
    '    </mirror>' \
    '  </mirrors>' \
    '</settings>' \
    > /root/.m2/settings.xml

# 先复制 pom 并预下载依赖，充分利用 Docker 构建缓存。
COPY pom.xml /app/pom.xml
RUN mvn -q -B -s /root/.m2/settings.xml dependency:go-offline -DskipTests

# 复制源码和资源并构建当前单模块 Spring Boot 应用。
COPY src /app/src
RUN mvn -q -B -s /root/.m2/settings.xml clean package -DskipTests

# =========================
# Runtime stage
# =========================
FROM crpi-v2fmzydhnzmlpzjc.cn-shanghai.personal.cr.aliyuncs.com/machenkai/eclipse-temurin:8-jre

WORKDIR /opt/minio-server

COPY --from=builder /app/target/minio-server-1.0-SNAPSHOT.jar /opt/minio-server/app.jar

LABEL org.opencontainers.image.title="minio-server" \
      org.opencontainers.image.description="MinIO file center backend service" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.authors="mack"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/minio-server/app.jar"]
