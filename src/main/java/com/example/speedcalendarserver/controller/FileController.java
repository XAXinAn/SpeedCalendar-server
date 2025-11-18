package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问控制器
 * 提供本地文件的HTTP访问
 *
 * TODO: 生产环境使用OSS时可以移除此Controller
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local", matchIfMissing = true)
public class FileController {

    private final FileStorageConfig config;

    /**
     * 访问头像文件
     *
     * GET /api/files/avatars/{filename}
     *
     * TODO: 生产环境使用OSS，前端直接访问OSS URL，无需此接口
     *
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/avatars/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            // 构建文件路径 - 从配置读取上传目录
            Path filePath = Paths.get(config.getLocal().getUploadDir()).resolve(filename).normalize();
            log.info("【文件访问】请求文件: {}, 完整路径: {}", filename, filePath.toAbsolutePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("【文件访问】文件不存在或不可读: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // 根据文件扩展名动态设置ContentType
            MediaType contentType = getMediaTypeForFilename(filename);

            // 返回文件
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000") // 缓存1年
                    .body(resource);

        } catch (Exception e) {
            log.error("【文件访问】访问文件失败: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据文件名获取MediaType
     *
     * @param filename 文件名
     * @return MediaType
     */
    private MediaType getMediaTypeForFilename(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.APPLICATION_OCTET_STREAM; // 默认类型
        };
    }
}
