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

@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<List<ScheduleDTO>> getSchedules(
            @RequestParam Integer year,
            @RequestParam Integer month,
            HttpServletRequest httpRequest
    ) {
        try {
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

    @PostMapping
    public ApiResponse<ScheduleDTO> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            log.info("【创建日程】userId: {}, title: {}", userId, request.getTitle());
            ScheduleDTO schedule = scheduleService.createSchedule(userId, request);
            return ApiResponse.success("创建成功", schedule);
        } catch (SecurityException e) {
            log.error("【创建日程失败 - 权限不足】{}", e.getMessage());
            return ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage());
        } catch (Exception e) {
            log.error("【创建日程失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{scheduleId}")
    public ApiResponse<ScheduleDTO> updateSchedule(
            @PathVariable String scheduleId,
            @Valid @RequestBody UpdateScheduleRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            log.info("【更新日程】userId: {}, scheduleId: {}", userId, scheduleId);
            ScheduleDTO schedule = scheduleService.updateSchedule(userId, scheduleId, request);
            return ApiResponse.success("更新成功", schedule);
        } catch (SecurityException e) {
            log.error("【更新日程失败 - 权限不足】{}", e.getMessage());
            return ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("【更新日程失败】{}", e.getMessage(), e);
            if (e.getMessage().contains("不存在")) {
                return ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
            }
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Void> deleteSchedule(
            @PathVariable String scheduleId,
            HttpServletRequest httpRequest
    ) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            log.info("【删除日程】userId: {}, scheduleId: {}", userId, scheduleId);
            scheduleService.deleteSchedule(userId, scheduleId);
            return ApiResponse.success("删除成功", null);
        } catch (SecurityException e) {
            log.error("【删除日程失败 - 权限不足】{}", e.getMessage());
            return ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("【删除日程失败】{}", e.getMessage(), e);
            if (e.getMessage().contains("不存在")) {
                return ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
            }
            return ApiResponse.error(e.getMessage());
        }
    }

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
