package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.ApiResponse;
import com.example.speedcalendarserver.dto.LoginResponse;
import com.example.speedcalendarserver.dto.PhoneLoginRequest;
import com.example.speedcalendarserver.dto.RefreshTokenRequest;
import com.example.speedcalendarserver.dto.RefreshTokenResponse;
import com.example.speedcalendarserver.dto.SendCodeRequest;
import com.example.speedcalendarserver.dto.UpdateUserInfoRequest;
import com.example.speedcalendarserver.dto.UserInfo;
import com.example.speedcalendarserver.service.AuthService;
import com.example.speedcalendarserver.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户登录、注册、验证码等相关请求
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送验证码
     *
     * POST /api/auth/code
     * 请求体: { "phone": "13800138000" }
     * 响应: { "code": 200, "message": "验证码发送成功", "data": null }
     *
     * @param request     发送验证码请求
     * @param httpRequest HTTP请求
     * @return 统一响应
     */
    @PostMapping("/code")
    public ApiResponse<Void> sendCode(
            @Valid @RequestBody SendCodeRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = IpUtil.getClientIp(httpRequest);
            log.info("【发送验证码】手机号: {}, IP: {}", request.getPhone(), ipAddress);

            authService.sendVerificationCode(request, ipAddress);

            return ApiResponse.success("验证码发送成功", null);
        } catch (Exception e) {
            log.error("【发送验证码失败】{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 手机号登录
     *
     * POST /api/auth/login/phone
     * 请求体: { "phone": "13800138000", "code": "123456" }
     * 响应: {
     * "code": 200,
     * "message": "登录成功",
     * "data": {
     * "userId": "xxx",
     * "token": "jwt-access-token",
     * "refreshToken": "jwt-refresh-token",
     * "expiresIn": 7200,
     * "userInfo": { ... }
     * }
     * }
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求
     * @return 登录响应
     */
    @PostMapping("/login/phone")
    public ApiResponse<LoginResponse> phoneLogin(
            @Valid @RequestBody PhoneLoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = IpUtil.getClientIp(httpRequest);
            log.info("【手机号登录】手机号: {}, IP: {}", request.getPhone(), ipAddress);

            LoginResponse loginResponse = authService.phoneLogin(request, httpRequest);

            log.info("【登录成功】userId: {}, phone: {}", loginResponse.getUserId(), request.getPhone());

            return ApiResponse.success("登录成功", loginResponse);
        } catch (Exception e) {
            log.error("【登录失败】{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 刷新Token
     *
     * POST /api/auth/refresh
     * 请求体: { "refreshToken": "xxx" }
     * 响应: { "code": 200, "message": "刷新成功", "data": { "token": "xxx",
     * "refreshToken": "xxx", "expiresIn": 7200 } }
     *
     * @param request     刷新Token请求
     * @param httpRequest HTTP请求
     * @return 刷新Token响应
     */
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = IpUtil.getClientIp(httpRequest);
            log.info("【刷新Token】IP: {}", ipAddress);

            RefreshTokenResponse response = authService.refreshToken(request.getRefreshToken(), httpRequest);

            log.info("【刷新Token成功】");

            return ApiResponse.success("刷新成功", response);
        } catch (Exception e) {
            log.error("【刷新Token失败】{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     *
     * PUT /api/auth/user/{userId}
     * 请求体: { "username": "新昵称", "avatar": "头像URL" }
     * 响应: { "code": 200, "message": "更新成功", "data": { ... } }
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/user/{userId}")
    public ApiResponse<UserInfo> updateUserInfo(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserInfoRequest request) {
        try {
            log.info("【更新用户信息】userId: {}, request: {}", userId, request);

            UserInfo userInfo = authService.updateUserInfo(userId, request);

            return ApiResponse.success("更新成功", userInfo);
        } catch (Exception e) {
            log.error("【更新用户信息失败】{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户信息（支持隐私过滤）
     *
     * GET /api/auth/user/{userId}?requesterId=xxx
     * 响应: { "code": 200, "message": "获取成功", "data": { ... } }
     *
     * @param userId      用户ID（被查看者）
     * @param requesterId 请求者ID（查看者），可选参数。不提供则返回完整信息
     * @return 用户信息（根据隐私设置过滤）
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<UserInfo> getUserInfo(
            @PathVariable String userId,
            @RequestParam(required = false) String requesterId) {
        try {
            log.info("【获取用户信息】targetUserId: {}, requesterId: {}", userId, requesterId);

            UserInfo userInfo = authService.getUserInfo(userId, requesterId);

            return ApiResponse.success("获取成功", userInfo);
        } catch (Exception e) {
            log.error("【获取用户信息失败】{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 健康检查
     *
     * GET /api/auth/health
     * 响应: { "code": 200, "message": "服务正常", "data": null }
     *
     * @return 健康检查响应
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("服务正常", "SpeedCalendar Auth Service is running");
    }
}
