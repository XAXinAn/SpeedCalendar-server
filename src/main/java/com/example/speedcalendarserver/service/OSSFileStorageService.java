package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 阿里云OSS文件存储服务实现
 * 用于生产环境
 *
 * TODO: 生产环境需要完整实现此类
 * TODO: 添加阿里云OSS SDK依赖到pom.xml
 * <dependency>
 *     <groupId>com.aliyun.oss</groupId>
 *     <artifactId>aliyun-sdk-oss</artifactId>
 *     <version>3.17.1</version>
 * </dependency>
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.type", havingValue = "oss")
public class OSSFileStorageService implements FileStorageService {

    private final FileStorageConfig config;

    // TODO: 生产环境需要注入OSS客户端
    // private OSS ossClient;

    @Override
    public String uploadAvatar(MultipartFile file, String userId) throws Exception {
        // TODO: 生产环境实现OSS上传逻辑
        /*
        示例代码：
        1. 验证文件
        validateFile(file);

        2. 生成OSS文件路径
        String objectKey = "avatars/" + userId + "_" + System.currentTimeMillis() + extension;

        3. 上传到OSS
        ossClient.putObject(config.getOss().getBucket(), objectKey, file.getInputStream());

        4. 返回访问URL
        if (config.getOss().getCdnDomain() != null) {
            return config.getOss().getCdnDomain() + "/" + objectKey;
        } else {
            return "https://" + config.getOss().getBucket() + "." +
                   config.getOss().getEndpoint() + "/" + objectKey;
        }
        */

        log.warn("【OSS上传】OSS服务未实现，请在生产环境完成实现");
        throw new UnsupportedOperationException("OSS服务未实现，请配置 file.storage.type=local 或实现OSS上传逻辑");
    }

    @Override
    public void deleteAvatar(String fileUrl) throws Exception {
        // TODO: 生产环境实现OSS删除逻辑
        /*
        示例代码：
        1. 从URL提取objectKey
        String objectKey = extractObjectKeyFromUrl(fileUrl);

        2. 删除OSS对象
        ossClient.deleteObject(config.getOss().getBucket(), objectKey);
        */

        log.warn("【OSS删除】OSS服务未实现");
    }

    @Override
    public void validateFile(MultipartFile file) throws IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > config.getAvatar().getMaxSize()) {
            long maxSizeMB = config.getAvatar().getMaxSize() / 1024 / 1024;
            throw new IllegalArgumentException("文件大小不能超过 " + maxSizeMB + "MB");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("无效的文件名");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList(config.getAvatar().getAllowedTypes()).contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        // 验证MIME类型
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(config.getAvatar().getAllowedMimeTypes()).contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件格式");
        }
    }

    // TODO: 生产环境需要初始化OSS客户端
    /*
    @PostConstruct
    public void init() {
        this.ossClient = new OSSClientBuilder().build(
            config.getOss().getEndpoint(),
            config.getOss().getAccessKey(),
            config.getOss().getSecretKey()
        );
        log.info("【OSS客户端】初始化成功");
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("【OSS客户端】已关闭");
        }
    }
    */
}
