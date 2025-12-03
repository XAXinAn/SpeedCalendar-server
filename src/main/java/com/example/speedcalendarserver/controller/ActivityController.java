package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.*;
import com.example.speedcalendarserver.service.ActivityMessageService;
import com.example.speedcalendarserver.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 活动消息控制器
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Slf4j
@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityMessageService activityMessageService;
    private final JwtUtil jwtUtil;

    /**
     * 获取活动消息列表
     *
     * GET /api/activity/messages
     * Headers: Authorization: Bearer {token}
     * Query: page (可选，默认1), pageSize (可选，默认20)
     *
     * @param page        页码
     * @param pageSize    每页数量
     * @param httpRequest HTTP请求
     * @return 消息列表
     */
    @GetMapping("/messages")
    public ApiResponse<ActivityMessageListResponse> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【获取消息列表】userId: {}, page: {}, pageSize: {}", userId, page, pageSize);

            ActivityMessageListResponse response = activityMessageService.getMessages(userId, page, pageSize);

            return ApiResponse.success("success", response);
        } catch (Exception e) {
            log.error("【获取消息列表失败】{}", e.getMessage(), e);
            return ApiResponse.error("服务器错误");
        }
    }

    /**
     * 获取未读消息数量
     *
     * GET /api/activity/unread-count
     * Headers: Authorization: Bearer {token}
     *
     * @param httpRequest HTTP请求
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount(HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【获取未读数量】userId: {}", userId);

            UnreadCountResponse response = activityMessageService.getUnreadCount(userId);

            return ApiResponse.success("success", response);
        } catch (Exception e) {
            log.error("【获取未读数量失败】{}", e.getMessage(), e);
            return ApiResponse.error("服务器错误");
        }
    }

    /**
     * 标记单条消息已读
     *
     * POST /api/activity/messages/{messageId}/read
     * Headers: Authorization: Bearer {token}
     *
     * @param messageId   消息ID
     * @param httpRequest HTTP请求
     * @return 操作结果
     */
    @PostMapping("/messages/{messageId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable String messageId,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【标记已读】userId: {}, messageId: {}", userId, messageId);

            activityMessageService.markAsRead(userId, messageId);

            return ApiResponse.success("success", null);
        } catch (IllegalArgumentException e) {
            log.warn("【标记已读失败】{}", e.getMessage());
            return ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
        } catch (Exception e) {
            log.error("【标记已读失败】{}", e.getMessage(), e);
            return ApiResponse.error("服务器错误");
        }
    }

    /**
     * 标记全部消息已读
     *
     * POST /api/activity/messages/read-all
     * Headers: Authorization: Bearer {token}
     *
     * @param httpRequest HTTP请求
     * @return 标记已读的数量
     */
    @PostMapping("/messages/read-all")
    public ApiResponse<ReadAllResponse> markAllAsRead(HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【全部已读】userId: {}", userId);

            ReadAllResponse response = activityMessageService.markAllAsRead(userId);

            return ApiResponse.success("success", response);
        } catch (Exception e) {
            log.error("【全部已读失败】{}", e.getMessage(), e);
            return ApiResponse.error("服务器错误");
        }
    }

    /**
     * 获取消息详情
     *
     * GET /api/activity/messages/{messageId}
     * Headers: Authorization: Bearer {token}
     *
     * @param messageId   消息ID
     * @param httpRequest HTTP请求
     * @return 消息详情
     */
    @GetMapping("/messages/{messageId}")
    public ApiResponse<ActivityMessageDTO> getMessageDetail(
            @PathVariable String messageId,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【获取消息详情】userId: {}, messageId: {}", userId, messageId);

            ActivityMessageDTO response = activityMessageService.getMessageDetail(userId, messageId);

            return ApiResponse.success("success", response);
        } catch (IllegalArgumentException e) {
            log.warn("【获取消息详情失败】{}", e.getMessage());
            return ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
        } catch (Exception e) {
            log.error("【获取消息详情失败】{}", e.getMessage(), e);
            return ApiResponse.error("服务器错误");
        }
    }

    /**
     * 从请求中获取用户ID
     *
     * @param request HTTP请求
     * @return 用户ID，如果token无效返回null
     */
    private String getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        }
        return null;
    }
}
