package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 * 仅用于开发环境
 *
 * TODO: 生产环境切换到OSSFileStorageService
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final FileStorageConfig config;

    @Override
    public String uploadAvatar(MultipartFile file, String userId) throws Exception {
        // 验证文件
        validateFile(file);

        // 创建上传目录
        Path uploadPath = Paths.get(config.getLocal().getUploadDir());
        log.info("【文件上传】配置的上传目录: {}", config.getLocal().getUploadDir());
        log.info("【文件上传】解析后的路径: {}", uploadPath.toAbsolutePath());

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("【文件上传】创建上传目录成功: {}", uploadPath.toAbsolutePath());
        } else {
            log.info("【文件上传】上传目录已存在: {}", uploadPath.toAbsolutePath());
        }

        // 生成唯一文件名（压缩后统一使用 .jpg 格式）
        String originalFilename = file.getOriginalFilename();
        FileStorageConfig.CompressionConfig compressionConfig = config.getCompression();
        String extension = compressionConfig.isEnabled()
                ? "." + compressionConfig.getOutputFormat()
                : (originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg");
        String filename = userId + "_" + System.currentTimeMillis() + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        log.info("【文件上传】准备保存到: {}", filePath.toAbsolutePath());
        log.info("【文件上传】原始文件大小: {} bytes", file.getSize());

        File targetFile = filePath.toFile();
        log.info("【文件上传】目标文件对象: {}", targetFile.getAbsolutePath());
        log.info("【文件上传】父目录是否存在: {}", targetFile.getParentFile().exists());
        log.info("【文件上传】父目录是否可写: {}", targetFile.getParentFile().canWrite());

        // 判断是否启用压缩
        if (compressionConfig.isEnabled()) {
            // 使用 Thumbnailator 压缩图片
            log.info("【图片压缩】开始压缩，目标尺寸: {}x{}, 质量: {}, 格式: {}",
                    compressionConfig.getMaxWidth(), compressionConfig.getMaxHeight(),
                    compressionConfig.getQuality(), compressionConfig.getOutputFormat());

            Thumbnails.of(file.getInputStream())
                    .size(compressionConfig.getMaxWidth(), compressionConfig.getMaxHeight())
                    .outputQuality(compressionConfig.getQuality())
                    .keepAspectRatio(true)
                    .outputFormat(compressionConfig.getOutputFormat())
                    .toFile(targetFile);

            long compressedSize = targetFile.length();
            double compressionRate = (1 - (double) compressedSize / file.getSize()) * 100;
            log.info("【图片压缩】压缩完成，压缩后大小: {} bytes", compressedSize);
            log.info("【图片压缩】压缩率: {}%", String.format("%.2f", compressionRate));
        } else {
            // 不压缩，直接保存原文件
            file.transferTo(targetFile);
        }

        log.info("【文件上传】保存完成");
        log.info("【文件上传】文件是否存在: {}", targetFile.exists());
        log.info("【文件上传】最终文件大小: {} bytes", targetFile.length());
        log.info("【文件上传】成功保存文件: {} -> {}", originalFilename, filename);

        // 返回访问URL
        String fileUrl = config.getLocal().getBaseUrl() + "/avatars/" + filename;
        log.info("【文件上传】文件URL: {}", fileUrl);

        // TODO: 生产环境使用OSS，返回OSS的URL
        return fileUrl;
    }

    @Override
    public void deleteAvatar(String fileUrl) throws Exception {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // 从URL提取文件名
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(config.getLocal().getUploadDir(), filename);

            // 删除文件
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("【文件删除】成功删除文件: {}", filename);
            }

            // TODO: 生产环境需要删除OSS上的文件
        } catch (IOException e) {
            log.error("【文件删除】删除文件失败: {}", fileUrl, e);
            throw e;
        }
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

        // 验证文件类型（扩展名）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("无效的文件名");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList(config.getAvatar().getAllowedTypes()).contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension +
                    "，仅支持: " + String.join(", ", config.getAvatar().getAllowedTypes()));
        }

        // 验证MIME类型（真实文件类型检查）
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(config.getAvatar().getAllowedMimeTypes()).contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件格式");
        }

        log.debug("【文件验证】文件验证通过: {}, 大小: {} bytes, 类型: {}",
                originalFilename, file.getSize(), contentType);
    }
}
