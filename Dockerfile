# =============================================
# SpeedCalendar 多阶段构建 Dockerfile
# 参考: https://spring.io/guides/gs/spring-boot-docker
#       https://docs.docker.com/reference/dockerfile/
# =============================================

# 阶段一: 构建阶段 (使用 Maven 构建 JAR)
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# 复制 Maven 配置文件（利用 Docker 缓存层）
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 下载依赖（单独一层，便于缓存）
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# 复制源代码
COPY src src

# 构建项目（跳过测试加快构建速度）
RUN ./mvnw package -DskipTests -B

# 解压 JAR 以便分层（提高镜像缓存效率）
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# =============================================
# 阶段二: 运行阶段 (精简镜像)
# =============================================
FROM eclipse-temurin:17-jre-alpine

# 创建非 root 用户运行应用（安全最佳实践）
RUN addgroup -S spring && adduser -S spring -G spring

# 设置工作目录
WORKDIR /app

# 从构建阶段复制解压后的依赖（分层缓存）
COPY --from=builder /app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=builder /app/target/dependency/META-INF /app/META-INF
COPY --from=builder /app/target/dependency/BOOT-INF/classes /app

# 创建上传目录
RUN mkdir -p /data/speedcalendar/uploads/avatars && \
    mkdir -p /data/logs && \
    chown -R spring:spring /data

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
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp app:app/lib/* com.example.speedcalendarserver.SpeedCalendarServerApplication"]
