package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.ApiResponse;
import com.example.speedcalendarserver.dto.CreateScheduleRequest;
import com.example.speedcalendarserver.dto.ScheduleDTO;
import com.example.speedcalendarserver.dto.UpdateScheduleRequest;
import com.example.speedcalendarserver.service.ScheduleService;
import com.example.speedcalendarserver.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日程控制器
 * 处理日程的创建、查询、更新、删除等相关请求
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final JwtUtil jwtUtil;

    /**
     * 获取指定月份的日程
     *
     * GET /api/schedules?year=2024&month=7
     * Headers: Authorization: Bearer {token}
     * 响应: {
     *   "code": 200,
     *   "message": "获取成功",
     *   "data": [
     *     {
     *       "scheduleId": "xxx",
     *       "userId": "xxx",
     *       "title": "会议",
     *       "scheduleDate": "2024-07-15",
     *       "startTime": "14:00",
     *       "endTime": "15:00",
     *       "location": "会议室",
     *       "isAllDay": false,
     *       "createdAt": 1234567890000
     *     }
     *   ]
     * }
     *
     * @param year        年份
     * @param month       月份
     * @param httpRequest HTTP请求
     * @return 日程列表
     */
    @GetMapping
    public ApiResponse<List<ScheduleDTO>> getSchedules(
            @RequestParam Integer year,
            @RequestParam Integer month,
            HttpServletRequest httpRequest
    ) {
        try {
            // 从token中获取用户ID
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【获取日程列表】userId: {}, year: {}, month: {}", userId, year, month);

            List<ScheduleDTO> schedules = scheduleService.getSchedulesByMonth(userId, year, month);

            return ApiResponse.success("获取成功", schedules);
        } catch (Exception e) {
            log.error("【获取日程列表失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建新日程
     *
     * POST /api/schedules
     * Headers: Authorization: Bearer {token}
     * 请求体: {
     *   "title": "会议",
     *   "scheduleDate": "2024-07-15",
     *   "startTime": "14:00",
     *   "endTime": "15:00",
     *   "location": "会议室",
     *   "isAllDay": false
     * }
     * 响应: {
     *   "code": 200,
     *   "message": "创建成功",
     *   "data": { ... }
     * }
     *
     * @param request     创建日程请求
     * @param httpRequest HTTP请求
     * @return 创建的日程
     */
    @PostMapping
    public ApiResponse<ScheduleDTO> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            // 从token中获取用户ID
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【创建日程】userId: {}, title: {}", userId, request.getTitle());

            ScheduleDTO schedule = scheduleService.createSchedule(userId, request);

            return ApiResponse.success("创建成功", schedule);
        } catch (Exception e) {
            log.error("【创建日程失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新日程
     *
     * PUT /api/schedules/{scheduleId}
     * Headers: Authorization: Bearer {token}
     * 请求体: {
     *   "title": "会议（已更新）",
     *   "scheduleDate": "2024-07-15",
     *   "startTime": "15:00",
     *   "endTime": "16:00",
     *   "location": "会议室A",
     *   "isAllDay": false
     * }
     * 响应: {
     *   "code": 200,
     *   "message": "更新成功",
     *   "data": { ... }
     * }
     *
     * @param scheduleId  日程ID
     * @param request     更新日程请求
     * @param httpRequest HTTP请求
     * @return 更新后的日程
     */
    @PutMapping("/{scheduleId}")
    public ApiResponse<ScheduleDTO> updateSchedule(
            @PathVariable String scheduleId,
            @Valid @RequestBody UpdateScheduleRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            // 从token中获取用户ID
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【更新日程】userId: {}, scheduleId: {}", userId, scheduleId);

            ScheduleDTO schedule = scheduleService.updateSchedule(userId, scheduleId, request);

            return ApiResponse.success("更新成功", schedule);
        } catch (Exception e) {
            log.error("【更新日程失败】{}", e.getMessage(), e);
            if (e.getMessage().contains("不存在")) {
                return ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
            }
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除日程
     *
     * DELETE /api/schedules/{scheduleId}
     * Headers: Authorization: Bearer {token}
     * 响应: {
     *   "code": 204,
     *   "message": "删除成功",
     *   "data": null
     * }
     *
     * @param scheduleId  日程ID
     * @param httpRequest HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Void> deleteSchedule(
            @PathVariable String scheduleId,
            HttpServletRequest httpRequest
    ) {
        try {
            // 从token中获取用户ID
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【删除日程】userId: {}, scheduleId: {}", userId, scheduleId);

            scheduleService.deleteSchedule(userId, scheduleId);

            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("【删除日程失败】{}", e.getMessage(), e);
            if (e.getMessage().contains("不存在")) {
                return ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
            }
            return ApiResponse.error(e.getMessage());
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
