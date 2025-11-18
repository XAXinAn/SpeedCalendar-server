package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.ApiResponse;
import com.example.speedcalendarserver.entity.User;
import com.example.speedcalendarserver.repository.UserRepository;
import com.example.speedcalendarserver.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 头像管理控制器
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Slf4j
@RestController
@RequestMapping("/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    /**
     * 上传用户头像
     *
     * POST /api/avatar/upload
     * Content-Type: multipart/form-data
     * 参数: file (文件), userId (用户ID)
     * 响应: { "code": 200, "message": "上传成功", "data": { "avatarUrl": "..." } }
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像URL
     */
    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId
    ) {
        try {
            log.info("【上传头像】用户ID: {}, 文件名: {}, 大小: {} bytes",
                    userId, file.getOriginalFilename(), file.getSize());

            // 查询用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

            // 删除旧头像（如果存在）
            String oldAvatarUrl = user.getAvatar();
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty() &&
                    !oldAvatarUrl.contains("dicebear.com")) { // 不删除默认头像
                try {
                    fileStorageService.deleteAvatar(oldAvatarUrl);
                    log.info("【上传头像】已删除旧头像: {}", oldAvatarUrl);
                } catch (Exception e) {
                    log.warn("【上传头像】删除旧头像失败: {}", oldAvatarUrl, e);
                }
            }

            // 上传新头像
            String avatarUrl = fileStorageService.uploadAvatar(file, userId);

            // 更新数据库
            user.setAvatar(avatarUrl);
            userRepository.save(user);

            log.info("【上传头像】上传成功: {}", avatarUrl);

            // 返回结果
            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);

            return ApiResponse.success("上传成功", result);

        } catch (IllegalArgumentException e) {
            log.warn("【上传头像】参数错误: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("【上传头像】上传失败", e);
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户头像（恢复为默认头像）
     *
     * DELETE /api/avatar/{userId}
     * 响应: { "code": 200, "message": "删除成功", "data": null }
     *
     * @param userId 用户ID
     * @return 响应结果
     */
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteAvatar(@PathVariable String userId) {
        try {
            log.info("【删除头像】用户ID: {}", userId);

            // 查询用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

            // 删除头像文件
            String avatarUrl = user.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty() &&
                    !avatarUrl.contains("dicebear.com")) {
                fileStorageService.deleteAvatar(avatarUrl);
            }

            // 恢复默认头像
            String defaultAvatar = "https://api.dicebear.com/7.x/initials/svg?seed=" + user.getUsername();
            user.setAvatar(defaultAvatar);
            userRepository.save(user);

            log.info("【删除头像】删除成功，已恢复默认头像");

            return ApiResponse.success("删除成功", null);

        } catch (IllegalArgumentException e) {
            log.warn("【删除头像】参数错误: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("【删除头像】删除失败", e);
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}
