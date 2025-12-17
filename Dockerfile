# =============================================
# SpeedCalendar 多阶段构建 Dockerfile
# 参考: https://spring.io/guides/gs/spring-boot-docker
#       https://docs.docker.com/reference/dockerfile/
# =============================================

# 阶段一: 构建阶段 (使用 Maven 构建 JAR)
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 复制 Maven 配置文件（利用 Docker 缓存层）
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 配置 Maven 使用阿里云镜像（国内加速）
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">\
    <mirrors>\
    <mirror>\
    <id>aliyunmaven</id>\
    <mirrorOf>*</mirrorOf>\
    <name>阿里云公共仓库</name>\
    <url>https://maven.aliyun.com/repository/public</url>\
    </mirror>\
    </mirrors>\
    </settings>' > /root/.m2/settings.xml

# 下载依赖（单独一层，便于缓存）
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# 复制源代码
COPY src src

# 构建项目（跳过测试加快构建速度）
RUN ./mvnw package -DskipTests -B

# =============================================
# 阶段二: 运行阶段 (精简镜像)
# =============================================
FROM eclipse-temurin:21-jre-alpine

# 设置时区为上海（北京时间）
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建非 root 用户运行应用（安全最佳实践）
RUN addgroup -S spring && adduser -S spring -G spring

# 设置工作目录
WORKDIR /app

# 直接复制 JAR 文件（简单可靠）
COPY --from=builder /app/target/*.jar app.jar

# 创建上传目录
RUN mkdir -p /data/speedcalendar/uploads/avatars && \
    mkdir -p /data/logs && \
    chown -R spring:spring /data /app

# 切换到非 root 用户
USER spring:spring

# 暴露端口
EXPOSE 8080

# JVM 优化参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/auth/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
