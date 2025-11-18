package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.ApiResponse;
import com.example.speedcalendarserver.dto.PrivacySettingDTO;
import com.example.speedcalendarserver.dto.UpdatePrivacySettingsRequest;
import com.example.speedcalendarserver.service.PrivacyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 隐私设置控制器
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Slf4j
@RestController
@RequestMapping("/privacy")
@RequiredArgsConstructor
public class PrivacyController {

    private final PrivacyService privacyService;

    /**
     * 获取用户的隐私设置
     *
     * GET /api/privacy/settings/{userId}
     * 响应: { "code": 200, "message": "获取成功", "data": [...] }
     *
     * @param userId 用户ID
     * @return 隐私设置列表
     */
    @GetMapping("/settings/{userId}")
    public ApiResponse<List<PrivacySettingDTO>> getPrivacySettings(@PathVariable String userId) {
        try {
            log.info("【获取隐私设置】userId: {}", userId);

            List<PrivacySettingDTO> settings = privacyService.getUserPrivacySettings(userId);

            return ApiResponse.success("获取成功", settings);
        } catch (Exception e) {
            log.error("【获取隐私设置失败】{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 批量更新用户的隐私设置
     *
     * PUT /api/privacy/settings/{userId}
     * 请求体: { "settings": [{"fieldName": "phone", "visibilityLevel": "PRIVATE"}, ...] }
     * 响应: { "code": 200, "message": "更新成功", "data": null }
     *
     * @param userId 用户ID
     * @param request 更新请求
     * @return 统一响应
     */
    @PutMapping("/settings/{userId}")
    public ApiResponse<Void> updatePrivacySettings(
            @PathVariable String userId,
            @Valid @RequestBody UpdatePrivacySettingsRequest request
    ) {
        try {
            log.info("【更新隐私设置】userId: {}, settings: {}", userId, request.getSettings());

            privacyService.updatePrivacySettings(userId, request.getSettings());

            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            log.error("【更新隐私设置失败】{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 清除用户的隐私设置缓存
     * 用于调试和维护
     *
     * DELETE /api/privacy/cache/{userId}
     * 响应: { "code": 200, "message": "缓存已清除", "data": null }
     *
     * @param userId 用户ID
     * @return 统一响应
     */
    @DeleteMapping("/cache/{userId}")
    public ApiResponse<Void> clearCache(@PathVariable String userId) {
        try {
            log.info("【清除隐私设置缓存】userId: {}", userId);

            privacyService.clearCache(userId);

            return ApiResponse.success("缓存已清除", null);
        } catch (Exception e) {
            log.error("【清除缓存失败】{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
