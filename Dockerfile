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
COPY docs /app/docs
RUN mvn -q -B -s /root/.m2/settings.xml clean package -DskipTests

# =========================
# Runtime stage
# =========================
FROM crpi-v2fmzydhnzmlpzjc.cn-shanghai.personal.cr.aliyuncs.com/machenkai/eclipse-temurin:8-jre

WORKDIR /opt/reviewx-server

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS=""

# 服务端口来自 application.yml：server.port=${PORT:58080}
ENV PORT=58080

COPY --from=builder /app/target/reviewx-server-1.0-SNAPSHOT.jar /opt/reviewx-server/app.jar

# 导入题目接口会读取 docs/识别结果id输出.zip，因此运行镜像中保留 docs 目录。
COPY --from=builder /app/docs /opt/reviewx-server/docs

LABEL org.opencontainers.image.title="reviewx-server" \
      org.opencontainers.image.description="ReviewX question practice backend service" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.authors="mack"

EXPOSE 58080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/reviewx-server/app.jar"]
