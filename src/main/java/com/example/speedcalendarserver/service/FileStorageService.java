package com.example.speedcalendarserver.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 * 策略模式：支持本地存储和OSS存储
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
public interface FileStorageService {

    /**
     * 上传头像
     *
     * @param file 文件
     * @param userId 用户ID
     * @return 文件访问URL
     * @throws Exception 上传失败时抛出异常
     */
    String uploadAvatar(MultipartFile file, String userId) throws Exception;

    /**
     * 删除头像
     *
     * @param fileUrl 文件URL或文件名
     * @throws Exception 删除失败时抛出异常
     */
    void deleteAvatar(String fileUrl) throws Exception;

    /**
     * 验证文件
     *
     * @param file 文件
     * @throws IllegalArgumentException 验证失败时抛出异常
     */
    void validateFile(MultipartFile file) throws IllegalArgumentException;
}
