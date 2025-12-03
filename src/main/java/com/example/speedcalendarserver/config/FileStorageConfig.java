package com.example.speedcalendarserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 *
 * TODO: 生产环境切换到OSS时需要修改配置
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {

    /**
     * 存储类型：local（本地存储） 或 oss（阿里云OSS）
     *
     * TODO: 生产环境改为 oss
     */
    private String type = "local";

    /**
     * 本地存储配置
     */
    private LocalConfig local = new LocalConfig();

    /**
     * OSS存储配置
     *
     * TODO: 生产环境需要配置OSS参数
     */
    private OssConfig oss = new OssConfig();

    /**
     * 头像配置
     */
    private AvatarConfig avatar = new AvatarConfig();

    /**
     * 图片压缩配置
     */
    private CompressionConfig compression = new CompressionConfig();

    /**
     * 本地存储配置
     */
    @Data
    public static class LocalConfig {
        /**
         * 上传目录
         *
         * TODO: 生产环境可能需要修改为绝对路径
         */
        private String uploadDir = "uploads/avatars";

        /**
         * 访问基础URL
         *
         * TODO: 生产环境改为实际域名 https://api.speedcalendar.com/api/files
         */
        private String baseUrl = "http://122.51.127.61:8080/api/files";
    }

    /**
     * 阿里云OSS配置
     *
     * TODO: 生产环境需要配置以下所有参数
     */
    @Data
    public static class OssConfig {
        /**
         * OSS endpoint
         * 例如: oss-cn-hangzhou.aliyuncs.com
         */
        private String endpoint;

        /**
         * OSS bucket名称
         * 例如: speedcalendar-avatars
         */
        private String bucket;

        /**
         * AccessKey ID
         * TODO: 生产环境使用环境变量或密钥管理服务
         */
        private String accessKey;

        /**
         * AccessKey Secret
         * TODO: 生产环境使用环境变量或密钥管理服务
         */
        private String secretKey;

        /**
         * CDN域名（可选）
         * 例如: https://cdn.speedcalendar.com
         */
        private String cdnDomain;
    }

    /**
     * 头像配置
     */
    @Data
    public static class AvatarConfig {
        /**
         * 最大文件大小（字节）
         * 默认5MB
         */
        private Long maxSize = 5 * 1024 * 1024L;

        /**
         * 允许的文件类型
         */
        private String[] allowedTypes = { "jpg", "jpeg", "png", "webp" };

        /**
         * 允许的MIME类型
         */
        private String[] allowedMimeTypes = { "image/jpeg", "image/png", "image/webp" };
    }

    /**
     * 图片压缩配置
     */
    @Data
    public static class CompressionConfig {
        /**
         * 是否启用压缩
         */
        private boolean enabled = true;

        /**
         * 最大宽度（像素）
         */
        private int maxWidth = 800;

        /**
         * 最大高度（像素）
         */
        private int maxHeight = 800;

        /**
         * 压缩质量 (0.0 - 1.0)
         */
        private double quality = 0.85;

        /**
         * 输出格式 (jpg/png)
         */
        private String outputFormat = "jpg";
    }
}
