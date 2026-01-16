package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.ApiResponse;
import com.example.speedcalendarserver.service.StatsService;
import com.example.speedcalendarserver.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final JwtUtil jwtUtil;

    /**
     * 分类占比统计 (B1)
     * GET /stats/categories?month=2023-11
     */
    @GetMapping("/categories")
    public ApiResponse<Map<String, Long>> getCategoryStats(
            @RequestParam String month,
            HttpServletRequest httpRequest
    ) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【分类统计】userId: {}, month: {}", userId, month);
            Map<String, Long> stats = statsService.getCategoryStats(userId, month);
            return ApiResponse.success("获取成功", stats);
        } catch (Exception e) {
            log.error("【分类统计失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 周活跃度统计 (B2)
     * GET /stats/weekly-activity?startDate=2023-10-01&endDate=2023-11-30
     */
    @GetMapping("/weekly-activity")
    public ApiResponse<List<Map<String, Object>>> getWeeklyActivityStats(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpServletRequest httpRequest
    ) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【周活跃统计】userId: {}, range: {} ~ {}", userId, startDate, endDate);
            List<Map<String, Object>> stats = statsService.getWeeklyActivityStats(userId, startDate, endDate);
            return ApiResponse.success("获取成功", stats);
        } catch (Exception e) {
            log.error("【周活跃统计失败】{}", e.getMessage(), e);
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
